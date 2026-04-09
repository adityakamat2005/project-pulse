package com.projectpulse.controller;

import com.projectpulse.entity.User;
import com.projectpulse.service.UserService;
import com.projectpulse.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String viewProfile(Model model) {
        User currentUser = securityUtils.getCurrentUser();
        model.addAttribute("user", currentUser);
        model.addAttribute("currentUser", currentUser);
        return "shared/profile";
    }

    @PostMapping("/update")
    public String updateProfile(
            @RequestParam String fullName,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String phone,
            RedirectAttributes redirectAttributes) {

        User currentUser = securityUtils.getCurrentUser();
        userService.updateProfile(currentUser.getId(), fullName, bio, phone);
        redirectAttributes.addFlashAttribute("successMsg", "Profile updated successfully!");
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMsg", "New passwords do not match.");
            return "redirect:/profile";
        }
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMsg", "Password must be at least 6 characters.");
            return "redirect:/profile";
        }

        try {
            User currentUser = securityUtils.getCurrentUser();
            userService.changePassword(currentUser.getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMsg", "Password changed successfully!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/profile";
    }
}
