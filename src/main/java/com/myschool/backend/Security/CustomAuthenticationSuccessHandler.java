package com.myschool.backend.Security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collection;


//Rederiger selon le 'role'
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                                            
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if ("ROLE_Direction".equals(role)) {
                response.sendRedirect("/req/direction");
                return;
            } else if (role.startsWith("ROLE_Formateur")) {
                response.sendRedirect("/req/formateur");
                return;
            } else if ("ROLE_Secretariat".equals(role)) {
                response.sendRedirect("/req/secretariat");
                return;
            }
        }

        // Default redirect if no match
        response.sendRedirect("/default");
    }
}
