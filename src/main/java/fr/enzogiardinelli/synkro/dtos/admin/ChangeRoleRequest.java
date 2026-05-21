package fr.enzogiardinelli.synkro.dtos.admin;

import fr.enzogiardinelli.synkro.annotations.ValidEnum;
import fr.enzogiardinelli.synkro.entities.users.UserRole;

public class ChangeRoleRequest {

    @ValidEnum(enumClass = UserRole.class, message = "Role must be one of: USER, ADMIN")
    private String role;

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
}
