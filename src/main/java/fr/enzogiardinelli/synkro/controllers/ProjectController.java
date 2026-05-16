package fr.enzogiardinelli.synkro.controllers;

import fr.enzogiardinelli.synkro.dtos.projects.ProjectMemberRequest;
import fr.enzogiardinelli.synkro.dtos.projects.ProjectRequest;
import fr.enzogiardinelli.synkro.dtos.projects.TransferOwnershipRequest;
import fr.enzogiardinelli.synkro.entities.projects.Project;
import fr.enzogiardinelli.synkro.entities.projects.ProjectParticipant;
import fr.enzogiardinelli.synkro.entities.projects.ProjectRole;
import fr.enzogiardinelli.synkro.entities.users.User;
import fr.enzogiardinelli.synkro.repositories.ProjectRepository;
import fr.enzogiardinelli.synkro.repositories.UserRepository;
import fr.enzogiardinelli.synkro.security.CustomUserDetails;
import fr.enzogiardinelli.synkro.services.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectRepository projectRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    public ProjectController(ProjectRepository projectRepository, FileStorageService fileStorageService, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    private boolean isGlobalAdmin(CustomUserDetails currentUser) {
        return currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    @GetMapping
    public List<Project> getMyProjects(@AuthenticationPrincipal CustomUserDetails currentUser) {
        if (isGlobalAdmin(currentUser)) {
            return projectRepository.findAll();
        }
        return projectRepository.findProjetsForUser(currentUser.getUser());
    }

    @PostMapping
    public String createProject(@RequestBody @Valid ProjectRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(currentUser.getUser());

        ProjectParticipant ownerParticipant = new ProjectParticipant();
        ownerParticipant.setProject(project);
        ownerParticipant.setUser(currentUser.getUser());
        ownerParticipant.setProjectRole(ProjectRole.OWNER);

        project.getParticipants().add(ownerParticipant);
        projectRepository.save(project);

        return "Project '" + project.getName() + "' created successfully !";
    }

    @GetMapping("/{id}")
    public Project getProjectDetails(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));

        if (isGlobalAdmin(currentUser)) return project;

        boolean isMember = project.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(currentUser.getUser().getId()));

        if (!isMember) throw new RuntimeException("Unauthorized");

        return project;
    }

    @DeleteMapping("/{id}")
    public String deleteProject(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOwner().getId().equals(currentUser.getUser().getId()) && !isGlobalAdmin(currentUser)) {
            throw new RuntimeException("Unauthorized");
        }

        projectRepository.delete(project);
        return "Project deleted successfully !";
    }

    @GetMapping("/{id}/members")
    public List<ProjectParticipant> getProjectMembers(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));

        if (!isGlobalAdmin(currentUser)) {
            boolean isMember = project.getParticipants().stream()
                    .anyMatch(p -> p.getUser().getId().equals(currentUser.getUser().getId()));

            if (!isMember) throw new RuntimeException("Unauthorized : You must be a member to see the team");
        }

        return project.getParticipants().stream().toList();
    }

    @PostMapping("/{id}/members")
    public String addMember(@PathVariable UUID id, @RequestBody @Valid ProjectMemberRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));

        if (!isGlobalAdmin(currentUser)) {
            ProjectParticipant currentParticipant = project.getParticipants().stream()
                    .filter(p -> p.getUser().getId().equals(currentUser.getUser().getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Unauthorized"));

            if (currentParticipant.getProjectRole() != ProjectRole.OWNER && currentParticipant.getProjectRole() != ProjectRole.EDITOR) {
                throw new RuntimeException("Unauthorized : Only OWNER or EDITOR can add members");
            }
        }

        User userToAdd = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));

        ProjectParticipant newParticipant = new ProjectParticipant();
        newParticipant.setProject(project);
        newParticipant.setUser(userToAdd);
        newParticipant.setProjectRole(ProjectRole.valueOf(request.getRole().toUpperCase()));

        project.getParticipants().add(newParticipant);
        projectRepository.save(project);

        return "User added successfully !";
    }

    @PatchMapping("/{id}/members")
    public String updateMemberRole(@PathVariable UUID id, @RequestBody @Valid ProjectMemberRequest request, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));

        if (!isGlobalAdmin(currentUser)) {
            ProjectParticipant currentParticipant = project.getParticipants().stream()
                    .filter(p -> p.getUser().getId().equals(currentUser.getUser().getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Unauthorized"));

            if (currentParticipant.getProjectRole() != ProjectRole.OWNER && currentParticipant.getProjectRole() != ProjectRole.EDITOR) {
                throw new RuntimeException("Unauthorized : Only OWNER or EDITOR can change roles");
            }
        }

        ProjectParticipant participantToUpdate = project.getParticipants().stream()
                .filter(p -> p.getUser().getEmail().equals(request.getEmail()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Member not found in this project"));

        if (participantToUpdate.getProjectRole() == ProjectRole.OWNER) {
            throw new RuntimeException("Cannot change the role of the Project Owner");
        }

        participantToUpdate.setProjectRole(ProjectRole.valueOf(request.getRole().toUpperCase()));
        projectRepository.save(project);

        return "Role of " + participantToUpdate.getUser().getUsername() + " updated successfully to " + request.getRole();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public String removeMember(@PathVariable UUID id, @PathVariable UUID userId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));

        if (!isGlobalAdmin(currentUser)) {
            ProjectParticipant currentParticipant = project.getParticipants().stream()
                    .filter(p -> p.getUser().getId().equals(currentUser.getUser().getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Unauthorized"));

            if (currentParticipant.getProjectRole() != ProjectRole.OWNER && currentParticipant.getProjectRole() != ProjectRole.EDITOR) {
                throw new RuntimeException("Unauthorized");
            }
        }

        project.getParticipants().removeIf(p -> p.getUser().getId().equals(userId) && p.getProjectRole() != ProjectRole.OWNER);
        projectRepository.save(project);

        return "Member removed successfully !";
    }
}