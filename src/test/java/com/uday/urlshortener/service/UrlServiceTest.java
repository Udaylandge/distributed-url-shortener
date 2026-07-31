package com.uday.urlshortener.service;

import com.uday.urlshortener.model.Url;
import com.uday.urlshortener.repository.UrlRepository;
import com.uday.urlshortener.service.impl.UrlServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UrlService Unit Tests")
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private UrlServiceImpl urlService;

    @BeforeEach
    void setUp() {
        urlService = new UrlServiceImpl(urlRepository, sequenceGeneratorService, redisTemplate);
        ReflectionTestUtils.setField(urlService, "baseUrl", "http://localhost:8080");

        // Default stub: Redis ops available
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // ── shortenUrl ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("shortenUrl creates Url with correct fields and saves to DB")
    void shortenUrl_createsAndSavesUrl() {
        when(sequenceGeneratorService.getNextSequence("url_sequence")).thenReturn(1L);

        Url saved = buildUrl("abc123", "https://example.com", "user@test.com");
        when(urlRepository.save(any(Url.class))).thenReturn(saved);

        Url result = urlService.shortenUrl("https://example.com", "user@test.com", 30);

        assertThat(result).isNotNull();
        assertThat(result.getOriginalUrl()).isEqualTo("https://example.com");
        assertThat(result.getCreatedBy()).isEqualTo("user@test.com");
        verify(urlRepository, times(1)).save(any(Url.class));
    }

    @Test
    @DisplayName("shortenUrl(String) delegates to three-arg overload with SYSTEM user and 365 days")
    void shortenUrl_singleArg_delegatesToThreeArgOverload() {
        when(sequenceGeneratorService.getNextSequence("url_sequence")).thenReturn(10L);
        Url saved = buildUrl("xyz", "https://google.com", "SYSTEM");
        when(urlRepository.save(any(Url.class))).thenReturn(saved);

        Url result = urlService.shortenUrl("https://google.com");

        assertThat(result.getCreatedBy()).isEqualTo("SYSTEM");
        verify(urlRepository, times(1)).save(any(Url.class));
    }

    @Test
    @DisplayName("shortenUrl generated shortUrl contains base URL prefix")
    void shortenUrl_shortUrlContainsBaseUrl() {
        when(sequenceGeneratorService.getNextSequence("url_sequence")).thenReturn(5L);
        when(urlRepository.save(any(Url.class))).thenAnswer(inv -> inv.getArgument(0));

        Url result = urlService.shortenUrl("https://example.com", "user@test.com", 7);

        assertThat(result.getShortUrl()).startsWith("http://localhost:8080/");
    }

    // ── findByShortCode ──────────────────────────────────────────────────────

    @Test
    @DisplayName("findByShortCode returns from MongoDB when Redis misses")
    void findByShortCode_redisMiss_returnsFromMongo() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("url:abc")).thenReturn(null);

        Url url = buildUrl("abc", "https://mongo.com", "user@test.com");
        when(urlRepository.findByShortCode("abc")).thenReturn(Optional.of(url));

        Url result = urlService.findByShortCode("abc");

        assertThat(result).isNotNull();
        assertThat(result.getOriginalUrl()).isEqualTo("https://mongo.com");
    }

    @Test
    @DisplayName("findByShortCode returns null when code doesn't exist")
    void findByShortCode_notFound_returnsNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(urlRepository.findByShortCode("missing")).thenReturn(Optional.empty());

        Url result = urlService.findByShortCode("missing");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("findByShortCode falls back to MongoDB when Redis throws")
    void findByShortCode_redisThrows_fallsBackToMongo() {
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis down"));

        Url url = buildUrl("abc", "https://fallback.com", "user@test.com");
        when(urlRepository.findByShortCode("abc")).thenReturn(Optional.of(url));

        Url result = urlService.findByShortCode("abc");

        assertThat(result).isNotNull();
        assertThat(result.getOriginalUrl()).isEqualTo("https://fallback.com");
    }

    // ── incrementClickCount ──────────────────────────────────────────────────

    @Test
    @DisplayName("incrementClickCount increases click count by 1")
    void incrementClickCount_existingUrl_incrementsCount() {
        Url url = buildUrl("abc", "https://example.com", "user@test.com");
        url.setClickCount(5L);
        when(urlRepository.findByShortCode("abc")).thenReturn(Optional.of(url));
        when(urlRepository.save(any(Url.class))).thenAnswer(inv -> inv.getArgument(0));

        urlService.incrementClickCount("abc");

        assertThat(url.getClickCount()).isEqualTo(6L);
        verify(urlRepository).save(url);
    }

    @Test
    @DisplayName("incrementClickCount does nothing when short code not found")
    void incrementClickCount_notFound_doesNotThrow() {
        when(urlRepository.findByShortCode("nope")).thenReturn(Optional.empty());

        // Should not throw
        urlService.incrementClickCount("nope");

        verify(urlRepository, never()).save(any());
    }

    // ── deleteUrl ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteUrl removes URL when owner matches")
    void deleteUrl_ownerMatches_deletesUrl() {
        Url url = buildUrl("abc", "https://example.com", "owner@test.com");
        url.setId("id1");
        when(urlRepository.findById("id1")).thenReturn(Optional.of(url));

        urlService.deleteUrl("id1", "owner@test.com");

        verify(urlRepository).delete(url);
    }

    @Test
    @DisplayName("deleteUrl throws when user is not the owner")
    void deleteUrl_notOwner_throwsException() {
        Url url = buildUrl("abc", "https://example.com", "owner@test.com");
        url.setId("id1");
        when(urlRepository.findById("id1")).thenReturn(Optional.of(url));

        assertThatThrownBy(() -> urlService.deleteUrl("id1", "other@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("permission");
    }

    @Test
    @DisplayName("deleteUrl throws when URL id not found")
    void deleteUrl_notFound_throwsException() {
        when(urlRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> urlService.deleteUrl("missing", "user@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    // ── countByUser ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("countByUser returns correct count from repository")
    void countByUser_returnsRepositoryCount() {
        when(urlRepository.countByCreatedBy("user@test.com")).thenReturn(7L);

        assertThat(urlService.countByUser("user@test.com")).isEqualTo(7L);
    }

    // ── sumClicksByUser ──────────────────────────────────────────────────────

    @Test
    @DisplayName("sumClicksByUser sums all click counts for user")
    void sumClicksByUser_sumsAllClicks() {
        Url u1 = buildUrl("a", "https://a.com", "u@test.com");
        u1.setClickCount(3L);
        Url u2 = buildUrl("b", "https://b.com", "u@test.com");
        u2.setClickCount(7L);

        when(urlRepository.findByCreatedBy("u@test.com")).thenReturn(List.of(u1, u2));

        assertThat(urlService.sumClicksByUser("u@test.com")).isEqualTo(10L);
    }

    @Test
    @DisplayName("sumClicksByUser returns 0 when user has no URLs")
    void sumClicksByUser_noUrls_returnsZero() {
        when(urlRepository.findByCreatedBy("u@test.com")).thenReturn(List.of());

        assertThat(urlService.sumClicksByUser("u@test.com")).isEqualTo(0L);
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
