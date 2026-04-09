package com.projectpulse.controller;

import com.projectpulse.dto.ProjectDto;
import com.projectpulse.entity.Project;
import com.projectpulse.entity.User;
import com.projectpulse.service.ProjectService;
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
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String listProjects(Model model) {
        User currentUser = securityUtils.getCurrentUser();
        List<Project> projects = projectService.findProjectsForUser(currentUser);
        model.addAttribute("projects", projects);
        model.addAttribute("currentUser", currentUser);
        return "projects/list";
    }

    @GetMapping("/{id}")
    public String projectDetail(@PathVariable Long id, Model model) {
        User currentUser = securityUtils.getCurrentUser();
        Project project = projectService.findById(id);
        model.addAttribute("project", project);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("tasks", project.getTasks());
        return "projects/detail";
    }

    @GetMapping("/new")
    public String newProjectForm(Model model) {
        User currentUser = securityUtils.getCurrentUser();           // ← was missing
        model.addAttribute("currentUser", currentUser);             // ← was missing
        model.addAttribute("projectDto", new ProjectDto());
        model.addAttribute("leaders", userService.findByRole("ROLE_LEADER"));
        model.addAttribute("members", userService.findByRole("ROLE_MEMBER"));
        model.addAttribute("isNew", true);
        return "projects/form";
    }

    @PostMapping("/new")
    public String createProject(
            @Valid @ModelAttribute("projectDto") ProjectDto dto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            User currentUser = securityUtils.getCurrentUser();
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("leaders", userService.findByRole("ROLE_LEADER"));
            model.addAttribute("members", userService.findByRole("ROLE_MEMBER"));
            model.addAttribute("isNew", true);
            return "projects/form";
        }

        User currentUser = securityUtils.getCurrentUser();
        Project created = projectService.createProject(dto, currentUser);
        redirectAttributes.addFlashAttribute("successMsg",
                "Project \"" + created.getTitle() + "\" created successfully!");
        return "redirect:/projects/" + created.getId();
    }

    @GetMapping("/{id}/edit")
    public String editProjectForm(@PathVariable Long id, Model model) {
        User currentUser = securityUtils.getCurrentUser();
        Project project = projectService.findById(id);

        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setDeadline(project.getDeadline());
        dto.setLeaderId(project.getLeader() != null ? project.getLeader().getId() : null);
        dto.setStatus(project.getStatus().name());
        dto.setMemberIds(project.getMembers().stream().map(User::getId).toList());

        model.addAttribute("projectDto", dto);
        model.addAttribute("project", project);
        model.addAttribute("leaders", userService.findByRole("ROLE_LEADER"));
        model.addAttribute("members", userService.findByRole("ROLE_MEMBER"));
        model.addAttribute("statuses", Project.ProjectStatus.values());
        model.addAttribute("isNew", false);
        model.addAttribute("currentUser", currentUser);
        return "projects/form";
    }

    @PostMapping("/{id}/edit")
    public String updateProject(
            @PathVariable Long id,
            @Valid @ModelAttribute("projectDto") ProjectDto dto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            User currentUser = securityUtils.getCurrentUser();
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("leaders", userService.findByRole("ROLE_LEADER"));
            model.addAttribute("members", userService.findByRole("ROLE_MEMBER"));
            model.addAttribute("statuses", Project.ProjectStatus.values());
            model.addAttribute("isNew", false);
            return "projects/form";
        }

        User currentUser = securityUtils.getCurrentUser();
        projectService.updateProject(id, dto, currentUser);
        redirectAttributes.addFlashAttribute("successMsg", "Project updated successfully!");
        return "redirect:/projects/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = securityUtils.getCurrentUser();
        projectService.deleteProject(id, currentUser);
        redirectAttributes.addFlashAttribute("successMsg", "Project deleted.");
        return "redirect:/projects";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        User currentUser = securityUtils.getCurrentUser();
        projectService.updateStatus(id, status, currentUser);
        redirectAttributes.addFlashAttribute("successMsg", "Project status updated.");
        return "redirect:/projects/" + id;
    }
}