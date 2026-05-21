package fr.enzogiardinelli.synkro.dtos.user.response;

import fr.enzogiardinelli.synkro.entities.users.User;
import fr.enzogiardinelli.synkro.entities.users.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Complete view of a user for the owner (or an admin)
 * Include global role, creation date, but no password.
 */
public record UserResponse(UUID id, String email, String username, UserRole role, LocalDateTime createdAt) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
