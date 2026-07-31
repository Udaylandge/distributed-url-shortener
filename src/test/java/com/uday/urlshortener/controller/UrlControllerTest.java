package com.uday.urlshortener.controller;

import com.uday.urlshortener.model.Url;
import com.uday.urlshortener.security.CustomUserDetailsService;
import com.uday.urlshortener.service.UrlService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller unit tests for UrlController.
 *
 * Security filters are disabled via @AutoConfigureMockMvc(addFilters = false)
 * so we can focus on controller behaviour. @WithMockUser still injects the
 * mock authentication principal into @AuthenticationPrincipal parameters via
 * TestSecurityContextHolder (works independently of the filter chain).
 */
@WebMvcTest(UrlController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UrlController MVC Tests")
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    // Needed so SecurityConfig can instantiate in the limited @WebMvcTest context
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // ── GET /urls/create ─────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("GET /urls/create returns create view for authenticated user")
    void getCreate_authenticatedUser_returnsCreateView() throws Exception {
        mockMvc.perform(get("/urls/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("url/create"));
    }

    // ── GET /urls/manage ─────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("GET /urls/manage returns manage view with pagination model attributes")
    void getManage_authenticatedUser_returnsManageViewWithModel() throws Exception {
        Page<Url> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(urlService.searchUrlsByUser(anyString(), any(), any())).thenReturn(emptyPage);

        mockMvc.perform(get("/urls/manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("url/manage"))
                .andExpect(model().attributeExists("urlPage", "currentPage", "totalPages"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("GET /urls/manage with keyword passes keyword to model")
    void getManage_withKeyword_includesKeywordInModel() throws Exception {
        Page<Url> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(urlService.searchUrlsByUser(anyString(), eq("example"), any())).thenReturn(emptyPage);

        mockMvc.perform(get("/urls/manage").param("keyword", "example"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("keyword", "example"));
    }

    // ── POST /urls/create ────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("POST /urls/create shortens URL and returns create view with success")
    void postCreate_validUrl_returnsCreateViewWithSuccess() throws Exception {
        Url mockUrl = buildUrl("abc12", "https://example.com", "user@test.com");
        when(urlService.shortenUrl(anyString(), anyString(), anyInt())).thenReturn(mockUrl);

        mockMvc.perform(post("/urls/create")
                        .with(csrf())
                        .param("originalUrl", "https://example.com")
                        .param("expiryDays", "30"))
                .andExpect(status().isOk())
                .andExpect(view().name("url/create"))
                .andExpect(model().attributeExists("shortUrl", "createdUrl", "success"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("POST /urls/create uses default expiry of 365 days when not provided")
    void postCreate_defaultExpiry_usesDefaultDays() throws Exception {
        Url mockUrl = buildUrl("abc12", "https://example.com", "user@test.com");
        when(urlService.shortenUrl(anyString(), anyString(), eq(365))).thenReturn(mockUrl);

        mockMvc.perform(post("/urls/create")
                        .with(csrf())
                        .param("originalUrl", "https://example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("url/create"))
                .andExpect(model().attribute("success", true));
    }

    // ── POST /urls/delete/{id} ───────────────────────────────────────────────

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("POST /urls/delete/{id} redirects to manage page on success")
    void deleteUrl_success_redirectsToManage() throws Exception {
        mockMvc.perform(post("/urls/delete/someId")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/urls/manage"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("POST /urls/delete/{id} redirects with error flash attribute when service throws")
    void deleteUrl_serviceThrows_redirectsToManageWithError() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Permission denied"))
                .when(urlService).deleteUrl(anyString(), anyString());

        mockMvc.perform(post("/urls/delete/someId")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/urls/manage"))
                .andExpect(flash().attribute("errorMsg", "Permission denied"));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Url buildUrl(String shortCode, String originalUrl, String createdBy) {
        Url url = new Url();
        url.setShortCode(shortCode);
        url.setOriginalUrl(originalUrl);
        url.setShortUrl("http://localhost:8080/" + shortCode);
        url.setCreatedBy(createdBy);
        url.setActive(true);
        url.setClickCount(0L);
        url.setCreatedAt(LocalDateTime.now());
        url.setExpiryDate(LocalDateTime.now().plusDays(30));
        return url;
    }
}
