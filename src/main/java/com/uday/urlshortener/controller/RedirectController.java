package com.uday.urlshortener.controller;

import com.uday.urlshortener.model.Url;
import com.uday.urlshortener.service.UrlService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Set;

@Controller
public class RedirectController {

    private final UrlService urlService;
    private final PasswordEncoder passwordEncoder;

    private static final Set<String> RESERVED_KEYWORDS = Set.of(
            "favicon", "favicon.ico", "robots", "robots.txt", "sitemap", "sitemap.xml",
            "login", "register", "logout", "dashboard", "analytics", "profile", "admin",
            "urls", "api", "css", "js", "images", "fonts", "error", "actuator"
    );

    public RedirectController(UrlService urlService, PasswordEncoder passwordEncoder) {
        this.urlService = urlService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9_-]{1,30}}")
    public String redirect(@PathVariable String shortCode, Model model) {

        if (shortCode == null || shortCode.contains(".") || RESERVED_KEYWORDS.contains(shortCode.toLowerCase())) {
            return "redirect:/?error=not-found";
        }

        Url url = urlService.findByIdentifier(shortCode);

        if (url == null || url.isDeleted()) {
            return "error/404";
        }

        if (!url.isActive()) {
            model.addAttribute("url", url);
            return "url/disabled";
        }

        if (url.getExpiryDate() != null && url.getExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("url", url);
            return "url/expired";
        }

        if (url.getPassword() != null && !url.getPassword().isBlank()) {
            model.addAttribute("shortCode", shortCode);
            return "url/password";
        }

        // Increment click count only after successful resolution
        urlService.incrementClickCount(shortCode);

        return "redirect:" + url.getOriginalUrl();
    }

    @PostMapping("/url/pass/{shortCode}")
    public String verifyPasswordAndRedirect(@PathVariable String shortCode,
                                           @RequestParam String password,
                                           Model model) {
        Url url = urlService.findByIdentifier(shortCode);

        if (url == null || url.isDeleted() || !url.isActive()) {
            return "error/404";
        }

        if (url.getExpiryDate() != null && url.getExpiryDate().isBefore(LocalDateTime.now())) {
            model.addAttribute("url", url);
            return "url/expired";
        }

        if (url.getPassword() != null && !passwordEncoder.matches(password.trim(), url.getPassword())) {
            model.addAttribute("shortCode", shortCode);
            model.addAttribute("error", "Incorrect password. Please try again.");
            return "url/password";
        }

        urlService.incrementClickCount(shortCode);
        return "redirect:" + url.getOriginalUrl();
    }
}