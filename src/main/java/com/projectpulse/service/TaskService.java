package com.projectpulse.service;

import com.projectpulse.dto.TaskDto;
import com.projectpulse.entity.Project;
import com.projectpulse.entity.Task;
import com.projectpulse.entity.User;
import com.projectpulse.exception.AccessDeniedException;
import com.projectpulse.exception.ResourceNotFoundException;
import com.projectpulse.repository.TaskRepository;
import com.projectpulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // --- Read ---

    @Transactional(readOnly = true)
    public Task findById(Long id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Task> findByProject(Project project) {
        return taskRepository.findByProjectOrderByDeadlineAsc(project);
    }

    @Transactional(readOnly = true)
    public List<Task> findByProjectAndStatus(Project project, String status) {
        return taskRepository.findByProjectAndStatus(project, Task.TaskStatus.valueOf(status));
    }

    @Transactional(readOnly = true)
    public List<Task> findByProjectAndPriority(Project project, String priority) {
        return taskRepository.findByProjectAndPriority(project, Task.Priority.valueOf(priority));
    }

    @Transactional(readOnly = true)
    public List<Task> findPinnedTasks(Project project) {
        return taskRepository.findByProjectAndPinned(project, true);
    }

    @Transactional(readOnly = true)
    public List<Task> findActiveTasksForUser(User user) {
        return taskRepository.findActiveTasksByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Task> findTasksForCalendar(User user, LocalDate start, LocalDate end) {
        if (user.hasRole("ROLE_MEMBER")) {
            return taskRepository.findTasksInDateRangeForUser(start, end, user);
        }
        return taskRepository.findAll(); // leaders/admins see all (filtered in controller)
    }

    @Transactional(readOnly = true)
    public List<Task> findAssignedToUser(User user) {
        return taskRepository.findByAssignedTo(user);
    }

    // --- Create ---

    public Task createTask(TaskDto dto, User createdBy, Project project) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDeadline(dto.getDeadline());
        task.setPriority(Task.Priority.valueOf(dto.getPriority()));
        task.setPinned(dto.isPinned());
        task.setProject(project);
        task.setCreatedBy(createdBy);

        if (dto.getAssignedToId() != null) {
            User assignee = userRepository.findById(dto.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            task.setAssignedTo(assignee);
        }

        return taskRepository.save(task);
    }

    // --- Update ---

    public Task updateTask(Long id, TaskDto dto, User currentUser) {
        Task task = findById(id);
        checkLeaderOrAdmin(task, currentUser);

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDeadline(dto.getDeadline());
        task.setPriority(Task.Priority.valueOf(dto.getPriority()));
        task.setPinned(dto.isPinned());

        if (dto.getAssignedToId() != null) {
            User assignee = userRepository.findById(dto.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            task.setAssignedTo(assignee);
        }

        return taskRepository.save(task);
    }

    public Task updateStatus(Long id, String status, User currentUser) {
        Task task = findById(id);

        // Members can only move tasks to IN_PROGRESS or SUBMITTED
        if (currentUser.hasRole("ROLE_MEMBER")) {
            Task.TaskStatus newStatus = Task.TaskStatus.valueOf(status);
            if (newStatus != Task.TaskStatus.IN_PROGRESS && newStatus != Task.TaskStatus.SUBMITTED) {
                throw new AccessDeniedException("Members can only set status to IN_PROGRESS or SUBMITTED");
            }
            if (!task.getAssignedTo().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You can only update tasks assigned to you");
            }
        }

        task.setStatus(Task.TaskStatus.valueOf(status));
        return taskRepository.save(task);
    }

    public Task togglePin(Long id, User currentUser) {
        Task task = findById(id);
        checkLeaderOrAdmin(task, currentUser);
        task.setPinned(!task.isPinned());
        return taskRepository.save(task);
    }

    public Task approveTask(Long id, User reviewer) {
        Task task = findById(id);
        checkLeaderOrAdmin(task, reviewer);
        task.setStatus(Task.TaskStatus.APPROVED);
        return taskRepository.save(task);
    }

    public Task rejectTask(Long id, User reviewer) {
        Task task = findById(id);
        checkLeaderOrAdmin(task, reviewer);
        task.setStatus(Task.TaskStatus.REJECTED);
        return taskRepository.save(task);
    }

    // --- Delete ---

    public void deleteTask(Long id, User currentUser) {
        Task task = findById(id);
        checkLeaderOrAdmin(task, currentUser);
        taskRepository.delete(task);
    }

    // --- Stats ---

    @Transactional(readOnly = true)
    public long countAll() {
        return taskRepository.count();
    }

    @Transactional(readOnly = true)
    public long countCompleted() {
        return taskRepository.countCompletedTasks();
    }

    @Transactional(readOnly = true)
    public long countPendingApprovals() {
        return taskRepository.countPendingApprovals();
    }

    @Transactional(readOnly = true)
    public long countSubmittedByUser(User user) {
        return taskRepository.countSubmittedByUser(user);
    }

    // --- Helpers ---

    private void checkLeaderOrAdmin(Task task, User user) {
        boolean isAdmin = user.hasRole("ROLE_ADMIN");
        boolean isLeader = task.getProject().getLeader() != null &&
                           task.getProject().getLeader().getId().equals(user.getId());
        if (!isAdmin && !isLeader) {
            throw new AccessDeniedException("You do not have permission to modify this task");
        }
    }
}
