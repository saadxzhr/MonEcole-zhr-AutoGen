package com.szschoolmanager.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.*;

import com.szschoolmanager.auth.service.JwtService;

import org.springframework.context.annotation.Bean;

import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  private final RateLimitFilter rateLimitFilter;
  private final UserDetailsService userDetailsService;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtService jwtService;

  private static final String[] SWAGGER_WHITELIST = {
      "/v3/api-docs/**",
      "/swagger-ui/**",
      "/swagger-ui.html",
      "/swagger-resources/**",
      "/webjars/**",
      "/favicon.ico"
  };

  private static final String[] AUTH_WHITELIST = {"/api/v1/auth/**"};

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(AUTH_WHITELIST).permitAll()
            .requestMatchers(SWAGGER_WHITELIST).permitAll()
            .anyRequest().authenticated())
        .exceptionHandling(ex -> ex.authenticationEntryPoint(restAuthenticationEntryPoint()))
        .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    // Add rate limiter before auth filter
    http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


    return http.build();
  }

  @Bean
  public UrlBasedCorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOrigins(List.of("http://localhost:5173")); // set production origins explicitly
    cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
    cfg.setAllowCredentials(true);
    cfg.setExposedHeaders(List.of("Authorization"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
  }

  @Bean
  @SuppressWarnings("deprecation")
  public DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder encoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(encoder);
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  public AuthenticationEntryPoint restAuthenticationEntryPoint() {
    return (request, response, authException) -> {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      String message = Objects.requireNonNullElse(authException.getMessage(), "Unauthorized");
      String body = String.format("{\"status\":\"error\",\"message\":\"%s\"}", message.replace("\"", "'"));
      response.getWriter().write(body);
    };
  }
}
