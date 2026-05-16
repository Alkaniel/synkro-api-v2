package fr.enzogiardinelli.synkro.dtos.projects.response;

import fr.enzogiardinelli.synkro.dtos.user.response.UserSummary;
import fr.enzogiardinelli.synkro.entities.projects.Project;

import java.util.UUID;

/**
 * Compact view of a project, w/out participants
 * Used by GET /projects
 */
public record ProjectResponse(UUID id, String name, String description, String avatarUrl, UserSummary owner) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getAvatarUrl(),
                UserSummary.from(project.getOwner())
        );
    }
}
