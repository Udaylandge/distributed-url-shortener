package com.uday.urlshortener.controller;

import com.uday.urlshortener.model.User;
import com.uday.urlshortener.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        try {
            userService.register(user);
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "auth/register";
        }
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String registered,
                            @RequestParam(required = false) String verified,
                            @RequestParam(required = false) String resetSuccess,
                            Model model) {

        if (error != null) {
            model.addAttribute("errorMsg", "Invalid email or password.");
        }
        if (logout != null) {
            model.addAttribute("infoMsg", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("successMsg", "Account created successfully! Please check your email to verify your account or sign in.");
        }
        if (verified != null) {
            model.addAttribute("successMsg", "Email address verified successfully! You can now log in.");
        }
        if (resetSuccess != null) {
            model.addAttribute("successMsg", "Password reset successfully! Please sign in with your new password.");
        }

        return "auth/login";
    }
}