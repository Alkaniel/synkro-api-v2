package fr.enzogiardinelli.synkro.services;

import fr.enzogiardinelli.synkro.dtos.tasks.AssigneesRequest;
import fr.enzogiardinelli.synkro.dtos.tasks.PatchTaskRequest;
import fr.enzogiardinelli.synkro.dtos.tasks.TaskRequest;
import fr.enzogiardinelli.synkro.dtos.tasks.response.TaskResponse;
import fr.enzogiardinelli.synkro.entities.projects.Project;
import fr.enzogiardinelli.synkro.entities.projects.ProjectRole;
import fr.enzogiardinelli.synkro.entities.tasks.Task;
import fr.enzogiardinelli.synkro.entities.tasks.TaskStatus;
import fr.enzogiardinelli.synkro.entities.users.User;
import fr.enzogiardinelli.synkro.exceptions.ResourceNotFoundException;
import fr.enzogiardinelli.synkro.exceptions.UnauthorizedAccessException;
import fr.enzogiardinelli.synkro.repositories.ProjectRepository;
import fr.enzogiardinelli.synkro.repositories.TaskRepository;
import fr.enzogiardinelli.synkro.repositories.UserRepository;
import fr.enzogiardinelli.synkro.security.CustomUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Task's business logic. Give role check to ProjectService.
 */
@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, UserRepository userRepository, ProjectService projectService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectService = projectService;
    }

    private Project findProjectOrThrow(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    private Task findTaskOrThrow(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }

    private void requireWriteAccess(Project project, CustomUserDetails currentUser) {
        ProjectRole role = projectService.getUserRoleInProject(project, currentUser);
        if (role == ProjectRole.VIEWER) {
            throw new UnauthorizedAccessException("VIEWER cannot modify tasks");
        }
    }

    /**
     * Resolve a set of email as a set of User. Ignore if emails don't exist.
     */
    private Set<User> resolveAssignees(Set<String> emails) {
        if (emails == null || emails.isEmpty()) return new HashSet<>();
        Set<User> users = new HashSet<>();
        for (String email : emails) {
            userRepository.findByEmail(email).ifPresent(users::add);
        }
        return users;
    }

    public List<TaskResponse> getProjectTasks(UUID projectId, CustomUserDetails currentUser) {
        Project project = findProjectOrThrow(projectId);
        projectService.getUserRoleInProject(project, currentUser); // 403 si pas membre

        return taskRepository.findByProjectId(projectId).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional
    public TaskResponse createTask(UUID projectId, TaskRequest request, CustomUserDetails currentUser) {
        Project project = findProjectOrThrow(projectId);
        requireWriteAccess(project, currentUser);

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setProject(project);

        if (request.getStatus() != null) {
            task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
        } else {
            task.setStatus(TaskStatus.TODO);
        }
        task.setAssignees(resolveAssignees(request.getAssigneeEmails()));

        Task saved = taskRepository.save(task);
        return TaskResponse.from(saved);
    }

    @Transactional
    public TaskResponse updateTaskDetails(UUID id, PatchTaskRequest request, CustomUserDetails currentUser) {
        Task task = findTaskOrThrow(id);
        requireWriteAccess(task.getProject(), currentUser);

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
        }
        if (request.isRemoveDueDate()) {
            task.setDueDate(null);
        } else if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        Task saved = taskRepository.save(task);
        return TaskResponse.from(saved);
    }

    @Transactional
    public TaskResponse updateTaskStatus(UUID id, PatchTaskRequest request, CustomUserDetails currentUser) {
        if (request.getStatus() == null) {
            throw new UnauthorizedAccessException("Status is required");
        }
        Task task = findTaskOrThrow(id);
        requireWriteAccess(task.getProject(), currentUser);

        task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
        Task saved = taskRepository.save(task);
        return TaskResponse.from(saved);
    }

    @Transactional
    public void deleteTask(UUID id, CustomUserDetails currentUser) {
        Task task = findTaskOrThrow(id);
        requireWriteAccess(task.getProject(), currentUser);
        taskRepository.delete(task);
    }

    @Transactional
    public TaskResponse assignMembers(UUID id, AssigneesRequest request, CustomUserDetails currentUser) {
        Task task = findTaskOrThrow(id);
        requireWriteAccess(task.getProject(), currentUser);

        task.getAssignees().clear();
        task.getAssignees().addAll(resolveAssignees(request.getEmails()));

        Task saved = taskRepository.save(task);
        return TaskResponse.from(saved);
    }
}
