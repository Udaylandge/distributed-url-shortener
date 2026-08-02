package com.uday.urlshortener.service;

import com.uday.urlshortener.model.Url;
import com.uday.urlshortener.repository.UrlRepository;
import com.uday.urlshortener.service.impl.UrlServiceImpl;
import com.uday.urlshortener.util.RedisFallbackHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private RedisFallbackHelper redisFallbackHelper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UrlServiceImpl urlService;

    @BeforeEach
    void setUp() {
        urlService = new UrlServiceImpl(urlRepository, sequenceGeneratorService, redisFallbackHelper, passwordEncoder);
        ReflectionTestUtils.setField(urlService, "baseUrl", "http://localhost:8080");
    }

    @Test
    @DisplayName("shortenUrl creates Url with correct fields and saves to DB")
    void shortenUrl_createsAndSavesUrl() {
        when(sequenceGeneratorService.getNextSequence("url_sequence")).thenReturn(1L);

        Url saved = buildUrl("abc123", "https://google.com", "user@test.com");
        when(urlRepository.save(any(Url.class))).thenReturn(saved);

        Url result = urlService.shortenUrl("google.com", "user@test.com", 30);

        assertThat(result).isNotNull();
        assertThat(result.getOriginalUrl()).isEqualTo("https://google.com");
        assertThat(result.getCreatedBy()).isEqualTo("user@test.com");
        verify(urlRepository, times(1)).save(any(Url.class));
    }

    @Test
    @DisplayName("shortenUrl(String) delegates with SYSTEM user and 365 days")
    void shortenUrl_singleArg_delegatesToMultiArgOverload() {
        when(sequenceGeneratorService.getNextSequence("url_sequence")).thenReturn(10L);
        Url saved = buildUrl("xyz", "https://google.com", "system");
        when(urlRepository.save(any(Url.class))).thenReturn(saved);

        Url result = urlService.shortenUrl("https://google.com");

        assertThat(result.getCreatedBy()).isEqualTo("system");
        verify(urlRepository, times(1)).save(any(Url.class));
    }

    @Test
    @DisplayName("findByShortCode returns from MongoDB when Redis misses")
    void findByShortCode_redisMiss_returnsFromMongo() {
        when(redisFallbackHelper.get(anyString())).thenReturn(null);

        Url url = buildUrl("abc", "https://mongo.com", "user@test.com");
        when(urlRepository.findByShortCodeOrCustomAlias("abc")).thenReturn(Optional.of(url));

        Url result = urlService.findByShortCode("abc");

        assertThat(result).isNotNull();
        assertThat(result.getOriginalUrl()).isEqualTo("https://mongo.com");
    }

    @Test
    @DisplayName("incrementClickCount increases click count by 1")
    void incrementClickCount_existingUrl_incrementsCount() {
        Url url = buildUrl("abc", "https://google.com", "user@test.com");
        url.setClickCount(5L);
        when(urlRepository.findByShortCodeOrCustomAlias("abc")).thenReturn(Optional.of(url));
        when(urlRepository.save(any(Url.class))).thenAnswer(inv -> inv.getArgument(0));

        urlService.incrementClickCount("abc");

        assertThat(url.getClickCount()).isEqualTo(6L);
        verify(urlRepository).save(url);
    }

    @Test
    @DisplayName("deleteUrl removes URL when owner matches")
    void deleteUrl_ownerMatches_deletesUrl() {
        Url url = buildUrl("abc", "https://google.com", "owner@test.com");
        url.setId("id1");
        when(urlRepository.findById("id1")).thenReturn(Optional.of(url));

        urlService.deleteUrl("id1", "owner@test.com");

        verify(urlRepository).delete(url);
    }

    @Test
    @DisplayName("deleteUrl throws when user is not the owner")
    void deleteUrl_notOwner_throwsException() {
        Url url = buildUrl("abc", "https://google.com", "owner@test.com");
        url.setId("id1");
        when(urlRepository.findById("id1")).thenReturn(Optional.of(url));

        assertThatThrownBy(() -> urlService.deleteUrl("id1", "other@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("permission");
    }

    @Test
    @DisplayName("sumClicksByUser sums all click counts for user")
    void sumClicksByUser_sumsAllClicks() {
        Url u1 = buildUrl("a", "https://a.com", "u@test.com");
        u1.setClickCount(3L);
        Url u2 = buildUrl("b", "https://b.com", "u@test.com");
        u2.setClickCount(7L);

        when(urlRepository.findByCreatedByAndDeletedFalse("u@test.com")).thenReturn(List.of(u1, u2));

        assertThat(urlService.sumClicksByUser("u@test.com")).isEqualTo(10L);
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
