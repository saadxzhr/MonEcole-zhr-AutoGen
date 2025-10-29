package com.szschoolmanager.auth.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ✅ RateLimitFilter — Bucket4j 8.10.1
 * - 10 requêtes / minute par IP + URI
 * - Aucun import ou API déprécié
 * - Compatible Spring Boot 3.5.6, Java 17
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int REQUEST_LIMIT = 10;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

    // cache mémoire thread-safe
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(REQUEST_LIMIT)
                .refillIntervally(REQUEST_LIMIT, REFILL_PERIOD)
                .build();

        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String key = request.getRemoteAddr() + ":" + request.getRequestURI();
        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("⚠️ Rate limit exceeded for {} on {}", request.getRemoteAddr(), request.getRequestURI());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // HTTP 429
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
                {
                  "status": "error",
                  "message": "Too many requests. Please wait before retrying."
                }
            """);
        }
    }
}
