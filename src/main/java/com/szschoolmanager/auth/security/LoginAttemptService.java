package com.szschoolmanager.auth.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple local brute-force protection (per username).
 * Later can be switched to Redis (TTL-based).
 */
@Slf4j
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MS = 30 * 60 * 1000; // 30 minutes

    // username -> (failedAttempts, lockExpiration)
    private final Map<String, LoginAttempt> attempts = new ConcurrentHashMap<>();

    public void recordFailedAttempt(String username) {
        var current = attempts.getOrDefault(username, new LoginAttempt(0, 0));
        int newCount = current.failedAttempts() + 1;
        long lockUntil = current.lockUntil();

        if (newCount >= MAX_ATTEMPTS) {
            lockUntil = Instant.now().toEpochMilli() + LOCK_DURATION_MS;
            log.warn("🔒 Account {} locked for 30 minutes due to too many failed attempts", username);
        }

        attempts.put(username, new LoginAttempt(newCount, lockUntil));
    }

    public void resetAttempts(String username) {
        attempts.remove(username);
    }

    public boolean isLocked(String username) {
        var entry = attempts.get(username);
        if (entry == null) return false;

        if (entry.lockUntil() > Instant.now().toEpochMilli()) {
            return true;
        } else if (entry.lockUntil() != 0) {
            // expired lock -> reset
            attempts.remove(username);
        }
        return false;
    }

    private record LoginAttempt(int failedAttempts, long lockUntil) {}
}
