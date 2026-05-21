package fr.enzogiardinelli.synkro.controllers;

import fr.enzogiardinelli.synkro.dtos.user.UpdatePasswordRequest;
import fr.enzogiardinelli.synkro.dtos.user.UpdateProfileRequest;
import fr.enzogiardinelli.synkro.dtos.user.response.UserResponse;
import fr.enzogiardinelli.synkro.entities.users.User;
import fr.enzogiardinelli.synkro.exceptions.ConflictException;
import fr.enzogiardinelli.synkro.exceptions.InvalidCredentialsException;
import fr.enzogiardinelli.synkro.exceptions.ResourceNotFoundException;
import fr.enzogiardinelli.synkro.repositories.ProjectRepository;
import fr.enzogiardinelli.synkro.repositories.TaskRepository;
import fr.enzogiardinelli.synkro.repositories.UserRepository;
import fr.enzogiardinelli.synkro.security.CustomUserDetails;
import fr.enzogiardinelli.synkro.services.RefreshTokenService;
import fr.enzogiardinelli.synkro.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
public class UserController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, ProjectRepository projectRepository, TaskRepository taskRepository, RefreshTokenService refreshTokenService, UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(UserResponse.from(currentUser.getUser()));
    }

    @PatchMapping
    public ResponseEntity<UserResponse> updateMyProfile(@RequestBody @Valid UpdateProfileRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getUser().getId()).orElseThrow(() -> new ResourceNotFoundException("User", currentUser.getUser().getId()));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!user.getEmail().equals(request.getEmail()) && userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new ConflictException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }

        userRepository.save(user);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> updateMyPassword(@RequestBody @Valid UpdatePasswordRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getUser().getId()).orElseThrow(() -> new ResourceNotFoundException("User", currentUser.getUser().getId()));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getUser().getId()).orElseThrow(() -> new ResourceNotFoundException("User", currentUser.getUser().getId()));
        userService.deleteUser(user);
        return ResponseEntity.noContent().build();
    }
}
