package com.uday.urlshortener.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.uday.urlshortener.model.User;
import com.uday.urlshortener.service.UserService;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String registerPage(Model model) {

        model.addAttribute("user", new User());

        return "auth/register";
    }

    @PostMapping("/register")
public String registerUser(@ModelAttribute User user,
                           Model model) {

    try {

        userService.register(user);

        return "redirect:/login";

    } catch (RuntimeException e) {

        model.addAttribute("error", e.getMessage());
        model.addAttribute("user", user);

        return "auth/register";
    }

}

    @GetMapping("/login")
    public String loginPage() {

        return "auth/login";
    }

}