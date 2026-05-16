package fr.enzogiardinelli.synkro.dtos.tasks;

import fr.enzogiardinelli.synkro.annotations.ValidEnum;
import fr.enzogiardinelli.synkro.entities.tasks.TaskStatus;
import jakarta.persistence.Table;
import jakarta.validation.Constraint;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;
import java.time.LocalDateTime;

import static java.lang.annotation.ElementType.*;

public class PatchTaskRequest {

    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    private String description;

    @ValidEnum(enumClass = TaskStatus.class, message = "Status must be one of: TODO, IN_PROGRESS, DONE")
    private String status;

    @Future(message = "Due date must be in the future")
    private LocalDateTime dueDate;
    private boolean removeDueDate;

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

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isRemoveDueDate() {
        return removeDueDate;
    }
    public void setRemoveDueDate(boolean removeDueDate) {
        this.removeDueDate = removeDueDate;
    }


}
