package com.uday.urlshortener.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.uday.urlshortener.model.Url;
import com.uday.urlshortener.service.UrlService;

@Controller
@RequestMapping("/urls")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    // ── URL Management List ──────────────────────────────────────────────────

    @GetMapping("/manage")
    public String manageUrls(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String keyword,
                             Model model) {

        String email = userDetails.getUsername();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Url> urlPage = urlService.searchUrlsByUser(email, keyword, pageable);

        model.addAttribute("urlPage", urlPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", urlPage.getTotalPages());
        model.addAttribute("username", userDetails.getUsername());

        return "url/manage";
    }

    // ── Create URL (GET) ─────────────────────────────────────────────────────

    @GetMapping("/create")
    public String createPage() {
        return "url/create";
    }

    // ── Create URL (POST) ────────────────────────────────────────────────────

    @PostMapping("/create")
    public String createShortUrl(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam String originalUrl,
                                 @RequestParam(defaultValue = "365") int expiryDays,
                                 Model model) {

        String email = userDetails.getUsername();
        Url url = urlService.shortenUrl(originalUrl, email, expiryDays);

        model.addAttribute("shortUrl", url.getShortUrl());
        model.addAttribute("shortCode", url.getShortCode());
        model.addAttribute("createdUrl", url);
        model.addAttribute("success", true);
        model.addAttribute("username", email);

        return "url/create";
    }

    // ── Delete URL ───────────────────────────────────────────────────────────

    @PostMapping("/delete/{id}")
    public String deleteUrl(@AuthenticationPrincipal UserDetails userDetails,
                            @PathVariable String id,
                            RedirectAttributes redirectAttributes) {
        try {
            urlService.deleteUrl(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMsg", "URL deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/urls/manage";
    }
}