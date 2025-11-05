package com.szschoolmanager.auth.service;

import com.szschoolmanager.auth.model.Utilisateur;
import com.szschoolmanager.auth.repository.UtilisateurRepository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

  private final UtilisateurRepository utilisateurRepository;

  @Override
  @Cacheable(value = "userDetails", key = "#username", cacheManager = "redisCacheManager")
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Utilisateur u =
        utilisateurRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));

    return User.builder()
        .username(u.getUsername())
        .password(u.getPassword())
        .authorities(
            List.of(
                new SimpleGrantedAuthority("ROLE_" + u.getRole().toUpperCase())))
        .build();
  }

  @CacheEvict(value = "userDetails", key = "#username")
  public void invalidateUserCache(String username) {}
}
