package com.uday.urlshortener.service;

import com.uday.urlshortener.dto.UrlAnalyticsDto;
import com.uday.urlshortener.model.Url;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UrlService {

    Url shortenUrl(String originalUrl);

    Url shortenUrl(String originalUrl, String email, int expiryDays);

    Url shortenUrl(String originalUrl, String email, int expiryDays, String customAlias, String rawPassword, boolean isOneTime);

    Url findByShortCode(String shortCode);

    Url findByIdentifier(String identifier);

    List<Url> getUrlsByUser(String email);

    Page<Url> getUrlsByUser(String email, Pageable pageable);

    Page<Url> searchUrlsByUser(String email, String keyword, Pageable pageable);

    Page<Url> getSoftDeletedUrlsByUser(String email, Pageable pageable);

    void softDeleteUrl(String id, String email);

    void restoreUrl(String id, String email);

    void deleteUrl(String id, String email);

    Url getUrlById(String id);

    void incrementClickCount(String identifier);

    long countByUser(String email);

    long countActiveByUser(String email);

    long countExpiredByUser(String email);

    long countTodayByUser(String email);

    long sumClicksByUser(String email);

    List<Url> getTopUrlsByUser(String email);

    List<UrlAnalyticsDto> getTopUrlAnalyticsByUser(String email);

    List<Url> getRecentUrlsByUser(String email);

    Page<Url> getAllUrls(Pageable pageable);

    void adminDeleteUrl(String id);

    long countAllUrls();

    long sumAllClicks();

    List<Url> getRecentUrls();

    void toggleActiveStatus(String id, String email);

    byte[] exportUserUrlsCsv(String email);
}