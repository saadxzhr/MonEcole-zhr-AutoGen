package com.szschoolmanager.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.*;

import com.szschoolmanager.auth.config.RateLimitFilter;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

  @RequiredArgsConstructor
  @Configuration
  @EnableWebSecurity
  @EnableMethodSecurity(prePostEnabled = true)
  public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Environment environment;

    // 👉 Externalisation des origines CORS via application.properties
    // Exemple :
    // app.cors.allowed-origins=http://localhost:5173,https://schoolmanager.ma
    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String[] allowedOrigins;

    private static final String[] SWAGGER_WHITELIST = {
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/swagger-resources/**",
        "/webjars/**",
        "/favicon.ico"
    };

    private static final String[] AUTH_WHITELIST = {"/api/v1/auth/**"};

    private static final String[] ACTUATOR_WHITELIST = {"/actuator/**"};


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints publics
                .requestMatchers("/api/v1/auth/**").permitAll()

                // Swagger : accessible uniquement en profil dev
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).access((authentication, context) -> {
                    boolean devProfileActive = Arrays.asList(environment.getActiveProfiles()).contains("dev");
                    return new AuthorizationDecision(devProfileActive);
                })

                // Actuator
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")

                // Erreurs et fichiers statiques
                .requestMatchers("/error", "/favicon.ico").permitAll()

                // Tout le reste = JWT obligatoire
                .anyRequest().authenticated()
            )
            .authenticationProvider(daoAuthenticationProvider(passwordEncoder()))// ✅ correspond à ton bean réel
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // ✅ nom réel de ton filtre
            .build();
    }




    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
      CorsConfiguration cfg = new CorsConfiguration();
      cfg.setAllowedOriginPatterns(Arrays.asList(allowedOrigins));
      cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
      cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
      cfg.setExposedHeaders(List.of("Authorization"));
      cfg.setAllowCredentials(true);
      
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", cfg);
      return source;
    }


    // ✅ Provider utilisant ton DatabaseUserDetailsService
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
