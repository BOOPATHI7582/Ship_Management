package com.company.exportplatform.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Dependency-free fixed-window rate limiter for sensitive public endpoints.
 * Buckets live in memory (per instance), keyed as {@code purpose:key}.
 */
@Component
@Slf4j
public class RateLimitService {

    private record Window(AtomicLong count, long resetAtMillis) {
    }

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Value("${app.security.rate-limit.contact:5}")
    private int contactLimit;

    @Value("${app.security.rate-limit.register:10}")
    private int registerLimit;

    @Value("${app.security.rate-limit.forgot-password:5}")
    private int forgotPasswordLimit;

    @Value("${app.security.rate-limit.window-minutes:15}")
    private long windowMinutes;

    public void checkContact(String key) {
        check("contact:" + key, contactLimit);
    }

    public void checkRegister(String key) {
        check("register:" + key, registerLimit);
    }

    public void checkForgotPassword(String key) {
        check("forgot-password:" + key, forgotPasswordLimit);
    }

    void check(String bucketKey, int limit) {
        long now = System.currentTimeMillis();
        long windowMillis = windowMinutes * 60_000L;
        Window window = windows.compute(bucketKey, (k, existing) -> {
            if (existing == null || existing.resetAtMillis() <= now) {
                return new Window(new AtomicLong(0), now + windowMillis);
            }
            return existing;
        });
        if (window.count().incrementAndGet() > limit) {
            log.warn("Rate limit exceeded for {}", bucketKey);
            throw new com.company.exportplatform.exception.TooManyRequestsException(
                    "Too many requests. Please try again later.");
        }
        // opportunistic cleanup
        if (windows.size() > 1000) {
            windows.entrySet().removeIf(e -> e.getValue().resetAtMillis() <= now);
        }
    }
}
