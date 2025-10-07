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
@AllArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private final MyAppUserService appUserService;
    
    
    @Bean
    public UserDetailsService userDetailsService(){
        return appUserService;
    }
    
    //initialisation des infos de l'utilisateur et encodage du mot de passe
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(appUserService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
        
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
       //Exécutez logique personnalisée d'authentification apres connexion reussie.
        return http
            .formLogin(httpForm ->{
                httpForm.loginPage("/login").permitAll();
                httpForm.successHandler(new CustomAuthenticationSuccessHandler());
            })

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")   
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
    
            
            .authorizeHttpRequests(registry -> {
                registry.requestMatchers("/login", "/css/**", "/js/**", "*/**").permitAll();
                registry.requestMatchers("/direction/**").hasRole("Direction");
                registry.requestMatchers("/secretariat/**").hasRole("Secretariat");
                registry.requestMatchers("/generate-recurring/**").hasAnyRole("Direction", "Secretariat");
                registry.requestMatchers("/formateur/**").hasAnyAuthority("ROLE_Formateur_Vacataire", "ROLE_Formateur_Permanent");

                
                registry.anyRequest().authenticated();
                
            })
            .build();

        
    }
    
   
}