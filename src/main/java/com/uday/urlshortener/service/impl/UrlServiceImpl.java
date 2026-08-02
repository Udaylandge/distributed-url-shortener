package com.uday.urlshortener.service.impl;

import com.uday.urlshortener.dto.UrlAnalyticsDto;
import com.uday.urlshortener.exception.ResourceNotFoundException;
import com.uday.urlshortener.model.Url;
import com.uday.urlshortener.repository.UrlRepository;
import com.uday.urlshortener.service.SequenceGeneratorService;
import com.uday.urlshortener.service.UrlService;
import com.uday.urlshortener.util.Base62Encoder;
import com.uday.urlshortener.util.RedisFallbackHelper;
import com.uday.urlshortener.util.UrlSanitizerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UrlServiceImpl implements UrlService {

    private static final Logger log = LoggerFactory.getLogger(UrlServiceImpl.class);

    private final UrlRepository urlRepository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final RedisFallbackHelper redisFallbackHelper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final String REDIS_URL_PREFIX = "url:";
    private static final long REDIS_TTL_HOURS = 24;

    public UrlServiceImpl(UrlRepository urlRepository,
                          SequenceGeneratorService sequenceGeneratorService,
                          RedisFallbackHelper redisFallbackHelper,
                          PasswordEncoder passwordEncoder) {
        this.urlRepository = urlRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.redisFallbackHelper = redisFallbackHelper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Url shortenUrl(String originalUrl) {
        return shortenUrl(originalUrl, "SYSTEM", 365, null, null, false);
    }

    @Override
    public Url shortenUrl(String originalUrl, String email, int expiryDays) {
        return shortenUrl(originalUrl, email, expiryDays, null, null, false);
    }

    @Override
    public Url shortenUrl(String originalUrl, String email, int expiryDays, String customAlias, String rawPassword, boolean isOneTime) {
        String sanitizedUrl = UrlSanitizerUtil.sanitizeAndNormalizeUrl(originalUrl);
        String finalEmail = (email != null && !email.isBlank()) ? email.trim().toLowerCase() : "SYSTEM";
        int validExpiryDays = Math.max(1, expiryDays);

        String shortCode;
        String aliasToSave = null;

        if (customAlias != null && !customAlias.isBlank()) {
            String trimmedAlias = customAlias.trim();
            if (trimmedAlias.contains("@") || trimmedAlias.contains(".") || trimmedAlias.contains(" ") ||
                    trimmedAlias.toLowerCase().startsWith("http://") || trimmedAlias.toLowerCase().startsWith("https://")) {
                throw new IllegalArgumentException("Custom alias cannot contain '@', '.', spaces, or URLs. Only letters, numbers, hyphens, and underscores are allowed.");
            }
            if (!trimmedAlias.matches("^[a-zA-Z0-9_-]{3,30}$")) {
                throw new IllegalArgumentException("Custom alias must be 3-30 characters long and contain only letters, numbers, hyphens, or underscores.");
            }
            if (urlRepository.existsByCustomAlias(trimmedAlias) || urlRepository.existsByShortCode(trimmedAlias)) {
                throw new IllegalArgumentException("Custom alias '" + trimmedAlias + "' is already taken. Please choose another.");
            }
            aliasToSave = trimmedAlias;
            shortCode = trimmedAlias;
        } else {
            long sequence = sequenceGeneratorService.getNextSequence("url_sequence");
            shortCode = Base62Encoder.encode(sequence);
            while (urlRepository.existsByShortCode(shortCode) || urlRepository.existsByCustomAlias(shortCode)) {
                sequence = sequenceGeneratorService.getNextSequence("url_sequence");
                shortCode = Base62Encoder.encode(sequence);
            }
        }

        String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        Url url = new Url();
        url.setOriginalUrl(sanitizedUrl);
        url.setShortCode(shortCode);
        url.setCustomAlias(aliasToSave);
        url.setShortUrl(cleanBaseUrl + "/" + shortCode);
        url.setClickCount(0);
        url.setCreatedAt(LocalDateTime.now());
        url.setExpiryDate(LocalDateTime.now().plusDays(validExpiryDays));
        url.setActive(true);
        url.setOneTime(isOneTime);
        url.setDeleted(false);
        url.setCreatedBy(finalEmail);

        if (rawPassword != null && !rawPassword.isBlank()) {
            url.setPassword(passwordEncoder.encode(rawPassword.trim()));
        }

        Url saved = urlRepository.save(url);

        if (url.getPassword() == null && !url.isOneTime()) {
            cacheUrl(shortCode, sanitizedUrl);
        }

        return saved;
    }

    @Override
    public Url findByShortCode(String shortCode) {
        return findByIdentifier(shortCode);
    }

    @Override
    public Url findByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return null;
        }

        Object cachedObj = redisFallbackHelper.get(REDIS_URL_PREFIX + identifier);
        if (cachedObj != null) {
            Optional<Url> urlOpt = urlRepository.findByShortCodeOrCustomAlias(identifier);
            if (urlOpt.isPresent() && !urlOpt.get().isDeleted()) {
                return urlOpt.get();
            }
        }

        Optional<Url> urlOpt = urlRepository.findByShortCodeOrCustomAlias(identifier);
        urlOpt.ifPresent(url -> {
            if (url.isActive() && !url.isDeleted() && url.getPassword() == null && !url.isOneTime()) {
                cacheUrl(identifier, url.getOriginalUrl());
            }
        });
        return urlOpt.orElse(null);
    }

    @Override
    public void incrementClickCount(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return;
        }
        urlRepository.findByShortCodeOrCustomAlias(identifier).ifPresent(url -> {
            url.setClickCount(url.getClickCount() + 1);
            if (url.isOneTime()) {
                url.setActive(false);
                redisFallbackHelper.delete(REDIS_URL_PREFIX + identifier);
            }
            urlRepository.save(url);
        });
    }

    private void cacheUrl(String key, String originalUrl) {
        redisFallbackHelper.set(REDIS_URL_PREFIX + key, originalUrl, REDIS_TTL_HOURS, TimeUnit.HOURS);
    }

    @Override
    public List<Url> getUrlsByUser(String email) {
        if (email == null || email.isBlank()) {
            return List.of();
        }
        return urlRepository.findByCreatedByAndDeletedFalse(email.trim().toLowerCase());
    }

    @Override
    public Page<Url> getUrlsByUser(String email, Pageable pageable) {
        if (email == null || email.isBlank()) {
            return Page.empty();
        }
        return urlRepository.findByCreatedByAndDeletedFalse(email.trim().toLowerCase(), pageable);
    }

    @Override
    public Page<Url> searchUrlsByUser(String email, String keyword, Pageable pageable) {
        if (email == null || email.isBlank()) {
            return Page.empty();
        }
        String cleanEmail = email.trim().toLowerCase();
        if (keyword == null || keyword.isBlank()) {
            return urlRepository.findByCreatedByAndDeletedFalse(cleanEmail, pageable);
        }
        return urlRepository.searchByCreatedByAndDeletedFalse(cleanEmail, keyword.trim(), pageable);
    }

    @Override
    public Page<Url> getSoftDeletedUrlsByUser(String email, Pageable pageable) {
        if (email == null || email.isBlank()) {
            return Page.empty();
        }
        return urlRepository.findByCreatedByAndDeletedTrue(email.trim().toLowerCase(), pageable);
    }

    @Override
    public void softDeleteUrl(String id, String email) {
        Url url = getUrlById(id);
        validateOwnership(url, email);
        url.setDeleted(true);
        urlRepository.save(url);
        redisFallbackHelper.delete(REDIS_URL_PREFIX + url.getShortCode());
        if (url.getCustomAlias() != null) {
            redisFallbackHelper.delete(REDIS_URL_PREFIX + url.getCustomAlias());
        }
    }

    @Override
    public void restoreUrl(String id, String email) {
        Url url = getUrlById(id);
        validateOwnership(url, email);
        url.setDeleted(false);
        urlRepository.save(url);
    }

    @Override
    public void deleteUrl(String id, String email) {
        Url url = getUrlById(id);
        validateOwnership(url, email);
        redisFallbackHelper.delete(REDIS_URL_PREFIX + url.getShortCode());
        if (url.getCustomAlias() != null) {
            redisFallbackHelper.delete(REDIS_URL_PREFIX + url.getCustomAlias());
        }
        urlRepository.delete(url);
    }

    @Override
    public Url getUrlById(String id) {
        if (id == null || id.isBlank()) {
            throw new ResourceNotFoundException("Invalid URL ID.");
        }
        return urlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("URL record not found with id: " + id));
    }

    @Override
    public void toggleActiveStatus(String id, String email) {
        Url url = getUrlById(id);
        validateOwnership(url, email);
        url.setActive(!url.isActive());
        urlRepository.save(url);
        if (!url.isActive()) {
            redisFallbackHelper.delete(REDIS_URL_PREFIX + url.getShortCode());
            if (url.getCustomAlias() != null) {
                redisFallbackHelper.delete(REDIS_URL_PREFIX + url.getCustomAlias());
            }
        }
    }

    private void validateOwnership(Url url, String email) {
        if (email == null || !url.getCreatedBy().equalsIgnoreCase(email.trim())) {
            throw new IllegalArgumentException("You do not have permission to modify this URL.");
        }
    }

    @Override
    public long countByUser(String email) {
        if (email == null || email.isBlank()) return 0;
        return urlRepository.countByCreatedByAndDeletedFalse(email.trim().toLowerCase());
    }

    @Override
    public long countActiveByUser(String email) {
        if (email == null || email.isBlank()) return 0;
        return urlRepository.countByCreatedByAndActiveAndDeletedFalse(email.trim().toLowerCase(), true);
    }

    @Override
    public long countExpiredByUser(String email) {
        if (email == null || email.isBlank()) return 0;
        return urlRepository.countByCreatedByAndExpiryDateBeforeAndDeletedFalse(email.trim().toLowerCase(), LocalDateTime.now());
    }

    @Override
    public long countTodayByUser(String email) {
        if (email == null || email.isBlank()) return 0;
        return urlRepository.countByCreatedByAndCreatedAtAfterAndDeletedFalse(
                email.trim().toLowerCase(), LocalDate.now().atStartOfDay()
        );
    }

    @Override
    public long sumClicksByUser(String email) {
        if (email == null || email.isBlank()) return 0;
        return urlRepository.findByCreatedByAndDeletedFalse(email.trim().toLowerCase()).stream()
                .mapToLong(Url::getClickCount)
                .sum();
    }

    @Override
    public List<Url> getTopUrlsByUser(String email) {
        if (email == null || email.isBlank()) return List.of();
        return urlRepository.findTop5ByCreatedByAndDeletedFalseOrderByClickCountDesc(email.trim().toLowerCase());
    }

    @Override
    public List<UrlAnalyticsDto> getTopUrlAnalyticsByUser(String email) {
        List<Url> topUrls = getTopUrlsByUser(email);
        if (topUrls == null || topUrls.isEmpty()) {
            return List.of();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return topUrls.stream()
                .map(u -> new UrlAnalyticsDto(
                        u.getId(),
                        u.getShortCode(),
                        u.getCustomAlias(),
                        u.getOriginalUrl(),
                        u.getShortUrl(),
                        u.getClickCount(),
                        u.getCreatedAt() != null ? u.getCreatedAt().format(formatter) : "",
                        u.isActive()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<Url> getRecentUrlsByUser(String email) {
        if (email == null || email.isBlank()) return List.of();
        return urlRepository.findTop10ByCreatedByAndDeletedFalseOrderByCreatedAtDesc(email.trim().toLowerCase());
    }

    @Override
    public Page<Url> getAllUrls(Pageable pageable) {
        return urlRepository.findByDeletedFalse(pageable);
    }

    @Override
    public void adminDeleteUrl(String id) {
        Url url = getUrlById(id);
        redisFallbackHelper.delete(REDIS_URL_PREFIX + url.getShortCode());
        if (url.getCustomAlias() != null) {
            redisFallbackHelper.delete(REDIS_URL_PREFIX + url.getCustomAlias());
        }
        urlRepository.delete(url);
    }

    @Override
    public long countAllUrls() {
        return urlRepository.count();
    }

    @Override
    public long sumAllClicks() {
        return urlRepository.findAll().stream()
                .mapToLong(Url::getClickCount)
                .sum();
    }

    @Override
    public List<Url> getRecentUrls() {
        return urlRepository.findTop10ByDeletedFalseOrderByCreatedAtDesc();
    }

    @Override
    public byte[] exportUserUrlsCsv(String email) {
        List<Url> urls = getUrlsByUser(email);
        StringBuilder csv = new StringBuilder();
        csv.append("Short Code,Custom Alias,Original URL,Short URL,Click Count,Active,One Time,Expiry Date,Created At\n");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Url url : urls) {
            csv.append("\"").append(url.getShortCode()).append("\",")
                    .append("\"").append(url.getCustomAlias() != null ? url.getCustomAlias() : "").append("\",")
                    .append("\"").append(url.getOriginalUrl().replace("\"", "\"\"")).append("\",")
                    .append("\"").append(url.getShortUrl()).append("\",")
                    .append(url.getClickCount()).append(",")
                    .append(url.isActive()).append(",")
                    .append(url.isOneTime()).append(",")
                    .append("\"").append(url.getExpiryDate() != null ? url.getExpiryDate().format(dtf) : "").append("\",")
                    .append("\"").append(url.getCreatedAt() != null ? url.getCreatedAt().format(dtf) : "").append("\"\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
}