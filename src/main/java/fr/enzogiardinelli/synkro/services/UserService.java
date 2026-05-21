package fr.enzogiardinelli.synkro.services;

import fr.enzogiardinelli.synkro.dtos.user.response.UserResponse;
import fr.enzogiardinelli.synkro.entities.projects.Project;
import fr.enzogiardinelli.synkro.entities.tasks.Task;
import fr.enzogiardinelli.synkro.entities.users.User;
import fr.enzogiardinelli.synkro.entities.users.UserRole;
import fr.enzogiardinelli.synkro.exceptions.ConflictException;
import fr.enzogiardinelli.synkro.exceptions.ResourceNotFoundException;
import fr.enzogiardinelli.synkro.exceptions.UnauthorizedAccessException;
import fr.enzogiardinelli.synkro.repositories.ProjectRepository;
import fr.enzogiardinelli.synkro.repositories.TaskRepository;
import fr.enzogiardinelli.synkro.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Business logic for users.
 * Used by UserController and AdminController
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final RefreshTokenService refreshTokenService;

    public UserService(UserRepository userRepository, ProjectRepository projectRepository, TaskRepository taskRepository, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.refreshTokenService = refreshTokenService;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse changeUserRole(UUID userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        UserRole role;
        try {
            role = UserRole.valueOf("ROLE_" + newRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ConflictException("Invalid role. Use USER or ADMIN");
        }

        user.setRole(role);
        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    /**
     * Deletes a user ,cleaning what they are linked to (tokens, tasks, ...)
     * Deny if user owns projects - call to transfer/deletion.
     */
    @Transactional
    public void deleteUser(User user) {
        List<Project> ownedProject = projectRepository.findByOwner(user);
        if (!ownedProject.isEmpty()) {
            throw new UnauthorizedAccessException("Cannot delete user: owns " + ownedProject.size() + " project(s). Transfer ownership or delete them first");
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

        refreshTokenService.deleteByUserId(user);
        userRepository.delete(user);
    }
}
