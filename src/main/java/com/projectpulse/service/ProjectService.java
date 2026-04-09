package com.projectpulse.service;

import com.projectpulse.dto.ProjectDto;
import com.projectpulse.entity.Project;
import com.projectpulse.entity.User;
import com.projectpulse.exception.AccessDeniedException;
import com.projectpulse.exception.ResourceNotFoundException;
import com.projectpulse.repository.ProjectRepository;
import com.projectpulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // --- Read ---

    @Transactional(readOnly = true)
    public Project findById(Long id) {
        return projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Project> findProjectsForUser(User user) {
        if (user.hasRole("ROLE_ADMIN")) {
            return projectRepository.findAll();
        }
        return projectRepository.findByLeaderOrMember(user);
    }

    @Transactional(readOnly = true)
    public List<Project> findByLeader(User leader) {
        return projectRepository.findByLeader(leader);
    }

    // --- Create ---

    public Project createProject(ProjectDto dto, User createdBy) {
        User leader = userRepository.findById(dto.getLeaderId())
            .orElseThrow(() -> new ResourceNotFoundException("Leader not found"));

        Project project = new Project();
        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setDeadline(dto.getDeadline());
        project.setCreatedBy(createdBy);
        project.setLeader(leader);

        if (dto.getMemberIds() != null && !dto.getMemberIds().isEmpty()) {
            Set<User> members = new HashSet<>(userRepository.findAllById(dto.getMemberIds()));
            project.setMembers(members);
        }

        return projectRepository.save(project);
    }

    // --- Update ---

    public Project updateProject(Long id, ProjectDto dto, User currentUser) {
        Project project = findById(id);
        checkLeaderOrAdmin(project, currentUser);

        User leader = userRepository.findById(dto.getLeaderId())
            .orElseThrow(() -> new ResourceNotFoundException("Leader not found"));

        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setDeadline(dto.getDeadline());
        project.setLeader(leader);

        if (dto.getMemberIds() != null) {
            Set<User> members = new HashSet<>(userRepository.findAllById(dto.getMemberIds()));
            project.setMembers(members);
        }

        if (dto.getStatus() != null) {
            project.setStatus(Project.ProjectStatus.valueOf(dto.getStatus()));
        }

        return projectRepository.save(project);
    }

    public Project updateStatus(Long id, String status, User currentUser) {
        Project project = findById(id);
        checkLeaderOrAdmin(project, currentUser);
        project.setStatus(Project.ProjectStatus.valueOf(status));
        return projectRepository.save(project);
    }

    // --- Delete ---

    public void deleteProject(Long id, User currentUser) {
        Project project = findById(id);
        if (!currentUser.hasRole("ROLE_ADMIN")) {
            throw new AccessDeniedException("Only admins can delete projects");
        }
        projectRepository.delete(project);
    }

    // --- Stats ---

    @Transactional(readOnly = true)
    public long countAll() {
        return projectRepository.count();
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return projectRepository.countActiveProjects();
    }

    // --- Helpers ---

    private void checkLeaderOrAdmin(Project project, User user) {
        boolean isAdmin = user.hasRole("ROLE_ADMIN");
        boolean isLeader = project.getLeader() != null &&
                           project.getLeader().getId().equals(user.getId());
        if (!isAdmin && !isLeader) {
            throw new AccessDeniedException("You do not have permission to modify this project");
        }
    }
}
