package fr.enzogiardinelli.synkro.controllers;

import fr.enzogiardinelli.synkro.dtos.tasks.AssigneesRequest;
import fr.enzogiardinelli.synkro.dtos.tasks.PatchTaskRequest;
import fr.enzogiardinelli.synkro.dtos.tasks.TaskRequest;
import fr.enzogiardinelli.synkro.dtos.tasks.response.TaskResponse;
import fr.enzogiardinelli.synkro.entities.projects.Project;
import fr.enzogiardinelli.synkro.entities.projects.ProjectParticipant;
import fr.enzogiardinelli.synkro.entities.projects.ProjectRole;
import fr.enzogiardinelli.synkro.entities.tasks.Task;
import fr.enzogiardinelli.synkro.entities.tasks.TaskStatus;
import fr.enzogiardinelli.synkro.repositories.ProjectRepository;
import fr.enzogiardinelli.synkro.repositories.TaskRepository;
import fr.enzogiardinelli.synkro.repositories.UserRepository;
import fr.enzogiardinelli.synkro.security.CustomUserDetails;
import fr.enzogiardinelli.synkro.services.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<List<TaskResponse>> getProjectTasks(@PathVariable UUID projectId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(taskService.getProjectTasks(projectId, currentUser));
    }

    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponse> createTask(@PathVariable UUID projectId, @RequestBody @Valid TaskRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        TaskResponse created = taskService.createTask(projectId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> updateTaskDetails(@PathVariable UUID id, @RequestBody @Valid PatchTaskRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(taskService.updateTaskDetails(id, request, currentUser));
    }

    @PatchMapping("/tasks/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(@PathVariable UUID id, @RequestBody @Valid PatchTaskRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, request, currentUser));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        taskService.deleteTask(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tasks/{id}/assign")
    public ResponseEntity<TaskResponse> assignMembers(@PathVariable UUID id, @RequestBody @Valid AssigneesRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(taskService.assignMembers(id, request, currentUser));
    }
}