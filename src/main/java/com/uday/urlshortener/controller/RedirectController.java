package com.uday.urlshortener.controller;

import com.uday.urlshortener.model.Url;
import com.uday.urlshortener.service.UrlService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

    @GetMapping("/{shortCode}")
    public String redirect(@PathVariable String shortCode) {

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