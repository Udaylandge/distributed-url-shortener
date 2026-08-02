package com.uday.urlshortener.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Utility helper that safely executes Redis operations with automatic error catching.
 * If Redis is down or unavailable, operations degrade gracefully without crashing the application.
 */
@Component
public class RedisFallbackHelper {

    private static final Logger log = LoggerFactory.getLogger(RedisFallbackHelper.class);
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisFallbackHelper(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public <T> T safeGet(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.warn("Redis unavailable during read operation: {}", e.getMessage());
            return null;
        }
    }

    public void safeExecute(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            log.warn("Redis unavailable during write operation: {}", e.getMessage());
        }
    }

    public Object get(String key) {
        return safeGet(() -> redisTemplate.opsForValue().get(key));
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        safeExecute(() -> redisTemplate.opsForValue().set(key, value, timeout, unit));
    }

    public void delete(String key) {
        safeExecute(() -> redisTemplate.delete(key));
    }
}
