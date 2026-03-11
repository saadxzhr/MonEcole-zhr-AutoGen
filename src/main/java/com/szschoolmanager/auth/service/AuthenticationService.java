package com.szschoolmanager.auth.service;

import com.szschoolmanager.auth.dto.AuthRequestDTO;
import com.szschoolmanager.auth.dto.AuthResponseDTO;
import com.szschoolmanager.auth.dto.TokensDTO;
import com.szschoolmanager.auth.dto.AccessTokenResult;
import com.szschoolmanager.auth.model.RefreshToken;
import com.szschoolmanager.auth.model.Utilisateur;
import com.szschoolmanager.shared.dto.ResponseDTO;
import com.szschoolmanager.shared.exception.AccountLockedException;
import com.szschoolmanager.shared.exception.BusinessValidationException;


import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final RedisLoginAttemptService loginAttemptService;
  private final TokenOrchestratorService tokenOrchestratorService;
  private final JwtService jwtService;
  private final UtilisateurService utilisateurService;
  private final RefreshTokenService refreshTokenService;
  private final PasswordEncoder passwordEncoder;
  private final Environment environment;


  @Value("${app.dev:true}") // default true for development; set false in prod
  private boolean devMode;

  @Value("${jwt.refresh-days:7}")
  private int refreshDays;

  

  @PostConstruct
    private void checkDevModeSafety() {
        String[] profiles = environment.getActiveProfiles();
        for (String profile : profiles) {
            if ("prod".equalsIgnoreCase(profile) && devMode) {
                throw new IllegalStateException(
                    "⚠️ app.dev must be set to false when running with 'prod' profile (safety check)");
            }
        }
    }


@Transactional
  public ResponseEntity<ResponseDTO<AuthResponseDTO>> login(
      AuthRequestDTO dto,
      HttpServletRequest request,
      HttpServletResponse response) {

      String username = dto.getUsername();
      if (loginAttemptService.isLocked(username)) {
          log.warn("Tentative de connexion sur un compte verrouillé: {}", username);
          throw new AccountLockedException("Compte verrouillé pour 30 minutes après plusieurs échecs");
      }


      Utilisateur utilisateur =
          utilisateurService
              .findByUsername(dto.getUsername())
              .orElseThrow(() -> new BadCredentialsException("Utilisateur introuvable"));

      boolean encoded = utilisateur.getPassword().startsWith("$2a$");
      boolean matches =
          encoded
              ? passwordEncoder.matches(dto.getPassword(), utilisateur.getPassword())
              : dto.getPassword().equals(utilisateur.getPassword());

      

      if (!matches) {
        loginAttemptService.recordFailedAttemptAndGet(username);
        throw new BadCredentialsException("Identifiants invalides");
      }

      if (!encoded) {
        utilisateurService.upgradePasswordIfNeeded(dto.getUsername(), dto.getPassword());
      }
      loginAttemptService.resetAttempts(username);
      

      // 3️⃣ Génération sécurisée des tokens via JwtService
      TokensDTO tokens = jwtService.generateTokens(utilisateur, request);
      String accessToken = tokens.getAccessToken();
      String rawRefresh = tokens.getRefreshToken();

      // ⚠️ In production, app.dev=false must be set to prevent refresh token exposure in JSON
      // In dev mode we return refresh token in JSON. In prod, set HttpOnly cookie instead.
      if (!devMode) {
        ResponseCookie cookie =
            ResponseCookie.from("refreshToken", rawRefresh)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofSeconds(tokens.getRefreshExpiresIn()))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
      }

      // 5️⃣ Détermination du tableau de bord selon le rôle utilisateur
      String redirectUrl =
          switch (utilisateur.getRole().toUpperCase()) {
            case "ADMIN" -> "/dashboard/admin";
            case "DIRECTION" -> "/dashboard/direction";
            case "SECRETARIAT" -> "/dashboard/secretariat";
            default -> "/dashboard/formateur";
          };

      // 6️Construction de la réponse complète pour le front
      AuthResponseDTO body =
          AuthResponseDTO.builder()
              .token(accessToken)
              .refreshToken(devMode ? rawRefresh : null) // only in dev
              .username(utilisateur.getUsername())
              .role(utilisateur.getRole())
              .forceChangePassword(utilisateur.getForceChangePassword())
              .redirectUrl(redirectUrl)
              .build();

      return ResponseEntity.ok(ResponseDTO.success("Authentification réussie", body));
  }



  public ResponseEntity<ResponseDTO<AuthResponseDTO>> refreshToken(
      @RequestHeader(value = "Refresh-Token", required = false) String headerRefresh,
      HttpServletRequest request,
      HttpServletResponse response) {

      // 1️⃣ Retrieve presented refresh token (header or cookie)
      String presented = getPresentedToken(headerRefresh, request);
      if (presented == null || presented.isBlank())
        throw new BusinessValidationException("Aucun refresh token fourni");

      // 1️⃣ Validate RT (to get user safely)
      // Utilisateur user = refreshTokenService.validateRefreshToken(presented).getUtilisateur();

      // 2️⃣ Rotate (detect reuse first)
      RefreshToken newRt =
          tokenOrchestratorService.rotateWithAccessHandling(
              presented, request.getHeader("User-Agent"), getClientIP(request), null);

      // 3️⃣ Generate new access token only if rotation succeeded
        AccessTokenResult access = jwtService.generateAccessToken(newRt.getUtilisateur());
        String newAccessToken = access.token();
        String newAccessJti = access.jti();

        // 4️⃣ Update the new refresh token with this access JTI (sync link)
      newRt.setAccessJti(newAccessJti);
      refreshTokenService.saveAccessJtiLink(newRt.getId(), newAccessJti);

      // 4️⃣ Return refresh cookie in production mode
      if (!devMode) {
        ResponseCookie cookie =
            ResponseCookie.from("refreshToken", newRt.getToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(refreshDays))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
      }

      // 5️⃣ Build response body
      AuthResponseDTO body =
          new AuthResponseDTO(
              newAccessToken,
              devMode ? newRt.getToken() : null,
              newRt.getUtilisateur().getUsername(),
              newRt.getUtilisateur().getRole(),
              false,
              null);
      return ResponseEntity.ok(ResponseDTO.success("Token régénéré avec succès", body));

  }

  private String getPresentedToken(String headerRefresh, HttpServletRequest request) {
    String presented = headerRefresh;
    if (presented == null || presented.isBlank()) {
      var cookies = request.getCookies();
      if (cookies != null) {
        for (var c : cookies) {
          if ("refreshToken".equals(c.getName())
              && c.getValue() != null
              && !c.getValue().isBlank()) {
            presented = c.getValue();
            break;
          }
        }
      }
    }
    return presented;
  }



