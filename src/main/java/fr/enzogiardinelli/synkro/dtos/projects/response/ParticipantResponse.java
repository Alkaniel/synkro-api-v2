package fr.enzogiardinelli.synkro.dtos.projects.response;

import fr.enzogiardinelli.synkro.dtos.user.response.UserSummary;
import fr.enzogiardinelli.synkro.entities.projects.ProjectParticipant;
import fr.enzogiardinelli.synkro.entities.projects.ProjectRole;

/**
 * Project participant view : contains user and their project role
 */
public record ParticipantResponse(UserSummary user, ProjectRole projectRole) {
    public static ParticipantResponse from(ProjectParticipant participant) {
        return new ParticipantResponse(
                UserSummary.from(participant.getUser()),
                participant.getProjectRole()
        );
    }
}
