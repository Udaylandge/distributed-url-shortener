package com.uday.urlshortener.controller;

import com.uday.urlshortener.model.Url;
import com.uday.urlshortener.model.User;
import com.uday.urlshortener.service.UrlService;
import com.uday.urlshortener.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/analytics")
public class AnalyticsController {

    private final UrlService urlService;
    private final UserService userService;

    public AnalyticsController(UrlService urlService, UserService userService) {
        this.urlService = urlService;
        this.userService = userService;
    }

    @GetMapping
    public String analyticsPage(@AuthenticationPrincipal UserDetails userDetails,
                                Model model) {

        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);

        List<Url> topUrls = urlService.getTopUrlsByUser(email);
        List<Url> recentUrls = urlService.getRecentUrlsByUser(email);
        List<Url> allUrls = urlService.getUrlsByUser(email);

        long totalClicks = urlService.sumClicksByUser(email);
        long totalUrls = urlService.countByUser(email);
        long activeUrls = urlService.countActiveByUser(email);

        model.addAttribute("username", user.getFullName());
        model.addAttribute("user", user);
        model.addAttribute("topUrls", topUrls);
        model.addAttribute("recentUrls", recentUrls);
        model.addAttribute("allUrls", allUrls);
        model.addAttribute("totalClicks", totalClicks);
        model.addAttribute("totalUrls", totalUrls);
        model.addAttribute("activeUrls", activeUrls);
        model.addAttribute("currentPage", "analytics");

        return "analytics/analytics";
    }
}