public ResponseEntity<ResponseDTO<Void>> logout(
      @RequestHeader(value = "Refresh-Token", required = false) String refreshTokenHeader,
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
      HttpServletRequest request,
      HttpServletResponse servletResponse) {

    // 1) Determine presented refresh token (raw) from header or cookie
    String presented = refreshTokenHeader;
    if (presented == null || presented.isBlank()) {
      if (request.getCookies() != null) {
        for (var c : request.getCookies()) {
          if ("refreshToken".equals(c.getName())
              && c.getValue() != null
              && !c.getValue().isBlank()) {
            presented = c.getValue();
            break;
          }
        }
      }
    }

    // 2) Revoke refresh token (hashing + lookup done inside service)
    if (presented != null && !presented.isBlank()) {
      refreshTokenService.revokeRefreshToken(presented);
    } else {
      // instruct client to clear cookie if any (best effort)
      ResponseCookie cookie =
          ResponseCookie.from("refreshToken", "")
              .httpOnly(true)
              .secure(true)
              .sameSite("Strict")
              .path("/api/v1/auth")
              .maxAge(Duration.ZERO)
              .build();
      servletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // 3) Blacklist access token jti (if provided)
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      String accessToken = authorizationHeader.substring(7).trim();
        try {
            Claims claims = jwtService.validateAccessToken(accessToken);

            String jti = claims.getId();
            if (jti != null && !jti.isBlank()) {

                Instant exp = claims.getExpiration().toInstant();
                Instant now = Instant.now();
                Duration ttl = Duration.between(now, exp);

                if (!ttl.isNegative() && !ttl.isZero()) {
                    jwtService.blacklistAccessTokenJti(jti, ttl);
                }
            }

        } catch (Exception ignored) {
            // ignore validation errors
        }

    }

    return ResponseEntity.ok(ResponseDTO.success("Déconnexion réussie", null));
  }

  private String getClientIP(HttpServletRequest request) {
    String xf = request.getHeader("X-Forwarded-For");
    return (xf == null || xf.isEmpty()) ? request.getRemoteAddr() : xf.split(",")[0];
  }
}