package com.uday.urlshortener.controller;

import com.uday.urlshortener.model.Url;
import com.uday.urlshortener.service.UrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Public REST API for URL shortening — no login required.
 * Used by the home page live demo to generate real short URLs.
 */
@RestController
@RequestMapping("/api")
public class PublicApiController {

    private final UrlService urlService;

    public PublicApiController(UrlService urlService) {
        this.urlService = urlService;
    }

    /**
     * POST /api/shorten
     * Body: { "originalUrl": "https://example.com" }
     * Returns: { "shortUrl": "http://localhost:8080/abc1", "shortCode": "abc1" }
     */
    @PostMapping("/shorten")
    public ResponseEntity<?> shorten(@RequestBody Map<String, String> body) {
        String originalUrl = body.get("originalUrl");

        if (originalUrl == null || originalUrl.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "originalUrl is required"));
        }

        // Validate basic URL format
        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL must start with http:// or https://"));
        }

        try {
            // Use SYSTEM user with 30-day expiry for public/demo URLs
            Url url = urlService.shortenUrl(originalUrl, "SYSTEM", 30);
            return ResponseEntity.ok(Map.of(
                    "shortUrl", url.getShortUrl(),
                    "shortCode", url.getShortCode(),
                    "originalUrl", url.getOriginalUrl()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to shorten URL"));
        }
    }
}
