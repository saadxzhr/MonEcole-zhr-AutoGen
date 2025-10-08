package com.myschool.backend.Security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collection;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String redirectUrl = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> switch (role) {
                    case "ROLE_Direction" -> "/req/direction";
                    case "ROLE_Secretariat" -> "/req/secretariat";
                    default -> role.startsWith("ROLE_Formateur") ? "/req/formateur" : "/default";
                })
                .findFirst()
                .orElse("/default");

        response.sendRedirect(redirectUrl);
    }
}
