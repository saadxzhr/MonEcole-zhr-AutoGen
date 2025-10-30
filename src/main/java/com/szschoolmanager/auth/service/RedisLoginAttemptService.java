package com.szschoolmanager.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;

import java.time.Duration;
import java.util.Collections;

/**
 * Service de gestion des tentatives de connexion basé sur Redis.
 * Utilise un script Lua atomique (INCR + EXPIRE) fourni par RedisConfig.rateLimitScript.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLoginAttemptService {

    private static final String KEY_PREFIX = "login:fail:";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration ACCOUNT_LOCK_DURATION = Duration.ofMinutes(30);
// 30 minutes

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> rateLimitScript; // bean fourni dans RedisConfig
    private final MeterRegistry meterRegistry;

    /**
     * Incrémente le compteur d'échecs pour username et retourne la valeur courante après incrément.
     * Si la valeur dépasse MAX_ATTEMPTS, considère le compte verrouillé.
     */
    public long recordFailedAttemptAndGet(String username) {
        String key = KEY_PREFIX + username;
        try {
            Long current = redisTemplate.execute(
                rateLimitScript,
                Collections.singletonList(key),
                String.valueOf(MAX_FAILED_ATTEMPTS), // limit argument non utilisé dans our script but kept for compatibility
                String.valueOf(ACCOUNT_LOCK_DURATION.getSeconds())
            );
            if (current == null) {
                 // Should never happen unless Redis script fails to return a value
                log.error("rateLimitScript returned null for key={}", key);
                return 0L;
            }
            meterRegistry.counter("auth.login.attempts").increment();
            log.debug("Login failed attempt for {} -> count={}", username, current);
            return current;
        } catch (RedisConnectionFailureException ex) {
            // Redis inaccessible : fail-open for login attempts (do not block legit users)
            meterRegistry.counter("auth.login.redis.unavailable").increment();
            log.error("Redis unavailable while recording login attempt for {} — allowing attempts (fail-open)", username, ex);
            return 0L;
        } catch (DataAccessException ex) {
            meterRegistry.counter("auth.login.redis.error").increment();
            log.error("Redis data access error while recording login attempt for {} — allowing attempts", username, ex);
            return 0L;
        } catch (Exception ex) {
            log.error("Unexpected error while recording login attempt for {} — allowing attempts", username, ex);
            return 0L;
        }
    }

    /**
     * Vérifie si le compte est verrouillé (basé sur la valeur stockée).
     */
    public boolean isLocked(String username) {
        String key = KEY_PREFIX + username;
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) return false;
            try {
                long count = Long.parseLong(value);
                boolean locked = count >= MAX_FAILED_ATTEMPTS;
                if (locked) {
                    meterRegistry.counter("auth.login.locked").increment();
                }
                return locked;
            } catch (NumberFormatException nfe) {
                // clé mal formée -> considérer non verrouillé (et log)
                log.warn("Malformed login attempt value for key={} value={}", key, value);
                return false;
            }
        } catch (RedisConnectionFailureException ex) {
            meterRegistry.counter("auth.login.redis.unavailable").increment();
            log.error("Redis unavailable while checking lock for {} — treat as not locked (fail-open)", username, ex);
            return false;
        } catch (Exception ex) {
            log.error("Unexpected error while checking lock for {} — treat as not locked", username, ex);
            return false;
        }
    }

    /**
     * Reset (supprime) les tentatives après succès de connexion.
     */
    public void resetAttempts(String username) {
        String key = KEY_PREFIX + username;
        try {
            redisTemplate.delete(key);
            meterRegistry.counter("auth.login.reset").increment();
            log.debug("Reset login attempts for {}", username);
        } catch (RedisConnectionFailureException ex) {
            meterRegistry.counter("auth.login.redis.unavailable").increment();
            log.error("Redis unavailable while resetting attempts for {} — nothing done (fail-open)", username, ex);
        } catch (Exception ex) {
            log.error("Unexpected error while resetting attempts for {}", username, ex);
        }
    }
}
