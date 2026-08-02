package com.uday.urlshortener.controller;

import com.uday.urlshortener.model.Url;
import com.uday.urlshortener.service.UrlService;
import com.uday.urlshortener.util.UrlSanitizerUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PublicApiController {

    private final UrlService urlService;

    public PublicApiController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<?> shorten(@RequestBody Map<String, Object> body) {
        String originalUrl = (String) body.get("originalUrl");
        String customAlias = (String) body.get("customAlias");
        Integer expiryDaysObj = body.get("expiryDays") instanceof Integer ? (Integer) body.get("expiryDays") : 30;

        if (originalUrl == null || originalUrl.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "originalUrl is required"));
        }

        try {
            String sanitizedUrl = UrlSanitizerUtil.sanitizeAndNormalizeUrl(originalUrl);
            Url url = urlService.shortenUrl(sanitizedUrl, "PUBLIC_GUEST", expiryDaysObj, customAlias, null, false);
            return ResponseEntity.ok(Map.of(
                    "shortUrl", url.getShortUrl(),
                    "shortCode", url.getShortCode(),
                    "originalUrl", url.getOriginalUrl(),
                    "createdAt", url.getCreatedAt() != null ? url.getCreatedAt().toString() : ""
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to shorten URL: " + e.getMessage()));
        }
    }
}
