// package com.myschool.backend.Security;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;

// /**
//  * Modern Spring Security Configuration - Zero Warnings
//  * Uses Spring Boot auto-configuration for authentication
//  */
// @Configuration
// @EnableWebSecurity
// @EnableMethodSecurity(prePostEnabled = true)
// public class SecurityConfig {

//     /**
//      * Password encoder bean
//      * Spring Boot automatically uses this for authentication
//      */
//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
//     }

//     /**
//      * Security filter chain - Your exact configuration
//      * No AuthenticationProvider bean needed - Spring Boot handles it!
//      */
//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//         return http
//             .formLogin(form -> form
//                 .loginPage("/login").permitAll()
//                 .successHandler(new CustomAuthenticationSuccessHandler())
//             )
//             .logout(logout -> logout
//                 .logoutUrl("/logout")
//                 .logoutSuccessUrl("/login?logout")
//                 .invalidateHttpSession(true)
//                 .deleteCookies("JSESSIONID")
//                 .permitAll()
//             )
//             .authorizeHttpRequests(auth -> {
//                 auth.requestMatchers("/login", "/css/**", "/js/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();
//                 auth.requestMatchers("/direction/**").hasRole("Direction");
//                 auth.requestMatchers("/secretariat/**").hasRole("Secretariat");
//                 auth.requestMatchers("/generate-recurring/**").hasAnyRole("Direction", "Secretariat");
//                 auth.requestMatchers("/formateur/**").hasAnyAuthority("ROLE_Formateur_Vacataire", "ROLE_Formateur_Permanent");
//                 auth.anyRequest().authenticated();
//             })
//             .build();
//     }
// }