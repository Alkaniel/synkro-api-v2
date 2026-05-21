package fr.enzogiardinelli.synkro.services;

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
import fr.enzogiardinelli.synkro.exceptions.ConflictException;
import fr.enzogiardinelli.synkro.exceptions.ResourceNotFoundException;
import fr.enzogiardinelli.synkro.exceptions.UnauthorizedAccessException;
import fr.enzogiardinelli.synkro.repositories.ProjectRepository;
import fr.enzogiardinelli.synkro.repositories.UserRepository;
import fr.enzogiardinelli.synkro.security.CustomUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Business logic for projects : CRUD + members + avatars + transfer
 * Every methods checks for rights.
 *
 * Conventions :
 * - getXxx() -> Read, must be at least a project member (or admin)
 * - createXxx()/updateXxx()/deleteXxx() -> write , must be OWNER or EDITOR.
 * - ROLE_ADMIN global role bypass any checks.
 */
@Service
public class ProjectService {

    // Avatar max size and allowed types
    private static final long MAX_AVATAR_SIZE = 5L * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_AVATAR_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, FileStorageService fileStorageService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    public boolean isGlobalAdmin(CustomUserDetails currentUser) {
        return currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Return user role in the project
     * If ADMIN -> OWNER (bypass).
     * If not a member -> UnauthorizedAccessException
     */
    public ProjectRole getUserRoleInProject(Project project, CustomUserDetails currentUser) {
        if (isGlobalAdmin(currentUser)) {
            return ProjectRole.OWNER;
        }
        return project.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(currentUser.getUser().getId()))
                .map(ProjectParticipant::getProjectRole)
                .findFirst()
                .orElseThrow(() -> new UnauthorizedAccessException("You are not a member of this project"));
    }

    private void requireRole(Project project, CustomUserDetails currentUser, ProjectRole... allowedRoles) {
        ProjectRole role = getUserRoleInProject(project, currentUser);
        for (ProjectRole allowed : allowedRoles) {
            if (role == allowed) return;
        }
        throw new UnauthorizedAccessException("Required role: " + List.of(allowedRoles) + ", but your are " + role);
    }

    private Project findProjectOrThrow(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    public List<ProjectResponse> getMyProjects(CustomUserDetails currentUser) {
        List<Project> projects = isGlobalAdmin(currentUser)
                ? projectRepository.findAll()
                : projectRepository.findProjetsForUser(currentUser.getUser());

        return projects.stream().map(ProjectResponse::from).toList();
    }

    public ProjectDetailResponse getProjectDetails(UUID id, CustomUserDetails currentUser) {
        Project project = findProjectOrThrow(id);
        getUserRoleInProject(project, currentUser);
        return ProjectDetailResponse.from(project);
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request, CustomUserDetails currentUser) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(currentUser.getUser());

        ProjectParticipant owner = new ProjectParticipant();
        owner.setProject(project);
        owner.setUser(currentUser.getUser());
        owner.setProjectRole(ProjectRole.OWNER);

        project.getParticipants().add(owner);
        Project saved = projectRepository.save(project);

        return ProjectResponse.from(saved);
    }

    @Transactional
    public ProjectResponse updateProject(UUID id, ProjectRequest request, CustomUserDetails currentUser) {
        Project project = findProjectOrThrow(id);
        requireRole(project, currentUser, ProjectRole.OWNER, ProjectRole.EDITOR);

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        Project saved = projectRepository.save(project);

        return ProjectResponse.from(saved);
    }

    @Transactional
    public void deleteProject(UUID id, CustomUserDetails currentUser) {
        Project project = findProjectOrThrow(id);

        if (!isGlobalAdmin(currentUser) && !project.getOwner().getId().equals(currentUser.getUser().getId())) {
            throw new UnauthorizedAccessException("Only the project OWNER can delete the project");
        }

        if (project.getAvatarUrl() != null) {
            fileStorageService.deleteFile(project.getAvatarUrl());
        }

        projectRepository.delete(project);
    }

    @Transactional
    public ProjectDetailResponse transferOwnership(UUID id, TransferOwnershipRequest request, CustomUserDetails currentUser) {
        Project project = findProjectOrThrow(id);

        if (!isGlobalAdmin(currentUser) && !project.getOwner().getId().equals(currentUser.getUser().getId())) {
            throw new UnauthorizedAccessException("Only the OWNER can transfer ownership");
        }

        User newOwner = userRepository.findByEmail(request.getNewOwnerEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getNewOwnerEmail()));

        if (newOwner.getId().equals(project.getOwner().getId())) {
            throw new ConflictException("This user is already the project owner");
        }

        User previousOwner = project.getOwner();

