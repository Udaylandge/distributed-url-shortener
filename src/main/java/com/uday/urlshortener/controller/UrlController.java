package com.uday.urlshortener.controller;

import com.uday.urlshortener.model.Url;
import com.uday.urlshortener.service.QRCodeService;
import com.uday.urlshortener.service.UrlService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/urls")
public class UrlController {

    private final UrlService urlService;
    private final QRCodeService qrCodeService;

    public UrlController(UrlService urlService, QRCodeService qrCodeService) {
        this.urlService = urlService;
        this.qrCodeService = qrCodeService;
    }

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
        model.addAttribute("username", email);
        model.addAttribute("activeNav", "manage");

        return "url/manage";
    }

    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("activeNav", "create");
        return "url/create";
    }

    @PostMapping("/create")
    public String createShortUrl(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam String originalUrl,
                                 @RequestParam(defaultValue = "365") int expiryDays,
                                 @RequestParam(required = false) String customAlias,
                                 @RequestParam(required = false) String password,
                                 @RequestParam(defaultValue = "false") boolean oneTime,
                                 Model model) {

        String email = userDetails.getUsername();
        try {
            Url url = urlService.shortenUrl(originalUrl, email, expiryDays, customAlias, password, oneTime);
            model.addAttribute("shortUrl", url.getShortUrl());
            model.addAttribute("shortCode", url.getShortCode());
            model.addAttribute("createdUrl", url);
            model.addAttribute("success", true);
            model.addAttribute("username", email);
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("originalUrl", originalUrl);
            model.addAttribute("customAlias", customAlias);
        }

        model.addAttribute("activeNav", "create");
        return "url/create";
    }

    @PostMapping("/delete/{id}")
    public String softDeleteUrl(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable String id,
                                RedirectAttributes redirectAttributes) {
        try {
            urlService.softDeleteUrl(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMsg", "URL moved to trash bin.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/urls/manage";
    }

    @GetMapping("/trash")
    public String viewTrashBin(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               Model model) {
        String email = userDetails.getUsername();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Url> trashPage = urlService.getSoftDeletedUrlsByUser(email, pageable);

        model.addAttribute("trashPage", trashPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", trashPage.getTotalPages());
        model.addAttribute("username", email);
        model.addAttribute("activeNav", "trash");

        return "url/trash";
    }

    @PostMapping("/restore/{id}")
    public String restoreUrl(@AuthenticationPrincipal UserDetails userDetails,
                             @PathVariable String id,
                             RedirectAttributes redirectAttributes) {
        try {
            urlService.restoreUrl(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMsg", "URL restored successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/urls/trash";
    }

    @PostMapping("/permanent-delete/{id}")
    public String permanentDeleteUrl(@AuthenticationPrincipal UserDetails userDetails,
                                     @PathVariable String id,
                                     RedirectAttributes redirectAttributes) {
        try {
            urlService.deleteUrl(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMsg", "URL permanently deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/urls/trash";
    }

    @PostMapping("/toggle-active/{id}")
    public String toggleActiveStatus(@AuthenticationPrincipal UserDetails userDetails,
                                     @PathVariable String id,
                                     RedirectAttributes redirectAttributes) {
        try {
            urlService.toggleActiveStatus(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMsg", "URL active status updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/urls/manage";
    }

    @GetMapping("/qr/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getQRCodeImage(@PathVariable String id) {
        try {
            Url url = urlService.getUrlById(id);
            byte[] imageBytes = qrCodeService.generateQRCodeImage(url.getShortUrl(), 300, 300);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"qr-" + url.getShortCode() + ".png\"")
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/export/csv")
    @ResponseBody
    public ResponseEntity<byte[]> exportCsv(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        byte[] csvData = urlService.exportUserUrlsCsv(email);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"shortify-links.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}