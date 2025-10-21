package com.myschool.backend.Utilisateur.security;

import com.myschool.backend.Utilisateur.Model.Utilisateur;
import com.myschool.backend.Utilisateur.service.UtilisateurQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UtilisateurQueryService utilisateurQueryService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur u = utilisateurQueryService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));

        return User.builder()
                .username(u.getUsername())
                .password(u.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().toUpperCase())))
                .build();
    }
}
