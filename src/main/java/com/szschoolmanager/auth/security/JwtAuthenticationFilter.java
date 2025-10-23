package com.szschoolmanager.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final ApplicationContext context;

  private static final List<String> WHITELIST =
      List.of(
          "/api/v1/auth",
          "/v3/api-docs",
          "/swagger-ui",
          "/swagger-ui.html",
          "/swagger-resources",
          "/webjars",
          "/favicon.ico");

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();
    if (WHITELIST.stream().anyMatch(path::startsWith)) {
      filterChain.doFilter(request, response);
      return;
    }

    final String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    final String token = authHeader.substring(7);
    String username;
    try {
      username = jwtService.getUsernameFromToken(token);
    } catch (Exception e) {
      filterChain.doFilter(request, response);
      return;
    }

    if (!jwtService.isTokenValid(token, username)) {
      filterChain.doFilter(request, response);
      return;
    }

    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetailsService userDetailsService = context.getBean(UserDetailsService.class);
      var userDetails = userDetailsService.loadUserByUsername(username);

      String role = jwtService.getRoleFromToken(token);
      List<GrantedAuthority> authorities =
          List.of(
              new SimpleGrantedAuthority("ROLE_" + (role == null ? "USER" : role.toUpperCase())));

      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    filterChain.doFilter(request, response);
  }
}
