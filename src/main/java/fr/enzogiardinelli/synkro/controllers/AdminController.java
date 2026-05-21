package fr.enzogiardinelli.synkro.controllers;

import fr.enzogiardinelli.synkro.dtos.admin.ChangeRoleRequest;
import fr.enzogiardinelli.synkro.dtos.user.response.UserResponse;
import fr.enzogiardinelli.synkro.entities.projects.Project;
import fr.enzogiardinelli.synkro.entities.tasks.Task;
import fr.enzogiardinelli.synkro.entities.users.User;
import fr.enzogiardinelli.synkro.entities.users.UserRole;
import fr.enzogiardinelli.synkro.exceptions.ResourceNotFoundException;
import fr.enzogiardinelli.synkro.repositories.ProjectRepository;
import fr.enzogiardinelli.synkro.repositories.TaskRepository;
import fr.enzogiardinelli.synkro.repositories.UserRepository;
import fr.enzogiardinelli.synkro.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserRepository userRepository;
    private final UserService userService;

    public AdminController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> changeUserRole(@PathVariable UUID id, @Valid @RequestBody ChangeRoleRequest request) {
        return ResponseEntity.ok(userService.changeUserRole(id, request.getRole()));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUserForce(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        userService.deleteUser(user);
        return ResponseEntity.noContent().build();
    }

}
