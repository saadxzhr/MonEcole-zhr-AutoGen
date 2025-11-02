package com.szschoolmanager.auth.service;

import com.szschoolmanager.auth.dto.AuthRequestDTO;
import com.szschoolmanager.auth.dto.TokensDTO;
import com.szschoolmanager.auth.exception.AccountLockedException;
import com.szschoolmanager.auth.model.Utilisateur;
import com.szschoolmanager.auth.repository.UtilisateurRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final UtilisateurRepository utilisateurRepository;
  private final RedisLoginAttemptService loginAttemptService;
  private final MeterRegistry meterRegistry;

  public TokensDTO authenticate(AuthRequestDTO request) {
    String username = request.getUsername();

    // 🔒 Vérifie si le compte est verrouillé
    if (loginAttemptService.isLocked(username)) {
      meterRegistry.counter("auth.locked.user").increment();
      log.warn("🚫 Tentative refusée : compte verrouillé -> {}", username);
      throw new AccountLockedException("Compte verrouillé pour 30 minutes après échecs multiples.");
    }

    try {
      // ✅ Authentification Spring Security
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(username, request.getPassword()));

      // ✅ Réinitialise les tentatives si succès
      loginAttemptService.resetAttempts(username);
      meterRegistry.counter("auth.success").increment();
      log.info("✅ Connexion réussie pour {}", username);

      Utilisateur user =
          utilisateurRepository
              .findByUsername(username)
              .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));

      // ✅ Génère les tokens JWT
      return jwtService.generateTokens(user);

    } catch (BadCredentialsException ex) {
      // ❌ Échec : incrémente le compteur Redis
      long count = loginAttemptService.recordFailedAttemptAndGet(username);
      meterRegistry.counter("auth.failed").increment();

      if (count >= 5) {
        log.warn("🔒 Compte {} verrouillé après {} échecs", username, count);
        throw new AccountLockedException("Trop de tentatives échouées. Compte verrouillé 30 min.");
      }

      log.warn("❌ Échec de connexion user={} (tentative {}/{})", username, count, 5);
      throw new BadCredentialsException("Nom d'utilisateur ou mot de passe invalide.");
    }
  }
}
