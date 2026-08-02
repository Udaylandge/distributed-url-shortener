package com.uday.urlshortener.controller;

import com.uday.urlshortener.model.Url;
import com.uday.urlshortener.security.CustomUserDetailsService;
import com.uday.urlshortener.service.QRCodeService;
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

@WebMvcTest(UrlController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UrlController MVC Tests")
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlService urlService;

    @MockitoBean
    private QRCodeService qrCodeService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("GET /urls/create returns create view for authenticated user")
    void getCreate_authenticatedUser_returnsCreateView() throws Exception {
        mockMvc.perform(get("/urls/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("url/create"));
    }

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

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("POST /urls/create shortens URL and returns create view with success")
    void postCreate_validUrl_returnsCreateViewWithSuccess() throws Exception {
        Url mockUrl = buildUrl("abc12", "https://google.com", "user@test.com");
        when(urlService.shortenUrl(anyString(), anyString(), anyInt(), any(), any(), anyBoolean())).thenReturn(mockUrl);

        mockMvc.perform(post("/urls/create")
                        .with(csrf())
                        .param("originalUrl", "google.com")
                        .param("expiryDays", "30"))
                .andExpect(status().isOk())
                .andExpect(view().name("url/create"))
                .andExpect(model().attributeExists("shortUrl", "createdUrl", "success"));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("POST /urls/create uses default expiry of 365 days when not provided")
    void postCreate_defaultExpiry_usesDefaultDays() throws Exception {
        Url mockUrl = buildUrl("abc12", "https://google.com", "user@test.com");
        when(urlService.shortenUrl(anyString(), anyString(), eq(365), any(), any(), anyBoolean())).thenReturn(mockUrl);

        mockMvc.perform(post("/urls/create")
                        .with(csrf())
                        .param("originalUrl", "google.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("url/create"))
                .andExpect(model().attribute("success", true));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("POST /urls/delete/{id} redirects to manage page on success")
    void deleteUrl_success_redirectsToManage() throws Exception {
        mockMvc.perform(post("/urls/delete/someId")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/urls/manage"));
    }

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
