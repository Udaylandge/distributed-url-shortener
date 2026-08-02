package com.uday.urlshortener.dto;

/**
 * Data Transfer Object for safely passing URL analytics data to frontend & Chart.js
 * without serializing complex Java LocalDateTime objects.
 */
public class UrlAnalyticsDto {

    private String id;
    private String shortCode;
    private String customAlias;
    private String originalUrl;
    private String shortUrl;
    private long clickCount;
    private String createdAt;
    private boolean active;

    public UrlAnalyticsDto() {}

    public UrlAnalyticsDto(String id, String shortCode, String customAlias, String originalUrl,
                          String shortUrl, long clickCount, String createdAt, boolean active) {
        this.id = id;
        this.shortCode = shortCode;
        this.customAlias = customAlias;
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.clickCount = clickCount;
        this.createdAt = createdAt;
        this.active = active;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public String getCustomAlias() { return customAlias; }
    public void setCustomAlias(String customAlias) { this.customAlias = customAlias; }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public String getShortUrl() { return shortUrl; }
    public void setShortUrl(String shortUrl) { this.shortUrl = shortUrl; }

    public long getClickCount() { return clickCount; }
    public void setClickCount(long clickCount) { this.clickCount = clickCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
