package com.projectpulse.controller;

import com.projectpulse.entity.Task;
import com.projectpulse.entity.TaskSubmission;
import com.projectpulse.entity.User;
import com.projectpulse.service.FileStorageService;
import com.projectpulse.service.SubmissionService;
import com.projectpulse.service.TaskService;
import com.projectpulse.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

@Controller
@RequestMapping("/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final TaskService taskService;
    private final SecurityUtils securityUtils;
    private final FileStorageService fileStorageService;

    // ── All pending submissions for leader ──────────────────────────────────
    @GetMapping
    public String listPendingSubmissions(Model model) {
        User currentUser = securityUtils.getCurrentUser();
        List<TaskSubmission> pending = submissionService.findPendingForLeader(currentUser);
        model.addAttribute("submissions", pending);
        model.addAttribute("currentUser", currentUser);
        return "submissions/list";
    }

    // ── Submissions for a specific task ─────────────────────────────────────
    @GetMapping("/task/{taskId}")
    public String submissionsForTask(@PathVariable Long taskId, Model model) {
        User currentUser = securityUtils.getCurrentUser();
        Task task = taskService.findById(taskId);
        List<TaskSubmission> submissions = submissionService.findByTask(task);
        model.addAttribute("task", task);
        model.addAttribute("submissions", submissions);
        model.addAttribute("currentUser", currentUser);
        return "submissions/task-submissions";
    }

    // ── Upload form ─────────────────────────────────────────────────────────
    @GetMapping("/upload/{taskId}")
    public String uploadForm(@PathVariable Long taskId, Model model) {
        User currentUser = securityUtils.getCurrentUser();
        Task task = taskService.findById(taskId);
        model.addAttribute("task", task);
        model.addAttribute("currentUser", currentUser);
        return "submissions/upload";
    }

    // ── Handle upload ───────────────────────────────────────────────────────
    @PostMapping("/upload/{taskId}")
    public String handleUpload(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "notes", required = false) String notes,
            RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Please select a file to upload.");
            return "redirect:/submissions/upload/" + taskId;
        }

        User currentUser = securityUtils.getCurrentUser();
        submissionService.submitFile(taskId, file, notes, currentUser);
        redirectAttributes.addFlashAttribute("successMsg",
            "File submitted successfully! Waiting for leader review.");
        return "redirect:/tasks/" + taskId;
    }

    // ── Review page ─────────────────────────────────────────────────────────
    @GetMapping("/review/{submissionId}")
    public String reviewPage(@PathVariable Long submissionId, Model model) {
        User currentUser = securityUtils.getCurrentUser();
        TaskSubmission submission = submissionService.findById(submissionId);
        model.addAttribute("submission", submission);
        model.addAttribute("currentUser", currentUser);
        return "submissions/review";
    }

    // ── Approve submission ───────────────────────────────────────────────────
    @PostMapping("/review/{submissionId}/approve")
    public String approveSubmission(
            @PathVariable Long submissionId,
            @RequestParam(value = "reviewNotes", required = false) String reviewNotes,
            RedirectAttributes redirectAttributes) {

        User currentUser = securityUtils.getCurrentUser();
        submissionService.approveSubmission(submissionId, reviewNotes, currentUser);
        redirectAttributes.addFlashAttribute("successMsg", "Submission approved! Task marked as complete.");
        return "redirect:/submissions";
    }

    // ── Reject submission ────────────────────────────────────────────────────
    @PostMapping("/review/{submissionId}/reject")
    public String rejectSubmission(
            @PathVariable Long submissionId,
            @RequestParam(value = "reviewNotes", required = false) String reviewNotes,
            RedirectAttributes redirectAttributes) {

        User currentUser = securityUtils.getCurrentUser();
        submissionService.rejectSubmission(submissionId, reviewNotes, currentUser);
        redirectAttributes.addFlashAttribute("errorMsg",
            "Submission rejected. Member has been notified to resubmit.");
        return "redirect:/submissions";
    }

    // ── Download file ────────────────────────────────────────────────────────
    @GetMapping("/download/{submissionId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long submissionId) {
        TaskSubmission submission = submissionService.findById(submissionId);

        try {
            // Use FileStorageService to resolve path safely
            Path filePath = fileStorageService.load(submission.getFileName());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + submission.getOriginalFileName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, submission.getFileType())
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
