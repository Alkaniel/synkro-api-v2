package fr.enzogiardinelli.synkro.dtos.user;

import jakarta.validation.constraints.Size;

public class UpdatePasswordRequest {
    @Size(min = 16, message = "Password must be at least 16 characters long")
    private String currentPassword;

    @Size(min = 16, message = "Password must be at least 16 characters long")
    private String newPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
