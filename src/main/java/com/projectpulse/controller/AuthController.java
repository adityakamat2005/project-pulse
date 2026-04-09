package com.projectpulse.controller;

import com.projectpulse.dto.UserRegistrationDto;
import com.projectpulse.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String expired,
            Model model) {

        if (error != null)   model.addAttribute("errorMsg", "Invalid username or password.");
        if (logout != null)  model.addAttribute("logoutMsg", "You have been logged out successfully.");
        if (expired != null) model.addAttribute("errorMsg", "Your session has expired. Please log in again.");

        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("userDto") UserRegistrationDto dto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (!dto.isPasswordMatching()) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
        }

        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.registerUser(dto);
            redirectAttributes.addFlashAttribute("successMsg",
                "Registration successful! You can now log in.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMsg", ex.getMessage());
            return "auth/register";
        }
    }
}
