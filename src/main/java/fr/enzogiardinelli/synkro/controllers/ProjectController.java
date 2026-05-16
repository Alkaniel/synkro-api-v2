package fr.enzogiardinelli.synkro.controllers;

import fr.enzogiardinelli.synkro.dtos.projects.ProjectMemberRequest;
import fr.enzogiardinelli.synkro.dtos.projects.ProjectRequest;
import fr.enzogiardinelli.synkro.dtos.projects.TransferOwnershipRequest;
import fr.enzogiardinelli.synkro.dtos.projects.response.ParticipantResponse;
import fr.enzogiardinelli.synkro.dtos.projects.response.ProjectDetailResponse;
import fr.enzogiardinelli.synkro.dtos.projects.response.ProjectResponse;
import fr.enzogiardinelli.synkro.entities.projects.Project;
import fr.enzogiardinelli.synkro.entities.projects.ProjectParticipant;
import fr.enzogiardinelli.synkro.entities.projects.ProjectRole;
import fr.enzogiardinelli.synkro.entities.users.User;
import fr.enzogiardinelli.synkro.repositories.ProjectRepository;
import fr.enzogiardinelli.synkro.repositories.UserRepository;
import fr.enzogiardinelli.synkro.security.CustomUserDetails;
import fr.enzogiardinelli.synkro.services.FileStorageService;
import fr.enzogiardinelli.synkro.services.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getMyProjects(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(projectService.getMyProjects(currentUser));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @RequestBody @Valid ProjectRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        ProjectResponse created = projectService.createProject(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDetailResponse> getProjectDetails(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(projectService.getProjectDetails(id, currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID id,
            @RequestBody @Valid ProjectRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(projectService.updateProject(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        projectService.deleteProject(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/transfer")
    public ResponseEntity<ProjectDetailResponse> transferOwnership(
            @PathVariable UUID id,
            @RequestBody @Valid TransferOwnershipRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(projectService.transferOwnership(id, request, currentUser));
    }

    @PostMapping("/{id}/avatar")
    public ResponseEntity<ProjectResponse> uploadAvatar(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(projectService.uploadAvatar(id, file, currentUser));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<ParticipantResponse>> getProjectMembers(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(projectService.getProjectMembers(id, currentUser));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ParticipantResponse> addMember(
            @PathVariable UUID id,
            @RequestBody @Valid ProjectMemberRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        ParticipantResponse added = projectService.addMember(id, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(added);
    }

    @PatchMapping("/{id}/members")
    public ResponseEntity<ParticipantResponse> updateMemberRole(
            @PathVariable UUID id,
            @RequestBody @Valid ProjectMemberRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(projectService.updateMemberRole(id, request, currentUser));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        projectService.removeMember(id, userId, currentUser);
        return ResponseEntity.noContent().build();
    }
}