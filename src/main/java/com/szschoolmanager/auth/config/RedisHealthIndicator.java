package com.szschoolmanager.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

    private final StringRedisTemplate redisTemplate;

    @Override
    public Health health() {
        try {
            // Ping Redis
            var factory = redisTemplate.getConnectionFactory();
            if (factory == null) {
                log.error("❌ RedisConnectionFactory is null — RedisTemplate not initialized");
                return Health.down()
                    .withDetail("redis", "not_initialized")
                    .build();
            }

            String pong = factory.getConnection().ping();

            if ("PONG".equalsIgnoreCase(pong)) {
                // Test lecture/écriture
                String testKey = "health:check:" + System.currentTimeMillis();
                redisTemplate.opsForValue().set(testKey, "ok");
                String value = redisTemplate.opsForValue().get(testKey);
                redisTemplate.delete(testKey);

                if ("ok".equals(value)) {
                    return Health.up()
                        .withDetail("redis", "connected")
                        .withDetail("ping", "PONG")
                        .withDetail("operations", "working")
                        .build();
                } else {
                    log.warn("⚠️ Redis ping OK mais opérations échouées");
                    return Health.down() // ✅ Use predefined methods
                        .withDetail("redis", "degraded")
                        .withDetail("reason", "operations_failed")
                        .build();
                }
            } else {
                log.warn("⚠️ Redis ping inattendu: {}", pong);
                return Health.down()
                    .withDetail("redis", "unresponsive")
                    .withDetail("ping_response", pong)
                    .build();
            }

        } catch (RedisConnectionFailureException e) {
            log.error("❌ Échec de connexion Redis: {}", e.getMessage());
            return Health.down()
                .withDetail("redis", "connection_failed")
                .withDetail("error", e.getMessage())
                .build();

        } catch (Exception e) {
            log.error("❌ Échec du health check Redis: {}", e.getMessage(), e);
            return Health.down(e)
                .withDetail("redis", "error")
                .build();
        }
    }
}
