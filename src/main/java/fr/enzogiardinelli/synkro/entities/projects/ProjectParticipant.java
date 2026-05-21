package fr.enzogiardinelli.synkro.entities.projects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.enzogiardinelli.synkro.entities.users.User;
import jakarta.persistence.*;

@Entity
@Table(name = "project_participants")
public class ProjectParticipant {

    @EmbeddedId
    private ProjectParticipantId id = new ProjectParticipantId();

    @JsonIgnore()
    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_role", nullable = false)
    private ProjectRole projectRole;

    public ProjectParticipant() {
    }

    public Project getProject() {
        return project;
    }
    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public ProjectRole getProjectRole() {
        return projectRole;
    }
    public void setProjectRole(ProjectRole projectRole) {
        this.projectRole = projectRole;
    }
}
