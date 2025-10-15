package com.myschool.backend.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.myschool.backend.Service.MyAppUserService;

import lombok.AllArgsConstructor;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Plus de champ final → pas de circularité
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(MyAppUserService userService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .formLogin(form -> form
                .loginPage("/login").permitAll()
                .successHandler(new CustomAuthenticationSuccessHandler())
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/login", "/css/**", "/js/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll();
                auth.requestMatchers("/direction/**").hasRole("Direction");
                auth.requestMatchers("/secretariat/**").hasRole("Secretariat");
                auth.requestMatchers("/generate-recurring/**").hasAnyRole("Direction", "Secretariat");
                auth.requestMatchers("/formateur/**").hasAnyAuthority("ROLE_Formateur_Vacataire", "ROLE_Formateur_Permanent");
                auth.anyRequest().authenticated();
            })
            .build();
    }
}
