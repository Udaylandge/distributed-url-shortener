package com.uday.urlshortener.controller;

import com.uday.urlshortener.model.Url;
import com.uday.urlshortener.service.UrlService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;

@Controller
public class RedirectController {

    private final UrlService urlService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_URL_PREFIX = "url:";

    public RedirectController(UrlService urlService,
                               RedisTemplate<String, Object> redisTemplate) {
        this.urlService = urlService;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9]{1,10}}")
    public String redirect(@PathVariable String shortCode) {

        // Guard: ignore well-known browser auto-requests that are not short codes
        if (shortCode.contains(".") || shortCode.equalsIgnoreCase("favicon")
                || shortCode.equalsIgnoreCase("robots")
                || shortCode.equalsIgnoreCase("sitemap")) {
            return "redirect:/?error=not-found";
        }

        // 1. Try Redis cache first (millisecond-level lookup)
        try {
            Object cached = redisTemplate.opsForValue().get(REDIS_URL_PREFIX + shortCode);
            if (cached != null) {
                // Trigger click count increment (via service to keep consistent)
                urlService.incrementClickCount(shortCode);
                return "redirect:" + cached.toString();
            }
        } catch (Exception ignored) {
            // Redis unavailable - fallback to MongoDB
        }

        // 2. Fallback to MongoDB
        Url url = urlService.findByShortCode(shortCode);

        if (url == null || !url.isActive()) {
            return "redirect:/?error=not-found";
        }

        return "redirect:" + url.getOriginalUrl();
    }
}