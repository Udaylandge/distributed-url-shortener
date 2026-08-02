package com.uday.urlshortener.controller;

import com.uday.urlshortener.dto.DashboardStatsDto;
import com.uday.urlshortener.model.Url;
import com.uday.urlshortener.model.User;
import com.uday.urlshortener.service.UrlService;
import com.uday.urlshortener.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final UrlService urlService;
    private final UserService userService;

    public DashboardController(UrlService urlService, UserService userService) {
        this.urlService = urlService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String getDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);

        DashboardStatsDto stats = new DashboardStatsDto(
                urlService.countByUser(email),
                urlService.sumClicksByUser(email),
                urlService.countActiveByUser(email),
                urlService.countExpiredByUser(email),
                urlService.countTodayByUser(email),
                urlService.countByUser(email) // Total links with active QR codes
        );

        List<Url> recentUrls = urlService.getRecentUrlsByUser(email);
        List<Url> topUrls = urlService.getTopUrlsByUser(email);

        model.addAttribute("username", user.getFullName());
        model.addAttribute("user", user);
        model.addAttribute("stats", stats);
        model.addAttribute("recentUrls", recentUrls);
        model.addAttribute("topUrls", topUrls);
        model.addAttribute("currentPage", "dashboard");

        return "dashboard/dashboard";
    }
}