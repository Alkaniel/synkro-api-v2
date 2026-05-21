package fr.enzogiardinelli.synkro.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
