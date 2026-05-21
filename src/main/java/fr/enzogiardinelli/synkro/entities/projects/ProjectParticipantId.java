package fr.enzogiardinelli.synkro.entities.projects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ProjectParticipantId implements Serializable {
    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "user_id")
    private UUID userId;

    public ProjectParticipantId() {
    }

    public ProjectParticipantId(UUID projectId, UUID userId) {
        this.projectId = projectId;
        this.userId = userId;
    }

    public UUID getProjectId() {
        return projectId;
    }
    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getUserId() {
        return userId;
    }
    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProjectParticipantId that = (ProjectParticipantId) o;
        return Objects.equals(projectId, that.projectId) && Objects.equals(userId, that.userId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(projectId, userId);
    }
}
