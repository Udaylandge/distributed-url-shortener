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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("GET /{shortCode} redirects to original URL when found and active")
    void redirect_activeUrl_redirectsToOriginal() throws Exception {
        Url url = buildUrl("abc12", "https://google.com");
        url.setActive(true);

        when(urlService.findByIdentifier("abc12")).thenReturn(url);

        mockMvc.perform(get("/abc12"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://google.com"));
    }

    @Test
    @DisplayName("GET /{shortCode} renders 404 error page when URL not found")
    void redirect_notFound_renders404View() throws Exception {
        when(urlService.findByIdentifier("xyz99")).thenReturn(null);

        mockMvc.perform(get("/xyz99"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));
    }

    @Test
    @DisplayName("GET /{shortCode} renders disabled view when URL is inactive")
    void redirect_inactiveUrl_rendersDisabledView() throws Exception {
        Url url = buildUrl("def45", "https://inactive.com");
        url.setActive(false);

        when(urlService.findByIdentifier("def45")).thenReturn(url);

        mockMvc.perform(get("/def45"))
                .andExpect(status().isOk())
                .andExpect(view().name("url/disabled"));
    }

    @Test
    @DisplayName("GET /{shortCode} renders expired view when URL expiryDate has passed")
    void redirect_expiredUrl_rendersExpiredView() throws Exception {
        Url url = buildUrl("exp12", "https://expired.com");
        url.setActive(true);
        url.setExpiryDate(LocalDateTime.now().minusDays(1));

        when(urlService.findByIdentifier("exp12")).thenReturn(url);

        mockMvc.perform(get("/exp12"))
                .andExpect(status().isOk())
                .andExpect(view().name("url/expired"));
    }

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
