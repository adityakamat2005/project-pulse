package com.projectpulse.service;

import com.projectpulse.entity.Task;
import com.projectpulse.entity.TaskSubmission;
import com.projectpulse.entity.User;
import com.projectpulse.exception.AccessDeniedException;
import com.projectpulse.exception.ResourceNotFoundException;
import com.projectpulse.repository.TaskSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubmissionService {

    private final TaskSubmissionRepository submissionRepository;
    private final FileStorageService fileStorageService;
    private final TaskService taskService;

    // --- Read ---

    @Transactional(readOnly = true)
    public TaskSubmission findById(Long id) {
        return submissionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<TaskSubmission> findByTask(Task task) {
        return submissionRepository.findByTaskOrderBySubmittedAtDesc(task);
    }

    @Transactional(readOnly = true)
    public List<TaskSubmission> findPendingForLeader(User leader) {
        return submissionRepository.findPendingSubmissionsForLeader(leader);
    }

    @Transactional(readOnly = true)
    public long countPendingForLeader(User leader) {
        return submissionRepository.countPendingForLeader(leader);
    }

    // --- Create ---

    public TaskSubmission submitFile(Long taskId, MultipartFile file, String notes, User submittedBy) {
        Task task = taskService.findById(taskId);

        // Validate: only assigned member can submit
        if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(submittedBy.getId())) {
            throw new AccessDeniedException("You are not assigned to this task");
        }

        // Store the file
        String storedFilename = fileStorageService.store(file);

        TaskSubmission submission = new TaskSubmission();
        submission.setTask(task);
        submission.setSubmittedBy(submittedBy);
        submission.setNotes(notes);
        submission.setFileName(storedFilename);
        submission.setOriginalFileName(file.getOriginalFilename());
        submission.setFilePath(fileStorageService.getFileUrl(storedFilename));
        submission.setFileType(getContentType(file));
        submission.setFileSize(file.getSize());
        submission.setStatus(TaskSubmission.SubmissionStatus.PENDING);

        // Auto-update task status to SUBMITTED
        task.setStatus(Task.TaskStatus.SUBMITTED);

        TaskSubmission saved = submissionRepository.save(submission);
        log.info("📎 Submission created for task {} by {}", taskId, submittedBy.getUsername());
        return saved;
    }

    // --- Review ---

    public TaskSubmission approveSubmission(Long submissionId, String reviewNotes, User reviewer) {
        TaskSubmission submission = findById(submissionId);
        checkReviewPermission(submission, reviewer);

        submission.setStatus(TaskSubmission.SubmissionStatus.APPROVED);
        submission.setReviewNotes(reviewNotes);
        submission.setReviewedBy(reviewer);
        submission.setReviewedAt(LocalDateTime.now());

        // Update task status to APPROVED
        Task task = submission.getTask();
        task.setStatus(Task.TaskStatus.APPROVED);

        return submissionRepository.save(submission);
    }

    public TaskSubmission rejectSubmission(Long submissionId, String reviewNotes, User reviewer) {
        TaskSubmission submission = findById(submissionId);
        checkReviewPermission(submission, reviewer);

        submission.setStatus(TaskSubmission.SubmissionStatus.REJECTED);
        submission.setReviewNotes(reviewNotes);
        submission.setReviewedBy(reviewer);
        submission.setReviewedAt(LocalDateTime.now());

        // Revert task status so member can resubmit
        Task task = submission.getTask();
        task.setStatus(Task.TaskStatus.REJECTED);

        return submissionRepository.save(submission);
    }

    // --- Helpers ---

    private void checkReviewPermission(TaskSubmission submission, User reviewer) {
        boolean isAdmin = reviewer.hasRole("ROLE_ADMIN");
        boolean isLeader = submission.getTask().getProject().getLeader() != null &&
                           submission.getTask().getProject().getLeader().getId().equals(reviewer.getId());
        if (!isAdmin && !isLeader) {
            throw new AccessDeniedException("Only the project leader or admin can review submissions");
        }
    }

    private String getContentType(MultipartFile file) {
        String contentType = file.getContentType();
        return (contentType != null) ? contentType : "application/octet-stream";
    }
}