        ProjectParticipant previousOwnerParticipant = project.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(previousOwner.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Participant (previous owner)", previousOwner.getId()));
        previousOwnerParticipant.setProjectRole(ProjectRole.EDITOR);

        ProjectParticipant newOwnerParticipant = project.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(newOwner.getId()))
                .findFirst()
                .orElse(null);

        if (newOwnerParticipant == null) {
            newOwnerParticipant = new ProjectParticipant();
            newOwnerParticipant.setProject(project);
            newOwnerParticipant.setUser(newOwner);
            project.getParticipants().add(newOwnerParticipant);
        }
        newOwnerParticipant.setProjectRole(ProjectRole.OWNER);

        project.setOwner(newOwner);
        Project saved = projectRepository.save(project);
        return ProjectDetailResponse.from(saved);
    }

    @Transactional
    public ProjectResponse uploadAvatar(UUID id, MultipartFile file, CustomUserDetails currentUser) {
        Project project = findProjectOrThrow(id);
        requireRole(project, currentUser, ProjectRole.OWNER, ProjectRole.EDITOR);

        if (file == null || file.isEmpty()) {
            throw new ConflictException("Avatar file is empty");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new ConflictException("Avatar must be at most 5 MB");
        }
        if (!ALLOWED_AVATAR_TYPES.contains(file.getContentType())) {
            throw new ConflictException(
                    "Avatar must be one of: " + ALLOWED_AVATAR_TYPES
            );
        }

        if (project.getAvatarUrl() != null) {
            fileStorageService.deleteFile(project.getAvatarUrl());
        }

        String url = fileStorageService.storeFile(file, "project-" + project.getId() + "-avatar");
        project.setAvatarUrl(url);
        Project saved = projectRepository.save(project);
        return ProjectResponse.from(saved);
    }

    public List<ParticipantResponse> getProjectMembers(UUID id, CustomUserDetails currentUser) {
        Project project = findProjectOrThrow(id);
        getUserRoleInProject(project, currentUser);

        return project.getParticipants().stream()
                .map(ParticipantResponse::from)
                .toList();
    }

    @Transactional
    public ParticipantResponse addMember(UUID id, ProjectMemberRequest request, CustomUserDetails currentUser) {
        Project project = findProjectOrThrow(id);
        requireRole(project, currentUser, ProjectRole.OWNER, ProjectRole.EDITOR);

        User userToAdd = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getEmail()));

        boolean alreadyMember = project.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(userToAdd.getId()));

        if (alreadyMember) {
            throw new ConflictException("User is already a member of this project");
        }

        ProjectRole roleToAssign = ProjectRole.valueOf(request.getRole().toUpperCase());
        if (roleToAssign == ProjectRole.OWNER) {
            throw new ConflictException("Cannot assign OWNER role directly. Use transfer ownership.");
        }

        ProjectParticipant newParticipant = new ProjectParticipant();
        newParticipant.setProject(project);
        newParticipant.setUser(userToAdd);
        newParticipant.setProjectRole(roleToAssign);

        project.getParticipants().add(newParticipant);
        projectRepository.save(project);
        return ParticipantResponse.from(newParticipant);
    }

    @Transactional
    public ParticipantResponse updateMemberRole(UUID id, ProjectMemberRequest request, CustomUserDetails currentUser) {
        Project project = findProjectOrThrow(id);
        requireRole(project, currentUser, ProjectRole.OWNER, ProjectRole.EDITOR);

        ProjectParticipant participant = project.getParticipants().stream()
                .filter(p -> p.getUser().getEmail().equals(request.getEmail()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Member", request.getEmail()));

        if (participant.getProjectRole() == ProjectRole.OWNER) {
            throw new ConflictException("Cannot change the role of the project OWNER");
        }

        ProjectRole newRole = ProjectRole.valueOf(request.getRole().toUpperCase());
        if (newRole == ProjectRole.OWNER) {
            throw new ConflictException("Cannot promote a member to OWNER directly. Use transfer ownership.");
        }

        participant.setProjectRole(newRole);
        projectRepository.save(project);
        return ParticipantResponse.from(participant);
    }

    @Transactional
    public void removeMember(UUID id, UUID userId, CustomUserDetails currentUser) {
        Project project = findProjectOrThrow(id);
        requireRole(project, currentUser, ProjectRole.OWNER, ProjectRole.EDITOR);

        ProjectParticipant target = project.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Member", userId));

        if (target.getProjectRole() == ProjectRole.OWNER) {
            throw new ConflictException("Cannot remove the project OWNER. Transfer ownership first.");
        }

        project.getParticipants().remove(target);
        projectRepository.save(project);
    }
}
