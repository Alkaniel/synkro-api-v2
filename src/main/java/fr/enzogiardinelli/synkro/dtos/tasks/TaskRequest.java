package fr.enzogiardinelli.synkro.dtos.tasks;

import fr.enzogiardinelli.synkro.annotations.ValidEnum;
import fr.enzogiardinelli.synkro.entities.tasks.TaskStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;

public class TaskRequest {

    @NotNull
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;
    private String description;

    @Future(message = "Due date must be in the future")
    private LocalDateTime dueDate;

    private Set<@NotNull @Email(message = "Invalid email format") String> assigneeEmails;

    @ValidEnum(enumClass = TaskStatus.class, message = "Status must be one of: TODO, IN_PROGRESS, DONE")
    private String status;

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Set<String> getAssigneeEmails() {
        return assigneeEmails;
    }
    public void setAssigneeEmails(Set<String> assigneeEmails) {
        this.assigneeEmails = assigneeEmails;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
