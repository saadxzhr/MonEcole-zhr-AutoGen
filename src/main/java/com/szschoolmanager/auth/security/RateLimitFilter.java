// package com.szschoolmanager.auth.security;

// import io.github.bucket4j.Bandwidth;
// import io.github.bucket4j.Bucket;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// import org.springframework.data.redis.core.StringRedisTemplate;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.lang.NonNull;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import java.io.IOException;
// import java.time.Duration;
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;

// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class RateLimitFilter extends OncePerRequestFilter {

//     private static final int REQUEST_LIMIT = 5;
//     private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

//     private final StringRedisTemplate redisTemplate;

//     @Override
//     protected void doFilterInternal(@NonNull HttpServletRequest request,
//                                     @NonNull HttpServletResponse response,
//                                     @NonNull FilterChain filterChain)
//             throws ServletException, IOException {

    
//                 String clientIp = request.getRemoteAddr();
//     String uri = request.getRequestURI();
//     String redisKey = "rl:" + clientIp + ":" + uri; // clé Redis partagée entre nœuds

//     // incr atomique
//     Long current;
//     try {
//         current = redisTemplate.opsForValue().increment(redisKey);
//         if (current != null && current == 1L) {
//             // première incr → définir TTL correspondant à la fenêtre
//             redisTemplate.expire(redisKey, REFILL_PERIOD);
//         }
//     } catch (Exception ex) {
//         log.error("Redis unavailable for rate limiting - denying request (fail-closed).", ex);
//         response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
//         response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//         response.getWriter().write("{\"status\":\"error\",\"message\":\"Service temporarily unavailable\"}");
//         return;
//     }

//     // contrôle de la limite
//     if (current != null && current <= REQUEST_LIMIT) {
//         filterChain.doFilter(request, response);
//     } else {
//         log.warn("⚠️ Rate limit exceeded for {} on {}", clientIp, uri);
//         response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // HTTP 429
//         response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//         response.getWriter().write("{\"status\":\"error\",\"message\":\"Too many requests. Please wait before retrying.\"}");
//     }

//     }
// }
