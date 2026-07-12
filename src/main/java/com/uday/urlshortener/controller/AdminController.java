package com.uday.urlshortener.controller;

import com.uday.urlshortener.model.Url;
import com.uday.urlshortener.model.User;
import com.uday.urlshortener.service.UrlService;
import com.uday.urlshortener.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final UrlService urlService;

    public AdminController(UserService userService, UrlService urlService) {
        this.userService = userService;
        this.urlService = urlService;
    }

    // ── Admin Dashboard ──────────────────────────────────────────────────────

    @GetMapping("/panel")
    public String adminPanel(Model model) {

        List<User> users = userService.getAllUsers();
        List<Url> recentUrls = urlService.getRecentUrls();

        model.addAttribute("users", users);
        model.addAttribute("recentUrls", recentUrls);
        model.addAttribute("totalUsers", userService.countAllUsers());
        model.addAttribute("activeUsers", userService.countActiveUsers());
        model.addAttribute("totalUrls", urlService.countAllUrls());
        model.addAttribute("totalClicks", urlService.sumAllClicks());
        model.addAttribute("currentPage", "admin");

        return "admin/panel";
    }

    // ── All URLs (Paginated) ─────────────────────────────────────────────────

    @GetMapping("/urls")
    public String adminUrls(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "15") int size,
                            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Url> urlPage = urlService.getAllUrls(pageable);

        model.addAttribute("urlPage", urlPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", urlPage.getTotalPages());

        return "admin/urls";
    }

    // ── All Users ─────────────────────────────────────────────────────────────

    @GetMapping("/users")
    public String adminUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    // ── Toggle User Active ───────────────────────────────────────────────────

    @PostMapping("/users/toggle/{userId}")
    public String toggleUser(@PathVariable String userId, RedirectAttributes ra) {
        try {
            userService.toggleUserActive(userId);
            ra.addFlashAttribute("successMsg", "User status updated.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ── Delete User ──────────────────────────────────────────────────────────

    @PostMapping("/users/delete/{userId}")
    public String deleteUser(@PathVariable String userId, RedirectAttributes ra) {
        try {
            userService.deleteUser(userId);
            ra.addFlashAttribute("successMsg", "User deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ── Delete URL ───────────────────────────────────────────────────────────

    @PostMapping("/urls/delete/{id}")
    public String deleteUrl(@PathVariable String id, RedirectAttributes ra) {
        try {
            urlService.adminDeleteUrl(id);
            ra.addFlashAttribute("successMsg", "URL deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/urls";
    }
}
