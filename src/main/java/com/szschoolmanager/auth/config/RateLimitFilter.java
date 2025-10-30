package com.szschoolmanager.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.time.Duration;


import static com.szschoolmanager.auth.config.RateLimitConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> rateLimitScript;

    // Paths exempt from rate limiting
    private static final Set<String> WHITELIST_PATHS = Set.of(
        "/actuator/health",
        "/v3/api-docs",
        "/swagger-ui",
        "/favicon.ico"
    );

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip whitelisted paths
        if (isWhitelisted(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Determine rate limit based on endpoint
        RateLimitConfig config = getRateLimitConfig(path);
        
        String clientIp = extractClientIP(request);
        String redisKey = RATE_LIMIT_PREFIX + clientIp + ":" + normalizeUri(path);

        Long currentCount;
        try {
            // Atomic increment + TTL via Lua script
            currentCount = redisTemplate.execute(
                rateLimitScript,
                Collections.singletonList(redisKey),
                String.valueOf(config.limit()),
                String.valueOf(config.window().getSeconds())
            );

            if (currentCount == null) {
                // Script execution failed but no exception - should never happen
                log.error("❌ Rate limit script returned null for key={}", redisKey);
                rejectRequest(response, HttpStatus.SERVICE_UNAVAILABLE, 
                    "Rate limiting service unavailable");
                return;
            }

        } catch (RedisConnectionFailureException ex) {
            // Fail-closed: Redis unavailable = deny request
            log.error("❌ Redis connection failed - denying request from {} to {} (fail-closed)", 
                clientIp, path, ex);
            rejectRequest(response, HttpStatus.SERVICE_UNAVAILABLE, 
                "Service temporarily unavailable");
            return;

        } catch (Exception ex) {
            // Unexpected error - fail-closed for security
            log.error("❌ Unexpected rate limit error for {} - denying request (fail-closed)", 
                clientIp, ex);
            rejectRequest(response, HttpStatus.SERVICE_UNAVAILABLE, 
                "Service temporarily unavailable");
            return;
        }

        // Check if limit exceeded
        if (currentCount <= config.limit()) {
            // Add rate limit headers for client awareness
            response.setHeader("X-RateLimit-Limit", String.valueOf(config.limit()));
            response.setHeader("X-RateLimit-Remaining", 
                String.valueOf(Math.max(0, config.limit() - currentCount)));
            response.setHeader("X-RateLimit-Reset", 
                String.valueOf(System.currentTimeMillis() / 1000 + config.window().getSeconds()));
            
            filterChain.doFilter(request, response);
        } else {
            log.warn("⚠️ Rate limit exceeded: ip={} path={} count={}/{}", 
                clientIp, path, currentCount, config.limit());
            
            response.setHeader("X-RateLimit-Limit", String.valueOf(config.limit()));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("Retry-After", String.valueOf(config.window().getSeconds()));
            
            rejectRequest(response, HttpStatus.TOO_MANY_REQUESTS, 
                "Too many requests. Please retry after " + config.window().getSeconds() + " seconds.");
        }
    }

    /**
     * Extract real client IP from request headers (proxy-aware).
     * Prevents IP spoofing by prioritizing trusted headers.
     */
    private String extractClientIP(HttpServletRequest request) {
        // Try X-Real-IP first (single IP, most reliable from reverse proxy)
        String ip = request.getHeader(X_REAL_IP);
        if (isValidIp(ip)) {
            return ip;
        }

        // Try X-Forwarded-For (comma-separated list, take first/leftmost = client)
        String xForwardedFor = request.getHeader(X_FORWARDED_FOR);
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            String clientIp = xForwardedFor.split(",")[0].trim();
            if (isValidIp(clientIp)) {
                return clientIp;
            }
        }

        // Fallback to remote address
        return request.getRemoteAddr();
    }

    private boolean isValidIp(String ip) {
        return ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip);
    }

    private boolean isWhitelisted(String path) {
        return WHITELIST_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Normalize URI to prevent cache key explosion.
     * Example: /api/users/123 -> /api/users/{id}
     */
    private String normalizeUri(String uri) {
        // Remove query parameters
        int queryIndex = uri.indexOf('?');
        if (queryIndex > 0) {
            uri = uri.substring(0, queryIndex);
        }

        // Replace numeric IDs with placeholder to avoid key explosion
        // /api/users/123 -> /api/users/{id}
        uri = uri.replaceAll("/\\d+", "/{id}");
        
        return uri;
    }

    /**
     * Determine rate limit based on endpoint sensitivity.
     */
    private RateLimitConfig getRateLimitConfig(String path) {
        if (path.startsWith("/api/v1/auth/login")) {
            return new RateLimitConfig(LOGIN_LIMIT, LOGIN_WINDOW);
        }
        if (path.startsWith("/api/v1/auth/refresh")) {
            return new RateLimitConfig(REFRESH_LIMIT, REFRESH_WINDOW);
        }
        // Default for all other API endpoints
        return new RateLimitConfig(API_LIMIT, API_WINDOW);
    }

    private void rejectRequest(HttpServletResponse response, HttpStatus status, String message) 
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format(
            "{\"status\":\"error\",\"message\":\"%s\",\"code\":%d}",
            message.replace("\"", "\\\""),
            status.value()
        );
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    /**
     * Rate limit configuration holder.
     */
    private record RateLimitConfig(int limit, Duration window) {}
}