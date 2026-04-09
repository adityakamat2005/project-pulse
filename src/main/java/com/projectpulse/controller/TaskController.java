package com.projectpulse.controller;

import com.projectpulse.dto.TaskDto;
import com.projectpulse.entity.Project;
import com.projectpulse.entity.Task;
import com.projectpulse.entity.User;
import com.projectpulse.service.ProjectService;
import com.projectpulse.service.TaskService;
import com.projectpulse.service.UserService;
import com.projectpulse.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final UserService userService;
    private final SecurityUtils securityUtils;

    // ── List tasks for a project ────────────────────────────────────────────
    @GetMapping
    public String listTasks(
            @RequestParam Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String pinned,
            Model model) {

        User currentUser = securityUtils.getCurrentUser();
        Project project = projectService.findById(projectId);

        List<Task> tasks;
        if (status != null && !status.isBlank()) {
            tasks = taskService.findByProjectAndStatus(project, status);
        } else if (priority != null && !priority.isBlank()) {
            tasks = taskService.findByProjectAndPriority(project, priority);
        } else if ("true".equals(pinned)) {
            tasks = taskService.findPinnedTasks(project);
        } else {
            tasks = taskService.findByProject(project);
        }

        model.addAttribute("tasks", tasks);
        model.addAttribute("project", project);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("taskStatuses", Task.TaskStatus.values());
        model.addAttribute("priorities", Task.Priority.values());
        return "tasks/list";
    }

    // ── Task detail ─────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public String taskDetail(@PathVariable Long id, Model model) {
        User currentUser = securityUtils.getCurrentUser();
        Task task = taskService.findById(id);
        model.addAttribute("task", task);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("taskStatuses", Task.TaskStatus.values());
        return "tasks/detail";
    }

    // ── New task form ───────────────────────────────────────────────────────
    @GetMapping("/new")
    public String newTaskForm(@RequestParam Long projectId, Model model) {
        Project project = projectService.findById(projectId);
        User currentUser = securityUtils.getCurrentUser();

        TaskDto dto = new TaskDto();
        dto.setProjectId(projectId);

        model.addAttribute("taskDto", dto);
        model.addAttribute("project", project);
        model.addAttribute("members", project.getMembers());
        model.addAttribute("priorities", Task.Priority.values());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isNew", true);
        return "tasks/form";
    }

    // ── Create task ─────────────────────────────────────────────────────────
    @PostMapping("/new")
    public String createTask(
            @Valid @ModelAttribute("taskDto") TaskDto dto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            Project project = projectService.findById(dto.getProjectId());
            model.addAttribute("project", project);
            model.addAttribute("members", project.getMembers());
            model.addAttribute("priorities", Task.Priority.values());
            model.addAttribute("isNew", true);
            return "tasks/form";
        }

        User currentUser = securityUtils.getCurrentUser();
        Project project = projectService.findById(dto.getProjectId());
        Task created = taskService.createTask(dto, currentUser, project);
        redirectAttributes.addFlashAttribute("successMsg",
            "Task \"" + created.getTitle() + "\" created successfully!");
        return "redirect:/tasks?projectId=" + dto.getProjectId();
    }

    // ── Edit task form ──────────────────────────────────────────────────────
    @GetMapping("/{id}/edit")
    public String editTaskForm(@PathVariable Long id, Model model) {
        Task task = taskService.findById(id);
        User currentUser = securityUtils.getCurrentUser();

        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setDeadline(task.getDeadline());
        dto.setPriority(task.getPriority().name());
        dto.setPinned(task.isPinned());
        dto.setProjectId(task.getProject().getId());
        dto.setAssignedToId(task.getAssignedTo() != null ? task.getAssignedTo().getId() : null);

        model.addAttribute("taskDto", dto);
        model.addAttribute("task", task);
        model.addAttribute("project", task.getProject());
        model.addAttribute("members", task.getProject().getMembers());
        model.addAttribute("priorities", Task.Priority.values());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isNew", false);
        return "tasks/form";
    }

    // ── Update task ─────────────────────────────────────────────────────────
    @PostMapping("/{id}/edit")
    public String updateTask(
            @PathVariable Long id,
            @Valid @ModelAttribute("taskDto") TaskDto dto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            Task task = taskService.findById(id);
            model.addAttribute("task", task);
            model.addAttribute("project", task.getProject());
            model.addAttribute("members", task.getProject().getMembers());
            model.addAttribute("priorities", Task.Priority.values());
            model.addAttribute("isNew", false);
            return "tasks/form";
        }

        User currentUser = securityUtils.getCurrentUser();
        taskService.updateTask(id, dto, currentUser);
        redirectAttributes.addFlashAttribute("successMsg", "Task updated successfully!");
        return "redirect:/tasks/" + id;
    }

    // ── Update task status ──────────────────────────────────────────────────
    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) Long projectId,
            RedirectAttributes redirectAttributes) {
        User currentUser = securityUtils.getCurrentUser();
        taskService.updateStatus(id, status, currentUser);
        redirectAttributes.addFlashAttribute("successMsg", "Task status updated.");
        return projectId != null
            ? "redirect:/tasks?projectId=" + projectId
            : "redirect:/tasks/" + id;
    }

    // ── Toggle pin ──────────────────────────────────────────────────────────
    @PostMapping("/{id}/pin")
    public String togglePin(@PathVariable Long id,
                            @RequestParam(required = false) Long projectId,
                            RedirectAttributes redirectAttributes) {
        User currentUser = securityUtils.getCurrentUser();
        Task task = taskService.togglePin(id, currentUser);
        redirectAttributes.addFlashAttribute("successMsg",
            task.isPinned() ? "Task pinned!" : "Task unpinned.");
        return projectId != null
            ? "redirect:/tasks?projectId=" + projectId
            : "redirect:/tasks/" + id;
    }

    // ── Delete task ─────────────────────────────────────────────────────────
    @PostMapping("/{id}/delete")
    public String deleteTask(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        User currentUser = securityUtils.getCurrentUser();
        Task task = taskService.findById(id);
        Long projectId = task.getProject().getId();
        taskService.deleteTask(id, currentUser);
        redirectAttributes.addFlashAttribute("successMsg", "Task deleted.");
        return "redirect:/tasks?projectId=" + projectId;
    }

    // ── Approve / Reject (leader shortcut from task detail) ─────────────────
    @PostMapping("/{id}/approve")
    public String approveTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = securityUtils.getCurrentUser();
        taskService.approveTask(id, currentUser);
        redirectAttributes.addFlashAttribute("successMsg", "Task approved.");
        return "redirect:/tasks/" + id;
    }

    @PostMapping("/{id}/reject")
    public String rejectTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = securityUtils.getCurrentUser();
        taskService.rejectTask(id, currentUser);
        redirectAttributes.addFlashAttribute("successMsg", "Task rejected. Member can resubmit.");
        return "redirect:/tasks/" + id;
    }
}
