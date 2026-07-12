package com.uday.urlshortener.repository;

import com.uday.urlshortener.model.Url;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UrlRepository extends MongoRepository<Url, String> {

    Optional<Url> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    // URLs by owner
    List<Url> findByCreatedBy(String email);

    Page<Url> findByCreatedBy(String email, Pageable pageable);

    // Search within user's URLs
    @Query("{ 'createdBy': ?0, '$or': [ {'originalUrl': {$regex: ?1, $options: 'i'}}, {'shortCode': {$regex: ?1, $options: 'i'}} ] }")
    Page<Url> searchByCreatedBy(String email, String keyword, Pageable pageable);

    // Active/Expired counting
    long countByCreatedByAndActive(String email, boolean active);

    long countByCreatedBy(String email);

    // Today's URLs
    long countByCreatedByAndCreatedAtAfter(String email, LocalDateTime since);

    // Total clicks sum - use aggregation via service
    List<Url> findByCreatedByAndActiveOrderByCreatedAtDesc(String email, boolean active);

    // Top URLs by clicks
    List<Url> findTop5ByCreatedByOrderByClickCountDesc(String email);

    // Expired URLs (active=true but expiryDate passed)
    @Query("{ 'createdBy': ?0, 'active': true, 'expiryDate': { $lt: ?1 } }")
    List<Url> findExpiredActiveUrls(String email, LocalDateTime now);

    // All URLs for admin
    Page<Url> findAll(Pageable pageable);

    // Recent URLs
    List<Url> findTop10ByCreatedByOrderByCreatedAtDesc(String email);

    List<Url> findTop10ByOrderByCreatedAtDesc();

    // Analytics: expired count
    long countByCreatedByAndExpiryDateBefore(String email, LocalDateTime now);
}