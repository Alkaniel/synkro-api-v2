package fr.enzogiardinelli.synkro.controllers;

import fr.enzogiardinelli.synkro.dtos.admin.ChangeRoleRequest;
import fr.enzogiardinelli.synkro.entities.projects.Project;
import fr.enzogiardinelli.synkro.entities.tasks.Task;
import fr.enzogiardinelli.synkro.entities.users.User;
import fr.enzogiardinelli.synkro.entities.users.UserRole;
import fr.enzogiardinelli.synkro.repositories.ProjectRepository;
import fr.enzogiardinelli.synkro.repositories.TaskRepository;
import fr.enzogiardinelli.synkro.repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public AdminController(UserRepository userRepository, ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PatchMapping("/users/{id}/role")
    public String changeUserRole(@PathVariable UUID id, @Valid @RequestBody ChangeRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            String roleName = "ROLE_" + request.getRole().toUpperCase();
            user.setRole(UserRole.valueOf(roleName));
            userRepository.save(user);
            return "Role of " + user.getUsername() +  "has been updated successfully to " + request.getRole();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + request.getRole() + " USE USER or ADMIN");
        }
    }

    @DeleteMapping("/users/{id}")
    public String deleteUserForce(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Project> ownedProjects = projectRepository.findByOwner(user);
        if (!ownedProjects.isEmpty()) {
            throw new RuntimeException("Cannot delete : User is the OWNER of " + ownedProjects.size() + " project(s). Transfer them first.");
        }

        List<Task> assignedTasks = taskRepository.findByAssignees_Id(user.getId());
        for (Task task : assignedTasks) {
            task.getAssignees().remove(user);
            taskRepository.save(task);
        }

        List<Project> participatingProjects = projectRepository.findByParticipants_User_Id(user.getId());
        for (Project project : participatingProjects) {
            project.getParticipants().removeIf(p -> p.getUser().getId().equals(user.getId()));
            projectRepository.save(project);
        }

        userRepository.delete(user);
        return "User " + user.getUsername() + " has been deleted successfully";
    }

}
