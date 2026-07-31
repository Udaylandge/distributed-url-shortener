package com.uday.urlshortener.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.uday.urlshortener.model.Url;
import com.uday.urlshortener.repository.UrlRepository;
import com.uday.urlshortener.service.SequenceGeneratorService;
import com.uday.urlshortener.service.UrlService;
import com.uday.urlshortener.util.Base62Encoder;


@Service
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final String REDIS_URL_PREFIX = "url:";
    private static final long REDIS_TTL_HOURS = 24;

    public UrlServiceImpl(UrlRepository urlRepository,
                          SequenceGeneratorService sequenceGeneratorService,
                          RedisTemplate<String, Object> redisTemplate) {
        this.urlRepository = urlRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
        this.redisTemplate = redisTemplate;
    }

    // ── Existing core method (kept intact) ──────────────────────────────────

    @Override
    public Url shortenUrl(String originalUrl) {
        return shortenUrl(originalUrl, "SYSTEM", 365);
    }

    // ── Authenticated URL Creation ───────────────────────────────────────────

    @Override
    public Url shortenUrl(String originalUrl, String email, int expiryDays) {

        long sequence = sequenceGeneratorService.getNextSequence("url_sequence");
        String shortCode = Base62Encoder.encode(sequence);

        Url url = new Url();
        url.setOriginalUrl(originalUrl);
        url.setShortCode(shortCode);
        url.setShortUrl(baseUrl + "/" + shortCode);
        url.setClickCount(0);
        url.setCreatedAt(LocalDateTime.now());
        url.setExpiryDate(LocalDateTime.now().plusDays(expiryDays));
        url.setActive(true);
        url.setCreatedBy(email);

        Url saved = urlRepository.save(url);

        // Cache in Redis immediately
        cacheUrl(shortCode, originalUrl);

        return saved;
    }

    // ── Redis Redirect with Click Count ─────────────────────────────────────

    @Override
    public Url findByShortCode(String shortCode) {

        // 1. Try Redis cache first (graceful degradation if Redis unavailable)
        try {
            String redisKey = REDIS_URL_PREFIX + shortCode;
            Object cached = redisTemplate.opsForValue().get(redisKey);
            if (cached != null) {
                incrementClickCount(shortCode);
                return urlRepository.findByShortCode(shortCode).orElse(null);
            }
        } catch (Exception ignored) {
            // Redis unavailable — fall through to MongoDB
        }

        // 2. Fallback to MongoDB
        Url url = urlRepository.findByShortCode(shortCode).orElse(null);
        if (url != null) {
            cacheUrl(shortCode, url.getOriginalUrl());
        }
        return url;
    }

    @Override
    public void incrementClickCount(String shortCode) {
        urlRepository.findByShortCode(shortCode).ifPresent(url -> {
            url.setClickCount(url.getClickCount() + 1);
            urlRepository.save(url);
        });
    }

    private void cacheUrl(String shortCode, String originalUrl) {
        try {
            redisTemplate.opsForValue().set(
                    REDIS_URL_PREFIX + shortCode,
                    originalUrl,
                    REDIS_TTL_HOURS,
                    TimeUnit.HOURS
            );
        } catch (Exception ignored) {
            // Redis unavailable – silently degrade to MongoDB-only
        }
    }

    // ── URL Management ───────────────────────────────────────────────────────

    @Override
    public List<Url> getUrlsByUser(String email) {
        return urlRepository.findByCreatedBy(email);
    }

    @Override
    public Page<Url> getUrlsByUser(String email, Pageable pageable) {
        return urlRepository.findByCreatedBy(email, pageable);
    }

    @Override
    public Page<Url> searchUrlsByUser(String email, String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return urlRepository.findByCreatedBy(email, pageable);
        }
        return urlRepository.searchByCreatedBy(email, keyword, pageable);
    }

    @Override
    public void deleteUrl(String id, String email) {
        Url url = urlRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("URL not found."));

        if (!url.getCreatedBy().equals(email)) {
            throw new RuntimeException("You do not have permission to delete this URL.");
        }

        // Evict from Redis cache
        try {
            redisTemplate.delete(REDIS_URL_PREFIX + url.getShortCode());
        } catch (Exception ignored) {}

        urlRepository.delete(url);
    }

    @Override
    public Url getUrlById(String id) {
        return urlRepository.findById(id).orElseThrow(() -> new RuntimeException("URL not found."));
    }

    // ── Dashboard Stats ──────────────────────────────────────────────────────

    @Override
    public long countByUser(String email) {
        return urlRepository.countByCreatedBy(email);
    }

    @Override
    public long countActiveByUser(String email) {
        return urlRepository.countByCreatedByAndActive(email, true);
    }

    @Override
    public long countExpiredByUser(String email) {
        return urlRepository.countByCreatedByAndExpiryDateBefore(email, LocalDateTime.now());
    }

    @Override
    public long countTodayByUser(String email) {
        return urlRepository.countByCreatedByAndCreatedAtAfter(
                email, LocalDate.now().atStartOfDay()
        );
    }

    @Override
    public long sumClicksByUser(String email) {
        return urlRepository.findByCreatedBy(email).stream()
                .mapToLong(Url::getClickCount)
                .sum();
    }

    // ── Analytics ────────────────────────────────────────────────────────────

    @Override
    public List<Url> getTopUrlsByUser(String email) {
        return urlRepository.findTop5ByCreatedByOrderByClickCountDesc(email);
    }

    @Override
    public List<Url> getRecentUrlsByUser(String email) {
        return urlRepository.findTop10ByCreatedByOrderByCreatedAtDesc(email);
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    @Override
    public Page<Url> getAllUrls(Pageable pageable) {
        return urlRepository.findAll(pageable);
    }

    @Override
    public void adminDeleteUrl(String id) {
        Url url = urlRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("URL not found."));
        try {
            redisTemplate.delete(REDIS_URL_PREFIX + url.getShortCode());
        } catch (Exception ignored) {}
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
        return urlRepository.findTop10ByOrderByCreatedAtDesc();
    }
}