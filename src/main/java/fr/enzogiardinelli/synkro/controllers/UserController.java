package fr.enzogiardinelli.synkro.controllers;

import fr.enzogiardinelli.synkro.dtos.user.UpdatePasswordRequest;
import fr.enzogiardinelli.synkro.dtos.user.UpdateProfileRequest;
import fr.enzogiardinelli.synkro.entities.projects.Project;
import fr.enzogiardinelli.synkro.entities.tasks.Task;
import fr.enzogiardinelli.synkro.entities.users.User;
import fr.enzogiardinelli.synkro.repositories.ProjectRepository;
import fr.enzogiardinelli.synkro.repositories.TaskRepository;
import fr.enzogiardinelli.synkro.repositories.UserRepository;
import fr.enzogiardinelli.synkro.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me")
public class UserController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    @GetMapping
    public User getMyProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return currentUser.getUser();
    }

    @PatchMapping
    public String updateMyProfile(@RequestBody @Valid UpdateProfileRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getUser().getId()).orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!user.getEmail().equals(request.getEmail()) && userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }

        userRepository.save(user);
        return "Profile updated successfully";
    }

    @PatchMapping("/password")
    public String updateMyPassword(@RequestBody @Valid UpdatePasswordRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getUser().getId()).orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return "Password updated successfully";
    }

    @DeleteMapping
    public String deleteMyAccount(@AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        List<Project> ownedProjets = projectRepository.findByOwner(user);
        if (!ownedProjets.isEmpty()) {
            throw new RuntimeException("Deletion cancelled : You own " + ownedProjets.size() + " project(s). Please transfer ownership or delete them before deleting your account.");
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
        return "Compte supprimé avec succès.";
    }
}
