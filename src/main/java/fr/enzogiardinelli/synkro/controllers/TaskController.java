package fr.enzogiardinelli.synkro.controllers;

import fr.enzogiardinelli.synkro.dtos.tasks.AssigneesRequest;
import fr.enzogiardinelli.synkro.dtos.tasks.TaskRequest;
import fr.enzogiardinelli.synkro.entities.projects.Project;
import fr.enzogiardinelli.synkro.entities.projects.ProjectParticipant;
import fr.enzogiardinelli.synkro.entities.projects.ProjectRole;
import fr.enzogiardinelli.synkro.entities.tasks.Task;
import fr.enzogiardinelli.synkro.entities.tasks.TaskStatus;
import fr.enzogiardinelli.synkro.repositories.ProjectRepository;
import fr.enzogiardinelli.synkro.repositories.TaskRepository;
import fr.enzogiardinelli.synkro.repositories.UserRepository;
import fr.enzogiardinelli.synkro.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/")
public class TaskController {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskController(TaskRepository taskRepository, ProjectRepository projectRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    private boolean isGlobalAdmin(CustomUserDetails currentUser) {
        return currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    private ProjectRole checkUserRole(Project project, CustomUserDetails currentUser) {
        if (isGlobalAdmin(currentUser)) {
            return ProjectRole.OWNER;
        }

        return project.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(currentUser.getUser().getId()))
                .map(ProjectParticipant::getProjectRole)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unauthorized : You are not a member of this project"));
    }

    @GetMapping("/projects/{projectId}/tasks")
    public List<Task> getProjectTasks(@PathVariable UUID projectId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        checkUserRole(project, currentUser);

        return taskRepository.findByProjectId(projectId);
    }

    @PostMapping("/projects/{projectId}/tasks")
    public String createTask(@PathVariable UUID projectId, @Valid @RequestBody TaskRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        ProjectRole role = checkUserRole(project, currentUser);
        if (role == ProjectRole.VIEWER) {
            throw new RuntimeException("Unauthorized : VIEWER cannot create tasks");
        }

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setProject(project);
        task.setStatus(TaskStatus.TODO);

        taskRepository.save(task);
        return "Task " + task.getTitle() + " created successfully";
    }

    @PatchMapping("/tasks/{id}/status")
    public String updateTaskStatus(@PathVariable UUID id, @Valid @RequestBody TaskRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));

        ProjectRole role = checkUserRole(task.getProject(), currentUser);
        if (role == ProjectRole.VIEWER) throw new RuntimeException("Unauthorized : VIEWER cannot change status");

        try {
            task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
            taskRepository.save(task);
            return "Task status updated to " + task.getStatus();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status");
        }
    }

    @PutMapping("/tasks/{id}")
    public String updateTaskDetails(@PathVariable UUID id, @Valid @RequestBody TaskRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));

        ProjectRole role = checkUserRole(task.getProject(), currentUser);
        if (role == ProjectRole.VIEWER) throw new RuntimeException("Unauthorized : VIEWER cannot update tasks");

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());

        taskRepository.save(task);
        return "Task details updated successfully";
    }

    @DeleteMapping("/tasks/{id}")
    public String deleteTask(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));

        ProjectRole role = checkUserRole(task.getProject(), currentUser);
        if (role == ProjectRole.VIEWER) throw new RuntimeException("Unauthorized : VIEWER cannot delete tasks");

        taskRepository.delete(task);
        return "Task deleted successfully";
    }

    @PostMapping("/tasks/{id}/assign")
    public String assignMembers(@PathVariable UUID id, @Valid @RequestBody AssigneesRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));

        ProjectRole role = checkUserRole(task.getProject(), currentUser);
        if (role == ProjectRole.VIEWER) throw new RuntimeException("Unauthorized : VIEWER cannot assign members");

        if (request.getEmails() != null) {
            task.getAssignees().clear();
            for (String email : request.getEmails()) {
                userRepository.findByEmail(email).ifPresent(user -> task.getAssignees().add(user));
            }
            taskRepository.save(task);
        }
        return "Members assigned to task successfully";
    }
}