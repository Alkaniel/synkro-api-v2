package fr.enzogiardinelli.synkro.dtos.tasks;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public class AssigneesRequest {

    private Set<@NotBlank @Email(message = "One of emails invalid") String> emails;

    public Set<String> getEmails() {
        return emails;
    }
    public void setEmails(Set<String> emails) {
        this.emails = emails;
    }
}
