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

    @Query("{ '$or': [ { 'shortCode': ?0 }, { 'customAlias': ?0 } ], 'deleted': { $ne: true } }")
    Optional<Url> findByShortCodeOrCustomAlias(String identifier);

    Optional<Url> findByShortCode(String shortCode);

    Optional<Url> findByCustomAlias(String customAlias);

    boolean existsByShortCode(String shortCode);

    boolean existsByCustomAlias(String customAlias);

    // Active (non-deleted) URLs by owner — matches deleted: false OR deleted missing
    @Query("{ 'createdBy': ?0, 'deleted': { $ne: true } }")
    List<Url> findByCreatedByAndDeletedFalse(String email);

    @Query("{ 'createdBy': ?0, 'deleted': { $ne: true } }")
    Page<Url> findByCreatedByAndDeletedFalse(String email, Pageable pageable);

    // Soft deleted URLs by owner (Trash bin)
    @Query("{ 'createdBy': ?0, 'deleted': true }")
    List<Url> findByCreatedByAndDeletedTrue(String email);

    @Query("{ 'createdBy': ?0, 'deleted': true }")
    Page<Url> findByCreatedByAndDeletedTrue(String email, Pageable pageable);

    // Search within non-deleted user URLs
    @Query("{ 'createdBy': ?0, 'deleted': { $ne: true }, '$or': [ {'originalUrl': {$regex: ?1, $options: 'i'}}, {'shortCode': {$regex: ?1, $options: 'i'}}, {'customAlias': {$regex: ?1, $options: 'i'}} ] }")
    Page<Url> searchByCreatedByAndDeletedFalse(String email, String keyword, Pageable pageable);

    // Count queries
    @Query(value = "{ 'createdBy': ?0, 'active': ?1, 'deleted': { $ne: true } }", count = true)
    long countByCreatedByAndActiveAndDeletedFalse(String email, boolean active);

    @Query(value = "{ 'createdBy': ?0, 'deleted': { $ne: true } }", count = true)
    long countByCreatedByAndDeletedFalse(String email);

    @Query(value = "{ 'createdBy': ?0, 'createdAt': { $gte: ?1 }, 'deleted': { $ne: true } }", count = true)
    long countByCreatedByAndCreatedAtAfterAndDeletedFalse(String email, LocalDateTime since);

    @Query("{ 'createdBy': ?0, 'deleted': { $ne: true } }")
    List<Url> findTop5ByCreatedByAndDeletedFalseOrderByClickCountDesc(String email);

    @Query("{ 'createdBy': ?0, 'deleted': { $ne: true } }")
    List<Url> findTop10ByCreatedByAndDeletedFalseOrderByCreatedAtDesc(String email);

    @Query("{ 'deleted': { $ne: true } }")
    List<Url> findTop10ByDeletedFalseOrderByCreatedAtDesc();

    @Query(value = "{ 'createdBy': ?0, 'expiryDate': { $lt: ?1 }, 'deleted': { $ne: true } }", count = true)
    long countByCreatedByAndExpiryDateBeforeAndDeletedFalse(String email, LocalDateTime now);

    @Query("{ 'deleted': { $ne: true } }")
    Page<Url> findByDeletedFalse(Pageable pageable);
}