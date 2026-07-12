package com.uday.urlshortener.service;

import com.uday.urlshortener.model.Url;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UrlService {

    // Core operations (existing)
    Url shortenUrl(String originalUrl);
    Url findByShortCode(String shortCode);

    // Authenticated URL creation
    Url shortenUrl(String originalUrl, String email, int expiryDays);

    // URL management
    List<Url> getUrlsByUser(String email);
    Page<Url> getUrlsByUser(String email, Pageable pageable);
    Page<Url> searchUrlsByUser(String email, String keyword, Pageable pageable);

    // URL operations
    void deleteUrl(String id, String email);
    Url getUrlById(String id);

    // Click tracking (Redis + MongoDB)
    void incrementClickCount(String shortCode);

    // Dashboard stats
    long countByUser(String email);
    long countActiveByUser(String email);
    long countExpiredByUser(String email);
    long countTodayByUser(String email);
    long sumClicksByUser(String email);

    // Analytics
    List<Url> getTopUrlsByUser(String email);
    List<Url> getRecentUrlsByUser(String email);

    // Admin
    Page<Url> getAllUrls(Pageable pageable);
    void adminDeleteUrl(String id);
    long countAllUrls();
    long sumAllClicks();
    List<Url> getRecentUrls();
}