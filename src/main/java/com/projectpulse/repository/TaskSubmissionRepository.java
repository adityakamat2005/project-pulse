package com.projectpulse.repository;

import com.projectpulse.entity.Task;
import com.projectpulse.entity.TaskSubmission;
import com.projectpulse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, Long> {

    @Query("SELECT ts FROM TaskSubmission ts LEFT JOIN FETCH ts.task t LEFT JOIN FETCH t.project LEFT JOIN FETCH ts.submittedBy WHERE ts.task = :task ORDER BY ts.submittedAt DESC")
    List<TaskSubmission> findByTaskOrderBySubmittedAtDesc(Task task);

    @Query("SELECT ts FROM TaskSubmission ts LEFT JOIN FETCH ts.task t LEFT JOIN FETCH t.project LEFT JOIN FETCH ts.submittedBy WHERE ts.submittedBy = :user")
    List<TaskSubmission> findBySubmittedBy(User user);

    @Query("SELECT ts FROM TaskSubmission ts LEFT JOIN FETCH ts.task t LEFT JOIN FETCH t.project LEFT JOIN FETCH ts.submittedBy WHERE t.project.leader = :leader AND ts.status = 'PENDING'")
    List<TaskSubmission> findPendingSubmissionsForLeader(User leader);

    @Query("SELECT COUNT(ts) FROM TaskSubmission ts WHERE ts.status = 'PENDING' AND ts.task.project.leader = :leader")
    long countPendingForLeader(User leader);
}