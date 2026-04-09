package com.projectpulse.repository;

import com.projectpulse.entity.Project;
import com.projectpulse.entity.Task;
import com.projectpulse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.project LEFT JOIN FETCH t.assignedTo LEFT JOIN FETCH t.createdBy WHERE t.project = :project ORDER BY t.deadline ASC")
    List<Task> findByProjectOrderByDeadlineAsc(Project project);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.project LEFT JOIN FETCH t.assignedTo WHERE t.project = :project AND t.status = :status")
    List<Task> findByProjectAndStatus(Project project, Task.TaskStatus status);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.project LEFT JOIN FETCH t.assignedTo WHERE t.project = :project AND t.priority = :priority")
    List<Task> findByProjectAndPriority(Project project, Task.Priority priority);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.project LEFT JOIN FETCH t.assignedTo WHERE t.project = :project AND t.pinned = :pinned")
    List<Task> findByProjectAndPinned(Project project, boolean pinned);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.project p LEFT JOIN FETCH t.assignedTo WHERE t.assignedTo = :user AND t.status NOT IN ('APPROVED')")
    List<Task> findActiveTasksByUser(User user);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.project LEFT JOIN FETCH t.assignedTo WHERE t.assignedTo = :user")
    List<Task> findByAssignedTo(User user);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.project WHERE t.project = :project")
    List<Task> findByProject(Project project);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.project WHERE t.deadline BETWEEN :start AND :end AND t.assignedTo = :user")
    List<Task> findTasksInDateRangeForUser(LocalDate start, LocalDate end, User user);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.project WHERE t.deadline BETWEEN :start AND :end AND t.project = :project")
    List<Task> findTasksInDateRangeForProject(LocalDate start, LocalDate end, Project project);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = 'APPROVED'")
    long countCompletedTasks();

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = 'SUBMITTED'")
    long countPendingApprovals();

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo = :user AND t.status = 'SUBMITTED'")
    long countSubmittedByUser(User user);
}