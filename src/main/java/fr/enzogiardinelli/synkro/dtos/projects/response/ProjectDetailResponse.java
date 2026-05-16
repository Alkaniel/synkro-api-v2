package fr.enzogiardinelli.synkro.dtos.projects.response;

import fr.enzogiardinelli.synkro.dtos.user.response.UserSummary;
import fr.enzogiardinelli.synkro.entities.projects.Project;

import java.util.List;
import java.util.UUID;

/**
 * Detailed view of a project, w/ participants
 * Used by GET /projects/{id}
 */
public record ProjectDetailResponse(UUID id, String name, String description, String avatarUrl, UserSummary owner, List<ParticipantResponse> participants) {
    public static ProjectDetailResponse from(Project project) {
        List<ParticipantResponse> participants = project.getParticipants().stream()
                .map(ParticipantResponse::from)
                .toList();

        return new ProjectDetailResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getAvatarUrl(),
                UserSummary.from(project.getOwner()),
                participants
        );
    }
}
