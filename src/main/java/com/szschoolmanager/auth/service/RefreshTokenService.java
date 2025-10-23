package com.szschoolmanager.auth.service;

import com.szschoolmanager.auth.model.RefreshToken;
import com.szschoolmanager.auth.model.Utilisateur;
import com.szschoolmanager.auth.repository.RefreshTokenRepository;
import com.szschoolmanager.auth.repository.UtilisateurRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final UtilisateurRepository utilisateurRepository;
  private static final int MAX_ACTIVE_SESSIONS = 5;

  private static final int REFRESH_TOKEN_EXPIRATION_DAYS = 7;

  @Transactional
  public RefreshToken createRefreshToken(String username, String userAgent, String ip) {
    Utilisateur user =
        utilisateurRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

    // Révoquer anciens tokens
    // Only revoke tokens older than X days OR limit to max 5 active sessions


        List<RefreshToken> activeSessions = refreshTokenRepository
            .findAllByUtilisateurIdAndRevokedFalse(user.getId());

        if (activeSessions.size() >= MAX_ACTIVE_SESSIONS) {
            // Revoke oldest session
            activeSessions.stream()
                .min(Comparator.comparing(RefreshToken::getCreatedAt))
                .ifPresent(oldest -> {
                    oldest.setRevoked(true);
                    refreshTokenRepository.save(oldest);
                });
        }

    RefreshToken refreshToken =
        RefreshToken.builder()
            .utilisateur(user)
            .token(UUID.randomUUID().toString())
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRATION_DAYS))
            .revoked(false)
            .userAgent(userAgent)
            .ipAddress(ip)
            .build();

    return refreshTokenRepository.save(refreshToken);
  }

  @Transactional(readOnly = true)
  public RefreshToken validateRefreshToken(String token) {
    RefreshToken refreshToken =
        refreshTokenRepository
            .findByToken(token)
            .orElseThrow(() -> new RuntimeException("Token invalide"));

    if (refreshToken.isExpired() || refreshToken.isRevoked()) {
      throw new RuntimeException("Token expiré ou révoqué");
    }
    return refreshToken;
  }

  @Transactional
  public void revokeRefreshToken(String token) {
    refreshTokenRepository
        .findByToken(token)
        .ifPresent(
            t -> {
              t.setRevoked(true);
              refreshTokenRepository.save(t);
            });
  }

  @Transactional
  public void revokeAllForUser(Long userId) {
    refreshTokenRepository
        .findAllByUtilisateurIdAndRevokedFalse(userId)
        .forEach(
            t -> {
              t.setRevoked(true);
              refreshTokenRepository.save(t);
            });
  }
}
