package com.szschoolmanager.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisHealthIndicator implements HealthIndicator {

    private final StringRedisTemplate redisTemplate;

    public RedisHealthIndicator(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            if ("PONG".equalsIgnoreCase(pong)) {
                return Health.up().withDetail("redis", "connected").build();
            } else {
                log.warn("⚠️ Redis ping failed: {}", pong);
                return Health.down().withDetail("redis", "unresponsive").build();
            }
        } catch (Exception e) {
            log.error("❌ Redis connection failed: {}", e.getMessage());
            return Health.down(e).build();
        }
    }
}
