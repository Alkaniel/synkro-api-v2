package fr.enzogiardinelli.synkro.dtos.user.response;

import fr.enzogiardinelli.synkro.entities.users.User;

import java.util.UUID;

/**
 * Public and compact view of a user, for list.
 * Contains no sensitive data as password, role, or creation date.
 */
public record UserSummary(UUID id, String username, String email) {
    public static UserSummary from(User user) {
        return new UserSummary(user.getId(), user.getUsername(), user.getEmail());
    }
}
