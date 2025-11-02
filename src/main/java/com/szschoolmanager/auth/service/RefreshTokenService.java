package com.szschoolmanager.auth.service;

import com.szschoolmanager.auth.model.RefreshToken;
import com.szschoolmanager.auth.model.Utilisateur;
import com.szschoolmanager.auth.repository.RefreshTokenRepository;
import com.szschoolmanager.auth.repository.UtilisateurRepository;
import com.szschoolmanager.exception.BusinessValidationException;
import jakarta.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Complete RefreshTokenService (production-ready). - Stores hashed tokens in DB and returns raw
 * token to client (detached object). - Limits active sessions. - Rotate with reuse detection
 * (committed revocation via REQUIRES_NEW). - Simple hex SHA-256 for token hashing (readable).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final UtilisateurRepository utilisateurRepository;
  private final StringRedisTemplate redisTemplate;
  private final Environment environment;


  

  @Value("${jwt.refresh-days:7}") 
  private int refreshTokenDays;

  @Value("${app.security.max-active-sessions:3}") 
  private int maxActiveSessions;

  @Value("${jwt.expiry-grace-seconds:30}")
  private int expiryGraceSeconds;



  // ----------------- CREATE -----------------
  @Transactional
  public RefreshToken createRefreshToken(
      Utilisateur user, String userAgent, String ipAddress, String accessJti) {
    Objects.requireNonNull(user, "Utilisateur ne peut pas être nul");

    try {
      // ✅ Re-fetch user to ensure it's fully initialized (no lazy proxy)
      Utilisateur fullUser =
          utilisateurRepository
              .findById(user.getId())
              .orElseThrow(() -> new BusinessValidationException("Utilisateur introuvable"));

      // 🔒 1. Lock active tokens to ensure atomic behavior
      List<RefreshToken> activeTokens = refreshTokenRepository.findActiveTokensForUpdate(fullUser);

      // 🧹 2. Enforce max active sessions
      if (activeTokens.size() >= maxActiveSessions) {
        int revokeCount = activeTokens.size() - maxActiveSessions + 1;
        activeTokens.stream()
            .sorted(Comparator.comparing(RefreshToken::getCreatedAt))
            .limit(revokeCount)
            .forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAllAndFlush(activeTokens);
        log.info("Revoked {} old refresh tokens for user {}", revokeCount, user.getUsername());
      }

      // 🔑 3. Generate secure token (raw + hashed)
      String raw = generateRawToken();
      String hashed = hashToken(raw);

      // 🧱 4. Build entity (hash only)
      RefreshToken entity =
          RefreshToken.builder()
              .utilisateur(fullUser)
              .token(hashed)
              .jti(UUID.randomUUID().toString())
              .createdAt(LocalDateTime.now())
              .expiresAt(LocalDateTime.now().plusDays(refreshTokenDays))
              .revoked(false)
              .reused(false)
              .userAgent(userAgent)
              .ipAddress(ipAddress)
              .accessJti(accessJti)
              .build();

      RefreshToken saved = refreshTokenRepository.save(entity); // one persist call only

      // 🎁 5. Return detached copy with raw token for frontend
      return RefreshToken.builder()
          .id(saved.getId())
          .utilisateur(fullUser)
          .token(raw) // raw never persisted
          .createdAt(saved.getCreatedAt())
          .expiresAt(saved.getExpiresAt())
          .revoked(saved.isRevoked())
          .reused(saved.isReused())
          .userAgent(saved.getUserAgent())
          .ipAddress(saved.getIpAddress())
          .jti(saved.getJti())
          .accessJti(accessJti)
          .build();

    } catch (Exception e) {
      log.error(
          "❌ Error creating refresh token for user {}: {}",
          user != null ? user.getUsername() : "unknown",
          e.getMessage(),
          e);
      throw new BusinessValidationException("Erreur lors de la création du refresh token");
    }
  }

  // ----------------- VALIDATE -----------------
  @Transactional(readOnly = true)
  public RefreshToken validateRefreshToken(String rawToken) {
    if (rawToken == null || rawToken.isBlank()) {
      throw new BusinessValidationException("Aucun refresh token fourni");
    }

    String hashed = hashToken(rawToken);
    RefreshToken rt =
        refreshTokenRepository
            .findLightByToken(hashed)
            .orElseThrow(() -> new BusinessValidationException("Token invalide"));

    if (rt.isExpiredWithGrace(expiryGraceSeconds)) throw new BusinessValidationException("Token expiré (grace period dépassé)");
    if (rt.isRevoked()) throw new BusinessValidationException("Token révoqué");
    return rt;
  }

  // ----------------- ROTATE -----------------
  /**
   * Rotate refresh token: - If presented token is active -> revoke it and issue new one. - If
   * presented token is already revoked -> mark reused, commit revocations for user (REQUIRES_NEW),
   * then throw.
   */
  @Transactional(noRollbackFor = BusinessValidationException.class)
  public RefreshToken rotateRefreshToken(
      String presentedRaw, String userAgent, String ipAddress, String accessJti) {
    if (presentedRaw == null || presentedRaw.isBlank()) {
      throw new BusinessValidationException("Aucun refresh token fourni");
    }
     String hashed = hashToken(presentedRaw);

    String lockKey = "rotate:lock:" + hashed;
    Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(5));
    if (!Boolean.TRUE.equals(acquired)) {
        throw new BusinessValidationException("Concurrent rotation detected");
    }

    try {

       
        RefreshToken stored =
            refreshTokenRepository
                .findDetailedByToken(hashed)
                .orElseThrow(() -> new BusinessValidationException("Token invalide"));
        // This line forces Hibernate to initialize the lazy proxy before leaving the transaction
        // boundary
        stored.getUtilisateur().getUsername();

        // normal rotation
        if (!stored.isRevoked()) {
          stored.setRevoked(true);
          refreshTokenRepository.saveAndFlush(stored);
          return createRefreshToken(stored.getUtilisateur(), userAgent, ipAddress, accessJti);
        }

        // reuse detected
        if (!stored.isReused()) {
          stored.setReused(true);
          refreshTokenRepository.saveAndFlush(stored);
        }
        revokeAllActiveSessionsForUserCommitted(stored.getUtilisateur().getId());

        throw new BusinessValidationException("Refresh token reuse detected - all sessions revoked");

      } finally {
      try {
          redisTemplate.delete(lockKey);
      } catch (Exception ex) {
          log.warn("Unable to delete rotate lock {}: {}", lockKey, ex.getMessage());
      }
    }
  }






  @Transactional
  public void saveAccessJtiLink(Long refreshId, String accessJti) {
    refreshTokenRepository.updateAccessJti(refreshId, accessJti);
  }







  // ----------------- REVOKE ONE -----------------
  @Transactional
  public void revokeRefreshToken(String rawToken) {
    if (rawToken == null || rawToken.isBlank()) {
      log.warn("Attempt to revoke blank token");
      return;
    }
    String hashed = hashToken(rawToken);
    Optional<RefreshToken> optional = refreshTokenRepository.findDetailedByToken(hashed);
    optional.ifPresent(
        rt -> {
          if (!rt.isRevoked()) {
            rt.setRevoked(true);
            refreshTokenRepository.saveAndFlush(rt);
            log.info("Revoked refresh token id={}", rt.getId());
          } else {
            log.info("Refresh token was already revoked id={}", rt.getId());
          }
        });
  }

  // ----------------- REVOKE ALL FOR USER (committed helper) -----------------
  /**
   * Commits the bulk revocation in its own transaction so the revoke persists even if caller rolls
   * back. Uses the repository bulk query (revokeAllByUserId).
   */
  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public int revokeAllActiveSessionsForUserCommitted(Long userId) {
     // Ne lance en async que si profil prod
    List<String> profiles = List.of(environment.getActiveProfiles());
    if (!profiles.contains("prod")) {
        log.debug("Async revocation skipped (non-prod profile)");
        return refreshTokenRepository.revokeAllByUserId(userId);
    }

    int count = refreshTokenRepository.revokeAllByUserId(userId);
    log.info("Committed revocation of {} refresh tokens for user id={}", count, userId);
    return count;
  }

  // ----------------- REVOKE ALL FOR USER (non-committed) -----------------
  @Transactional
  public void revokeAllForUser(Utilisateur user) {
    if (user == null) return;
    List<RefreshToken> tokens =
        refreshTokenRepository.findAllByUtilisateurIdAndRevokedFalse(user.getId());
    for (RefreshToken rt : tokens) {
      rt.setRevoked(true);
      refreshTokenRepository.save(rt);
    }
    refreshTokenRepository.flush();
    log.info("Revoked {} refresh tokens for user {}", tokens.size(), user.getUsername());
  }

  // ----------------- HELPERS -----------------
  private static String generateRawToken() {
    SecureRandom random = new SecureRandom();
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return Hex.encodeHexString(bytes);
  }

  private static final String HMAC_ALGO = "HmacSHA256";
  private static final Charset UTF8 = StandardCharsets.UTF_8;

  // this secret should come from your config (never hard-coded)
  @Value("${app.security.token-hash-secret}")
  private String tokenHashSecret;

  @PostConstruct
  void validateSecret() {
    if (tokenHashSecret == null || tokenHashSecret.length() < 32) {
      throw new IllegalStateException("Token hash secret must be at least 32 characters long");
    }
  }

  private String hashToken(String rawToken) {
    try {
      Mac hmac = Mac.getInstance(HMAC_ALGO);
      hmac.init(new SecretKeySpec(tokenHashSecret.getBytes(UTF8), HMAC_ALGO));
      byte[] digest = hmac.doFinal(rawToken.getBytes(UTF8));
      StringBuilder hex = new StringBuilder(digest.length * 2);
      for (byte b : digest) hex.append(String.format("%02x", b));
      return hex.toString();
    } catch (Exception e) {
      throw new IllegalStateException("Erreur lors du hachage sécurisé du refresh token", e);
    }
  }

  // private static String shorten(String s) {
  //     if (s == null) return "";
  //     return s.length() <= 200 ? s : s.substring(0, 200);
  // }

  @Scheduled(cron = "0 0 2 * * ?") // 2 AM daily
  @Transactional
  public void cleanupExpiredTokens() {
    try {
      int deleted = refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
      log.info("🧹 Deleted {} expired refresh tokens", deleted);
    } catch (Exception e) {
      log.error("Error while cleaning up expired refresh tokens", e);
    }
  }

  public long getRefreshExpirationSeconds() {
    return Duration.ofDays(refreshTokenDays).getSeconds();
  }

  @Transactional(readOnly = true)
  public RefreshToken findByRawToken(String rawToken) {
    if (rawToken == null || rawToken.isBlank()) {
      return null;
    }
    String hashed = hashToken(rawToken);
    return refreshTokenRepository.findRawByToken(hashed).orElse(null);
  }
}
