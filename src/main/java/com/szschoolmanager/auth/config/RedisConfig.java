package com.szschoolmanager.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.lettuce.core.api.StatefulConnection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;

@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.timeout:2000}")
    private long timeout;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisHost, redisPort);
        
        if (redisPassword != null && !redisPassword.isBlank()) {
            redisConfig.setPassword(redisPassword);
        }

        // ✅ Let Spring Boot auto-configure the pool (uses defaults)
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofMillis(timeout))
            .shutdownTimeout(Duration.ofSeconds(5))
            .build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig, clientConfig);
        factory.afterPropertiesSet();
        log.info("🔗 Redis connection factory configured: {}:{}", redisHost, redisPort);
        return factory;
    }




    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        log.info("✅ StringRedisTemplate configured");
        return template;
    }

    /**
     * Atomic rate limit script using Lua.
     * Guarantees INCR + EXPIRE happen atomically (no race condition).
     * 
     * Returns current count after increment.
     */
    @Bean
    public RedisScript<Long> rateLimitScript() {
        String luaScript = """
            local key = KEYS[1]
            local limit = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            
            local current = redis.call('INCR', key)
            
            -- Set TTL only on first increment (prevents race condition)
            if current == 1 then
                redis.call('EXPIRE', key, window)
            end
            
            return current
            """;
        
        RedisScript<Long> script = RedisScript.of(luaScript, Long.class);
        log.info("📜 Rate limit Lua script loaded");
        return script;
    }

    /**
     * Alternative: Sliding window script for more accurate rate limiting.
     * Uses sorted sets to track individual requests.
     */
    @Bean
    public RedisScript<Long> slidingWindowScript() {
        String luaScript = """
            local key = KEYS[1]
            local now = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local limit = tonumber(ARGV[3])
            
            local clearBefore = now - window
            
            -- Remove old entries outside the window
            redis.call('ZREMRANGEBYSCORE', key, 0, clearBefore)
            
            -- Count current requests in window
            local count = redis.call('ZCARD', key)
            
            if count < limit then
                -- Add new request with current timestamp as score
                redis.call('ZADD', key, now, now)
                redis.call('EXPIRE', key, window)
                return count + 1
            end
            
            return -1
            """;
        
        RedisScript<Long> script = RedisScript.of(luaScript, Long.class);
        log.info("📊 Sliding window script loaded");
        return script;
    }
}