package fr.enzogiardinelli.synkro.dtos.projects;

import fr.enzogiardinelli.synkro.annotations.ValidEnum;
import fr.enzogiardinelli.synkro.entities.projects.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class ProjectMemberRequest {
    @Email(message = "Invalid email format")
    @NotNull(message = "Email is required")
    private String email;

    @ValidEnum(enumClass = ProjectRole.class, message = "Role must be one of: OWNER, EDITOR, VIEWER")
    @NotNull
    private String role;

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
}
