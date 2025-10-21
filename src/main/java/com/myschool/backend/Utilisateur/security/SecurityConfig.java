package com.myschool.backend.Utilisateur.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final UserDetailsService userDetailsService; // DatabaseUserDetailsService bean

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/favicon.ico"
    };

    private static final String[] AUTH_WHITELIST = {
            "/api/v1/auth/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // disable csrf with lambda DSL (non-deprecated)
            .csrf(AbstractHttpConfigurer::disable)

            // cors basic (allow any origin for dev; tighten in prod)
            .cors(cors -> cors.configurationSource(request -> {
                var cfg = new org.springframework.web.cors.CorsConfiguration();
                cfg.setAllowedOrigins(java.util.List.of("*")); // change to your front origin(s)
                cfg.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                cfg.setAllowedHeaders(java.util.List.of("*"));
                cfg.setAllowCredentials(true);
                cfg.setExposedHeaders(java.util.List.of("Authorization"));
                return cfg;
            }))

            // authorize requests
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("*/**").permitAll()
                .requestMatchers(SWAGGER_WHITELIST).permitAll()
                .requestMatchers(AUTH_WHITELIST).permitAll()
                .anyRequest().authenticated()
            )

            // exception handling - send JSON 401 for unauthenticated requests
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(restAuthenticationEntryPoint())
            )

            // stateless session for JWT
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // add jwt filter before username/password filter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // DaoAuthenticationProvider using the provided UserDetailsService and PasswordEncoder.
    // This keeps Authentication logic clear and avoids cycles.
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder) {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    // Expose AuthenticationManager (used in AuthenticationController)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // Return a JSON 401 body instead of default HTML page
    @Bean
    public AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response,
                                 org.springframework.security.core.AuthenticationException authException) throws IOException {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                String message = Objects.requireNonNullElse(authException.getMessage(), "Unauthorized");
                String body = String.format("{\"status\":\"error\",\"message\":\"%s\"}", message.replace("\"", "'"));
                response.getWriter().write(body);
            }
        };
    }
}
