package com.szschoolmanager.auth.controller;

import com.szschoolmanager.auth.dto.AuthRequestDTO;
import com.szschoolmanager.auth.dto.AuthResponseDTO;
import com.szschoolmanager.auth.dto.TokensDTO;
import com.szschoolmanager.auth.model.RefreshToken;
import com.szschoolmanager.auth.model.Utilisateur;
import com.szschoolmanager.auth.service.DatabaseUserDetailsService;
import com.szschoolmanager.auth.service.JwtService;
import com.szschoolmanager.auth.service.RefreshTokenService;
import com.szschoolmanager.auth.service.TokenOrchestratorService;
import com.szschoolmanager.auth.service.UtilisateurService;
import com.szschoolmanager.exception.BusinessValidationException;
import com.szschoolmanager.exception.ResponseDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;


@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final TokenOrchestratorService tokenOrchestratorService;
  private final JwtService jwtService;
  private final UtilisateurService utilisateurService;
  private final RefreshTokenService refreshTokenService;
  private final DatabaseUserDetailsService userDetailsService;

  @Value("${app.dev:true}") // default true for development; set false in prod
  private boolean devMode;

  @Value("${jwt.refresh-days:7}")
  private int refreshDays;

  @PostMapping("/login")
  public ResponseEntity<ResponseDTO<AuthResponseDTO>> login(
      @Valid @RequestBody AuthRequestDTO dto, HttpServletRequest request, HttpServletResponse response) {
    try {
      Utilisateur utilisateur = utilisateurService
          .findByUsername(dto.getUsername())
          .orElseThrow(() -> new BadCredentialsException("Utilisateur introuvable"));

      boolean encoded = utilisateur.getPassword().startsWith("$2a$");
      boolean matches = encoded
          ? utilisateurService.passwordEncoder().matches(dto.getPassword(), utilisateur.getPassword())
          : dto.getPassword().equals(utilisateur.getPassword());

      if (!matches) throw new BadCredentialsException("Identifiants invalides");

      if (!encoded) {
        utilisateur.setPassword(utilisateurService.passwordEncoder().encode(dto.getPassword()));
        utilisateurService.save(utilisateur);
        userDetailsService.invalidateUserCache(utilisateur.getUsername());
      }

      // 3️⃣ Génération sécurisée des tokens via JwtService
      TokensDTO tokens = jwtService.generateTokens(utilisateur, request);
      String accessToken = tokens.getAccessToken();
      String rawRefresh = tokens.getRefreshToken();

      // In dev mode we return refresh token in JSON. In prod, set HttpOnly cookie instead.
      if (!devMode) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", rawRefresh) 
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .path("/api/v1/auth")
            .maxAge(Duration.ofSeconds(tokens.getRefreshExpiresIn()))
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
      }




      // 5️⃣ Détermination du tableau de bord selon le rôle utilisateur
      String redirectUrl = switch (utilisateur.getRole().toUpperCase()) {
        case "ADMIN" -> "/dashboard/admin";
        case "DIRECTION" -> "/dashboard/direction";
        case "SECRETARIAT" -> "/dashboard/secretariat";
        default -> "/dashboard/formateur";
      };

      // 6️Construction de la réponse complète pour le front
      AuthResponseDTO body = AuthResponseDTO.builder()
          .token(accessToken)
          .refreshToken(devMode ? rawRefresh : null) // only in dev
          .username(utilisateur.getUsername())
          .role(utilisateur.getRole())
          .forceChangePassword(utilisateur.getForceChangePassword())
          .redirectUrl(redirectUrl)
          .build();

      return ResponseEntity.ok(ResponseDTO.success("Authentification réussie", body));
      } catch (BadCredentialsException ex) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ResponseDTO.error("Identifiants invalides"));
    } catch (Exception ex) {
      log.error("Erreur inattendue pendant le login : {}", ex.getMessage(), ex);
      return ResponseEntity.internalServerError()
          .body(ResponseDTO.error("Erreur interne pendant l’authentification"));
    }
  }


  
@PostMapping("/refresh")
public ResponseEntity<ResponseDTO<AuthResponseDTO>> refreshToken(
    @RequestHeader(value = "Refresh-Token", required = false) String headerRefresh,
    HttpServletRequest request,
    HttpServletResponse response) {

  try {
    // 1️⃣ Retrieve presented refresh token (header or cookie)
    String presented = getPresentedToken(headerRefresh, request);
    if (presented == null || presented.isBlank())
      throw new BusinessValidationException("Aucun refresh token fourni");

    

    // 3️⃣ Generate new access token
    String newAccessToken = jwtService.generateAccessToken(
        refreshTokenService.validateRefreshToken(presented).getUtilisateur()
    );

    String newAccessJti = jwtService.getJti(newAccessToken);

    // 2️⃣ Rotate token (detect reuse and revoke if needed)
     RefreshToken newRt = tokenOrchestratorService.rotateWithAccessHandling(
                presented, request.getHeader("User-Agent"), getClientIP(request), newAccessJti);

    // 4️⃣ Return refresh cookie in production mode
    if (!devMode) {
      ResponseCookie cookie = ResponseCookie.from("refreshToken", newRt.getToken())
          .httpOnly(true)
          .secure(true)
          .sameSite("Strict")
          .path("/api/v1/auth")
          .maxAge(Duration.ofDays(refreshDays))
          .build();
      response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // 5️⃣ Build response body
    AuthResponseDTO body = new AuthResponseDTO(
        newAccessToken,
        devMode ? newRt.getToken() : null,
        newRt.getUtilisateur().getUsername(),
        newRt.getUtilisateur().getRole(),
        false,
        null
    );
    return ResponseEntity.ok(ResponseDTO.success("Token régénéré avec succès", body));

  } catch (BusinessValidationException e) {
    // ✅ Handle token reuse or invalid token gracefully (no rollback)
    log.warn("Erreur lors du refresh token: {}", e.getMessage());
    return ResponseEntity.badRequest().body(ResponseDTO.error(e.getMessage()));
  }
}
private String getPresentedToken(String headerRefresh, HttpServletRequest request) {
  String presented = headerRefresh;
  if (presented == null || presented.isBlank()) {
    var cookies = request.getCookies();
    if (cookies != null) {
      for (var c : cookies) {
        if ("refreshToken".equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
          presented = c.getValue();
          break;
        }
      }
    }
  }
  return presented;
}




  @PostMapping("/logout")
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
          if ("refreshToken".equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
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
      ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
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
        var jws = jwtService.parseToken(accessToken);
        var claims = jws.getBody();
        String jti = claims.getId();
        if (jti != null && !jti.isBlank()) {
          var exp = claims.getExpiration().toInstant();
          var now = Instant.now();
          var ttl = Duration.between(now, exp);
          if (!ttl.isNegative() && !ttl.isZero()) {
            jwtService.blacklistAccessTokenJti(jti, ttl); // atomic set with TTL
          }
        }
      } catch (Exception ignored) {
        // ignore parse errors
      }
    }

    return ResponseEntity.ok(ResponseDTO.success("Déconnexion réussie", null));
  }


  private String getClientIP(HttpServletRequest request) {
    String xf = request.getHeader("X-Forwarded-For");
    return (xf == null || xf.isEmpty()) ? request.getRemoteAddr() : xf.split(",")[0];
  }
}
