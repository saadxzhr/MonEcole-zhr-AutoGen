package com.szschoolmanager.auth.service;

import com.szschoolmanager.auth.model.RefreshToken;
import com.szschoolmanager.auth.repository.RefreshTokenRepository;
import com.szschoolmanager.auth.repository.UtilisateurRepository;
import com.szschoolmanager.auth.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final UtilisateurRepository utilisateurRepository;
  private final JwtService jwtService;

  private static final int MAX_ACTIVE_SESSIONS = 5;
  private static final int REFRESH_TOKEN_EXPIRATION_DAYS = 7;

  @Transactional
  public RefreshToken createRefreshToken(String username, String userAgent, String ip) {
    var user = utilisateurRepository.findByUsername(username).orElseThrow();
    var active = refreshTokenRepository.findAllByUtilisateurIdAndRevokedFalse(user.getId());
    if (active.size() >= MAX_ACTIVE_SESSIONS) {
      active.stream().min(Comparator.comparing(RefreshToken::getCreatedAt)).ifPresent(old -> {
        old.setRevoked(true);
        refreshTokenRepository.save(old);
      });
    }

    RefreshToken rt = RefreshToken.builder()
        .utilisateur(user)
        .token(UUID.randomUUID().toString())
        .jti(UUID.randomUUID().toString())
        .createdAt(LocalDateTime.now())
        .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRATION_DAYS))
        .revoked(false)
        .reused(false)
        .userAgent(userAgent != null ? userAgent : "unknown")
        .ipAddress(ip != null ? ip : "unknown")
        .build();

    return refreshTokenRepository.save(rt);
  }

  @Transactional
  public RefreshToken validateRefreshToken(String token) {
    var rt = refreshTokenRepository.findByToken(token).orElseThrow(() -> new RuntimeException("Token invalide"));
    if (rt.isExpired() || rt.isRevoked()) throw new RuntimeException("Token expiré ou révoqué");
    return rt;
  }

  @Transactional
  public TokensDTO rotateRefreshToken(String presentedToken, String userAgent, String ip) {
    var stored = refreshTokenRepository.findByToken(presentedToken).orElseThrow();
    if (stored.isRevoked()) {
      stored.setReused(true);
      refreshTokenRepository.save(stored);
      revokeAllForUser(stored.getUtilisateur().getId());
      throw new RuntimeException("Refresh token reuse detected");
    }

    stored.setRevoked(true);
    refreshTokenRepository.save(stored);

    var newRt = createRefreshToken(stored.getUtilisateur().getUsername(), userAgent, ip);
    String newAccess = jwtService.generateAccessToken(stored.getUtilisateur());

    return new TokensDTO(newAccess, newRt.getToken());
  }

  @Transactional
  public void revokeRefreshToken(String token) {
    refreshTokenRepository.findByToken(token).ifPresent(t -> { t.setRevoked(true); refreshTokenRepository.save(t); });
  }

  @Transactional
  public void revokeAllForUser(Long userId) {
    refreshTokenRepository.findAllByUtilisateurIdAndRevokedFalse(userId)
        .forEach(t -> { t.setRevoked(true); refreshTokenRepository.save(t); });
  }

  // Optional helper: revoke expired tokens for a user
  @Transactional
  public void revokeExpiredTokens(Long userId) {
    refreshTokenRepository.findAllByUtilisateurId(userId).stream()
        .filter(RefreshToken::isExpired)
        .forEach(t -> { t.setRevoked(true); refreshTokenRepository.save(t); });
  }

  // Simple record to return tokens
  public static record TokensDTO(String accessToken, String refreshToken) {}
}
