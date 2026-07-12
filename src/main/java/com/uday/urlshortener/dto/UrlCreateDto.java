package com.uday.urlshortener.dto;

/**
 * DTO for URL creation form binding.
 */
public class UrlCreateDto {

    private String originalUrl;
    private String expiryDays; // number of days until expiry, optional

    public UrlCreateDto() {}

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public String getExpiryDays() { return expiryDays; }
    public void setExpiryDays(String expiryDays) { this.expiryDays = expiryDays; }
}
