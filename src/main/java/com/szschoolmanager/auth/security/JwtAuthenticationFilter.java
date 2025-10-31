package com.szschoolmanager.auth.security;

import com.szschoolmanager.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Filtre JWT : secure, minimal, sans fallback DB automatique.
 * - Vérifie signature/expiration via JwtService
 * - Vérifie blacklist Redis (fail-closed configurable)
 * - Reconstruit Authentication depuis les claims "authorities"
 *
 * IMPORTANT : Les rôles restent dynamiques côté génération du token.
 */
@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    
    private final JwtService jwtService;
    private final StringRedisTemplate stringRedisTemplate;
    private final MeterRegistry meterRegistry;

    
    private static final List<String> WHITELIST = List.of(
            "/api/v1/auth",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html",
            "/.well-known/jwks.json",
            "/webjars/"
    );

    private static final String BLACKLIST_PREFIX = "blacklist:access:";
    private static final int MAX_TOKEN_LENGTH = 8192;

    @Value("${app.redis.fail-closed:true}")
    private boolean failClosed;


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 1) Whitelist rapide
        if (WHITELIST.stream().anyMatch(path::startsWith)) {
            chain.doFilter(request, response);
            return;
        }

        // 2) Header Authorization
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // 3) Extraction & validations basiques
        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            meterRegistry.counter("jwt.auth.failure").increment();
            meterRegistry.counter("jwt.auth.empty").increment();
            log.warn("JWT token manquant — ip={}", request.getRemoteAddr());
            respondError(response, HttpStatus.BAD_REQUEST, "Missing token");
            return;
        }

        if (token.length() > MAX_TOKEN_LENGTH) {
            meterRegistry.counter("jwt.auth.failure").increment();
            meterRegistry.counter("jwt.auth.oversized").increment();
            log.warn("JWT trop long ({} octets) — ip={}", token.length(), request.getRemoteAddr());
            respondError(response, HttpStatus.BAD_REQUEST, "Token too large");
            return;
        }

        // 4) Vérification cryptographique (signature / expiration)
        Jws<Claims> jws;
        try {
            jws = jwtService.parseToken(token);
        } catch (Exception e) {
            meterRegistry.counter("jwt.auth.failure").increment();
            log.warn("JWT invalide/expiré : {}", e.getMessage());
            respondError(response, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        Claims claims = jws.getBody();
        String username = claims.getSubject();
        // vérifier la blacklist
        String jti = claims.getId();
        if (jti != null && jwtService.isAccessTokenBlacklisted(jti)) {
            log.warn("Access token JTI {} is blacklisted", jti);
            respondError(response, HttpStatus.UNAUTHORIZED, "Access token revoked");
            return;
        }

        //  Blacklist (revoked tokens) — atomic check via Redis key existence
        if (jti != null) {
            try {
                if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(BLACKLIST_PREFIX + jti))) {
                    meterRegistry.counter("jwt.auth.blacklisted").increment();
                    log.info("Token blacklisté jti={} ip={}", jti, request.getRemoteAddr());
                    respondError(response, HttpStatus.UNAUTHORIZED, "Token revoked");
                    return;
                }
            } catch (RedisConnectionFailureException ex) {
                // Fail-closed configurable : en prod on préfère bloquer pour éviter bypass
                log.error("Redis indisponible (failClosed={}) pour jti={} ip={}", failClosed, jti, request.getRemoteAddr(), ex);
                meterRegistry.counter("jwt.auth.redis.unavailable").increment();
                if (failClosed) {
                    respondError(response, HttpStatus.SERVICE_UNAVAILABLE, "Authentication temporarily unavailable");
                    return;
                } else {
                    log.warn("Redis indisponible mais failClosed=false → mode DEV, on continue");
                }
            } catch (Exception ex) {
                log.error("Erreur vérif blacklist jti={} ip={}", jti, request.getRemoteAddr(), ex);
                meterRegistry.counter("jwt.auth.redis.error").increment();
                if (failClosed) {
                    respondError(response, HttpStatus.SERVICE_UNAVAILABLE, "Auth service temporarily unavailable");
                    return;
                }
            }
        }

        // 6) Reconstruction du SecurityContext **depuis le JWT uniquement**
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                List<GrantedAuthority> authorities = extractAuthorities(claims);

                if (authorities.isEmpty()) {
                    // Avertissement — cela signifie que le token a été généré sans authorities
                    meterRegistry.counter("jwt.auth.db.fallback").increment();
                    log.warn("Aucun rôle dans le JWT pour user={} — vérifier génération du token", username);
                }

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

                meterRegistry.counter("jwt.auth.success").increment();
                log.debug("Auth réussie user={} ip={}", username, request.getRemoteAddr());
            } catch (Exception e) {
                meterRegistry.counter("jwt.auth.failure").increment();
                log.error("Erreur construction SecurityContext pour user={} : {}", username, e.getMessage(), e);
                respondError(response, HttpStatus.UNAUTHORIZED, "Authentication context error");
                return;
            }
        }

        // Continue la chaîne de filtres
        chain.doFilter(request, response);
    }

    // --- utilitaires ---

    /**
     * Extrait des autorités depuis la claim "authorities" qui doit être un tableau JSON de strings.
     * Retourne une liste vide si absent.
     */
    private List<GrantedAuthority> extractAuthorities(Claims claims) {
        Object raw = claims.get("authorities");
        List<GrantedAuthority> out = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof String s && !s.isBlank()) {
                    out.add(new SimpleGrantedAuthority(s));
                }
            }
        }
        return out;
    }

    private void respondError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"status\":\"error\",\"message\":\"%s\",\"code\":%d}", message, status.value()));
        response.getWriter().flush();
    }
}
