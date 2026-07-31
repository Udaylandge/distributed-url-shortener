package com.uday.urlshortener.controller;

import com.uday.urlshortener.model.Url;
import com.uday.urlshortener.security.CustomUserDetailsService;
import com.uday.urlshortener.service.UrlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller unit tests for RedirectController.
 *
 * Security filters are disabled via @AutoConfigureMockMvc(addFilters = false)
 * because: (a) the redirect endpoints are public anyway, and (b) verifying
 * Spring Security rules belongs in integration tests, not controller slices.
 */
@WebMvcTest(RedirectController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RedirectController MVC Tests")
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    // Needed so SecurityConfig can instantiate in the limited @WebMvcTest context
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // ── /{shortCode} redirect ────────────────────────────────────────────────

    @Test
    @DisplayName("GET /{shortCode} redirects to original URL when found and active (MongoDB fallback)")
    void redirect_activeUrl_redirectsToOriginal() throws Exception {
        Url url = buildUrl("abc12", "https://example.com");
        url.setActive(true);

        // Redis miss → fallback to MongoDB
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("url:abc12")).thenReturn(null);
        when(urlService.findByShortCode("abc12")).thenReturn(url);

        mockMvc.perform(get("/abc12"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://example.com"));
    }

    @Test
    @DisplayName("GET /{shortCode} redirects to error page when URL not found")
    void redirect_notFound_redirectsToErrorPage() throws Exception {
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("url:xyz99")).thenReturn(null);
        when(urlService.findByShortCode("xyz99")).thenReturn(null);

        mockMvc.perform(get("/xyz99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?error=not-found"));
    }

    @Test
    @DisplayName("GET /{shortCode} redirects to error page when URL is inactive")
    void redirect_inactiveUrl_redirectsToErrorPage() throws Exception {
        Url url = buildUrl("def45", "https://inactive.com");
        url.setActive(false);

        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("url:def45")).thenReturn(null);
        when(urlService.findByShortCode("def45")).thenReturn(url);

        mockMvc.perform(get("/def45"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?error=not-found"));
    }

    @Test
    @DisplayName("GET /{shortCode} serves redirect directly from Redis cache (cache hit)")
    void redirect_redisCacheHit_redirectsFromCache() throws Exception {
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("url:cached1")).thenReturn("https://cached-target.com");

        mockMvc.perform(get("/cached1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://cached-target.com"));
    }

    @Test
    @DisplayName("GET /{shortCode} falls back to MongoDB when Redis throws an exception")
    void redirect_redisThrows_fallsBackToMongo() throws Exception {
        Url url = buildUrl("retry1", "https://fallback.com");
        url.setActive(true);

        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis down"));
        when(urlService.findByShortCode("retry1")).thenReturn(url);

        mockMvc.perform(get("/retry1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://fallback.com"));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Url buildUrl(String shortCode, String originalUrl) {
        Url url = new Url();
        url.setShortCode(shortCode);
        url.setOriginalUrl(originalUrl);
        url.setShortUrl("http://localhost:8080/" + shortCode);
        url.setClickCount(0L);
        url.setCreatedAt(LocalDateTime.now());
        url.setExpiryDate(LocalDateTime.now().plusDays(30));
        return url;
    }
}
