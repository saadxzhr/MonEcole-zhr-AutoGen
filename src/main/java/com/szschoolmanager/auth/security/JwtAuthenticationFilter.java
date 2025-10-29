package com.szschoolmanager.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.szschoolmanager.auth.service.JwtService;

import io.micrometer.core.instrument.MeterRegistry;

import java.io.IOException;
import java.util.List;

@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final List<String> WHITELIST = List.of(
      "/api/v1/auth", "/v3/api-docs", "/swagger-ui", "/swagger-ui.html", "/.well-known/jwks.json"
  );

  private static final int MAX_TOKEN_LENGTH = 8192;

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final StringRedisTemplate stringRedisTemplate;
  private final MeterRegistry meterRegistry;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain chain) throws ServletException, IOException {

    String path = request.getRequestURI();
    if (WHITELIST.stream().anyMatch(path::startsWith)) {
      chain.doFilter(request, response);
      return;
    }

    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      chain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7).trim();
    if (token.isEmpty() || token.length() > MAX_TOKEN_LENGTH) {
      meterRegistry.counter("jwt.auth.failure").increment();
      log.warn("JWT token malformed or too long from ip={}", request.getRemoteAddr());
      chain.doFilter(request, response);
      return;
    }

    Jws<Claims> jws;
    try {
      jws = jwtService.parseToken(token);
    } catch (Exception e) {
      meterRegistry.counter("jwt.auth.failure").increment();
      log.debug("Token parse failed: {}", e.getMessage());
      chain.doFilter(request, response);
      return;
    }

    Claims claims = jws.getBody();
    String username = claims.getSubject();
    String jti = claims.getId();
    String role = claims.get("role", String.class);

    if (jti != null) {
      try {
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey("blacklist:access:" + jti))) {
          meterRegistry.counter("jwt.auth.blacklisted").increment();
          log.info("Rejected blacklisted token jti={} ip={}", jti, request.getRemoteAddr());
          chain.doFilter(request, response);
          return;
        }
      } catch (Exception ex) {
        log.error("Redis check failed (allowing auth to proceed): {}", ex.getMessage());
        throw new AuthenticationServiceException("Auth service unavailable");
      }
    }

    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      try {
        var userDetails = userDetailsService.loadUserByUsername(username);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(
            "ROLE_" + (role == null ? "USER" : role.toUpperCase())
        ));
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
        meterRegistry.counter("jwt.auth.success").increment();
        log.info("Auth success user={} ip={} kid={}", username, request.getRemoteAddr(),
            jws.getHeader().get("kid"));
      } catch (Exception e) {
        meterRegistry.counter("jwt.auth.failure").increment();
        log.warn("Failed to set security context for user {} : {}", username, e.getMessage());
      }
    }

    chain.doFilter(request, response);
  }
}
