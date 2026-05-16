package fr.enzogiardinelli.synkro.dtos.projects;

import jakarta.validation.constraints.Email;

public class TransferOwnershipRequest {

    @Email(message = "Invalid email format")
    private String newOwnerEmail;

    public String getNewOwnerEmail() { return newOwnerEmail; }
    public void setNewOwnerEmail(String newOwnerEmail) { this.newOwnerEmail = newOwnerEmail; }
}