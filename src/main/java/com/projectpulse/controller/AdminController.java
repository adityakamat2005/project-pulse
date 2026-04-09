package com.projectpulse.controller;

import com.projectpulse.dto.UserRegistrationDto;
import com.projectpulse.entity.User;
import com.projectpulse.service.ProjectService;
import com.projectpulse.service.TaskService;
import com.projectpulse.service.UserService;
import com.projectpulse.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final SecurityUtils securityUtils;

    // ── Admin overview ──────────────────────────────────────────────────────
    @GetMapping
    public String adminDashboard(Model model) {
        model.addAttribute("totalUsers",    userService.countAll());
        model.addAttribute("totalProjects", projectService.countAll());
        model.addAttribute("totalTasks",    taskService.countAll());
        model.addAttribute("completedTasks",taskService.countCompleted());
        model.addAttribute("allUsers",      userService.findAll());
        model.addAttribute("allProjects",   projectService.findAll());
        model.addAttribute("currentUser",   securityUtils.getCurrentUser());
        return "admin/dashboard";
    }

    // ── User management ─────────────────────────────────────────────────────
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        model.addAttribute("currentUser", securityUtils.getCurrentUser());
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        model.addAttribute("currentUser", securityUtils.getCurrentUser());
        return "admin/user-form";
    }

    @PostMapping("/users/new")
    public String createUser(
            @Valid @ModelAttribute("userDto") UserRegistrationDto dto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (!dto.isPasswordMatching()) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        if (result.hasErrors()) {
            model.addAttribute("currentUser", securityUtils.getCurrentUser());
            return "admin/user-form";
        }

        try {
            userService.registerUser(dto);
            redirectAttributes.addFlashAttribute("successMsg",
                "User \"" + dto.getUsername() + "\" created successfully!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        model.addAttribute("currentUser", securityUtils.getCurrentUser());
        return "admin/user-detail";
    }

    // ── Change user role ─────────────────────────────────────────────────────
    @PostMapping("/users/{id}/role")
    public String changeRole(
            @PathVariable Long id,
            @RequestParam String role,
            RedirectAttributes redirectAttributes) {
        userService.updateRole(id, role);
        redirectAttributes.addFlashAttribute("successMsg", "Role updated successfully.");
        return "redirect:/admin/users/" + id;
    }

    // ── Enable / disable user ────────────────────────────────────────────────
    @PostMapping("/users/{id}/toggle-status")
    public String toggleStatus(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        User current = securityUtils.getCurrentUser();
        if (current.getId().equals(id)) {
            redirectAttributes.addFlashAttribute("errorMsg", "You cannot disable your own account.");
            return "redirect:/admin/users";
        }
        userService.toggleUserStatus(id);
        redirectAttributes.addFlashAttribute("successMsg", "User status updated.");
        return "redirect:/admin/users";
    }
}
