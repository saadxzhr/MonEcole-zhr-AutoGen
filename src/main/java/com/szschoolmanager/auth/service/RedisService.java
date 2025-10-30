package com.szschoolmanager.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    /**
     * Vérifie si un token (par son jti) est dans la blacklist.
     * - Si Redis répond, renvoie true/false.
     * - Si Redis indisponible ou erreur, lance RedisUnavailableException.
     */
    public boolean isTokenBlacklisted(String tokenId) {
        try {
            String key = "blacklist:access:" + tokenId;
            Boolean present = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(present);
        } catch (RedisConnectionFailureException ex) {
            log.error("❌ Redis connection failure while checking blacklist for tokenId={}", tokenId, ex);
            throw new RedisUnavailableException("Redis unavailable", ex);
        } catch (Exception ex) {
            log.error("❌ Unexpected Redis error while checking blacklist for tokenId={}", tokenId, ex);
            // On considère toutes les erreurs comme critique -> fail-closed
            throw new RedisUnavailableException("Redis error", ex);
        }
    }

    /**
     * Méthode utilitaire : marque un token dans la blacklist (usage ailleurs)
     */
    public void blacklistToken(String tokenId) {
        try {
            String key = "blacklist:access:" + tokenId;
            // Exemple: marquer avec TTL (1 jour) — ajuste si nécessaire
            redisTemplate.opsForValue().set(key, "1");
            redisTemplate.expire(key, java.time.Duration.ofDays(1));
        } catch (Exception ex) {
            log.error("❌ Failed to write blacklist key for tokenId={}", tokenId, ex);
            throw new RedisUnavailableException("Redis unavailable", ex);
        }
    }
}
