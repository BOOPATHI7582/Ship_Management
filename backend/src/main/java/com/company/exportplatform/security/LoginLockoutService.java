package com.company.exportplatform.security;

import com.company.exportplatform.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory login lockout: after N failed attempts for an email the account
 * is temporarily locked. State resets on a successful login.
 */
@Component
public class LoginLockoutService {

    private record Failures(AtomicInteger count, LocalDateTime lockedUntil) {
    }

    private final Map<String, Failures> failuresByEmail = new ConcurrentHashMap<>();

    @Value("${app.security.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.security.lock-minutes:15}")
    private long lockMinutes;

    public void checkAllowed(String email) {
        Failures f = failuresByEmail.get(email.toLowerCase());
        if (f != null && f.lockedUntil() != null && LocalDateTime.now().isBefore(f.lockedUntil())) {
            throw new BadRequestException("Too many failed attempts. Try again after "
                    + f.lockedUntil().toLocalTime().withNano(0) + ".");
        }
    }

    public void recordFailure(String email) {
        String key = email.toLowerCase();
        failuresByEmail.compute(key, (k, existing) -> {
            int count = existing == null ? 1 : existing.count().incrementAndGet();
            if (count >= maxFailedAttempts) {
                return new Failures(new AtomicInteger(0), LocalDateTime.now().plusMinutes(lockMinutes));
            }
            return new Failures(new AtomicInteger(count), null);
        });
    }

    public void recordSuccess(String email) {
        failuresByEmail.remove(email.toLowerCase());
    }
}
