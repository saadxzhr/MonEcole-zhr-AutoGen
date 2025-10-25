package com.szschoolmanager.auth.controller;

import com.szschoolmanager.auth.dto.AuthRequestDTO;
import com.szschoolmanager.auth.dto.AuthResponseDTO;

import com.szschoolmanager.auth.model.RefreshToken;
import com.szschoolmanager.auth.model.Utilisateur;
import com.szschoolmanager.auth.service.JwtService;
import com.szschoolmanager.auth.service.RefreshTokenService;
import com.szschoolmanager.auth.service.UtilisateurService;
import com.szschoolmanager.exception.BusinessValidationException;
import com.szschoolmanager.exception.ResponseDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final JwtService jwtService;
  private final UtilisateurService utilisateurService;
  private final RefreshTokenService refreshTokenService;
  private final RedisTemplate<String, String> redisTemplate;

  @PostMapping("/login")
  public ResponseEntity<ResponseDTO<AuthResponseDTO>> login(
      @Valid @RequestBody AuthRequestDTO dto, HttpServletRequest request) {
    try {
      Utilisateur utilisateur =
          utilisateurService
              .findByUsername(dto.getUsername())
              .orElseThrow(() -> new BadCredentialsException("Utilisateur introuvable"));

      boolean encoded = utilisateur.getPassword().startsWith("$2a$");
      boolean matches =
          encoded
              ? utilisateurService.passwordEncoder().matches(dto.getPassword(), utilisateur.getPassword())
              : dto.getPassword().equals(utilisateur.getPassword());

      if (!matches) throw new BadCredentialsException("Identifiants invalides");

      if (!encoded) {
        utilisateur.setPassword(utilisateurService.passwordEncoder().encode(dto.getPassword()));
        utilisateurService.save(utilisateur);
      }

      String accessToken = jwtService.generateAccessToken(utilisateur);

      RefreshToken refreshToken =
          refreshTokenService.createRefreshToken(
              utilisateur.getUsername(),
              request.getHeader("User-Agent"),
              getClientIP(request));

      String redirectUrl =
          switch (utilisateur.getRole().toUpperCase()) {
            case "ADMIN" -> "/dashboard/admin";
            case "DIRECTION" -> "/dashboard/direction";
            case "SECRETARIAT" -> "/dashboard/secretariat";
            default -> "/dashboard/formateur";
          };

      AuthResponseDTO response =
          AuthResponseDTO.builder()
              .token(accessToken)
              .refreshToken(refreshToken.getToken())
              .username(utilisateur.getUsername())
              .role(utilisateur.getRole())
              .forceChangePassword(utilisateur.getForceChangePassword())
              .redirectUrl(redirectUrl)
              .build();

      return ResponseEntity.ok(ResponseDTO.success("Authentification réussie", response));
    } catch (BadCredentialsException ex) {
      return ResponseEntity.status(401).body(ResponseDTO.error("Identifiants invalides"));
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<ResponseDTO<AuthResponseDTO>> refreshToken(HttpServletRequest request) {
    String refreshTokenHeader = request.getHeader("Refresh-Token");
    if (refreshTokenHeader == null)
      throw new BusinessValidationException("Aucun refresh token fourni");

    // validate (throws if invalid)
    RefreshToken token = refreshTokenService.validateRefreshToken(refreshTokenHeader);

    // rotate: revoke old + create new + generate new access
    var tokens = refreshTokenService.rotateRefreshToken(
        refreshTokenHeader,
        request.getHeader("User-Agent"),
        getClientIP(request));

    AuthResponseDTO response =
        new AuthResponseDTO(
            tokens.accessToken(),
            tokens.refreshToken(),
            token.getUtilisateur().getUsername(),
            token.getUtilisateur().getRole(),
            false,
            null);

    return ResponseEntity.ok(ResponseDTO.success("Token régénéré avec succès", response));
  }

  /**
   * Logout: revoke the provided refresh token, and blacklist current access token jti (if present).
   * - Requires "Refresh-Token" header for refresh revocation.
   * - If Authorization Bearer token present, its jti will be stored in Redis until expiry.
   */
  @PostMapping("/logout")
  public ResponseEntity<ResponseDTO<Void>> logout(
      @RequestHeader(value = "Refresh-Token", required = false) String refreshToken,
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

    // Revoke refresh token if presented
    if (refreshToken != null && !refreshToken.isBlank()) {
      refreshTokenService.revokeRefreshToken(refreshToken);
    }

    // Blacklist access token jti if Authorization header provided as Bearer ...
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      String accessToken = authorizationHeader.substring(7).trim();
      try {
        var jws = jwtService.parseToken(accessToken); // will validate signature/iss/aud/alg/typ/exp
        var claims = jws.getBody();
        String jti = claims.getId();
        if (jti != null && !jti.isBlank()) {
          Instant exp = claims.getExpiration().toInstant();
          Instant now = Instant.now();
          Duration ttl = Duration.between(now, exp);
          if (!ttl.isNegative() && !ttl.isZero()) {
            String key = "blacklist:access:" + jti;
            redisTemplate.opsForValue().set(key, "revoked");
            // expire with duration (works on current Spring Data Redis versions)
            try {
              redisTemplate.expire(key, ttl);
            } catch (Exception ignore) {
              // fallback: try long seconds expiry if duration->seconds available
              try {
                redisTemplate.expire(key, ttl.getSeconds(), java.util.concurrent.TimeUnit.SECONDS);
              } catch (Exception ex) {
                // if expire fails, still ok — entry will be there, may persist; log if required
              }
            }
          }
        }
      } catch (Exception e) {
        // token parse/validation failed — we ignore blacklist (logout still revokes refresh)
      }
    }

    return ResponseEntity.ok(ResponseDTO.success("Déconnexion réussie", null));
  }

  // Helper
  private String getClientIP(HttpServletRequest request) {
    String xf = request.getHeader("X-Forwarded-For");
    if (xf == null || xf.isBlank()) {
      return request.getRemoteAddr();
    }
    return xf.split(",")[0].trim();
  }
}
