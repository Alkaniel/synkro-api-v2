package fr.enzogiardinelli.synkro.dtos.tasks.response;

import fr.enzogiardinelli.synkro.dtos.user.response.UserSummary;
import fr.enzogiardinelli.synkro.entities.tasks.Task;
import fr.enzogiardinelli.synkro.entities.tasks.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * View of a task for client side
 * Don't contain all the project entity, only their ids for avoiding infinite loops.
 */
public record TaskResponse(UUID id, String title, String description, TaskStatus status, LocalDateTime dueDate, UUID projectId, List<UserSummary> assignees) {
    public static TaskResponse from(Task task) {
        List<UserSummary> assignees = task.getAssignees().stream()
                .map(UserSummary::from)
                .toList();

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDueDate(),
                task.getProject().getId(),
                assignees
        );
    }
}
