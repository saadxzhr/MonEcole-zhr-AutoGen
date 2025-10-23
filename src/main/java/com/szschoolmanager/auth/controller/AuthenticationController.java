package com.szschoolmanager.auth.controller;

import com.szschoolmanager.auth.dto.AuthRequestDTO;
import com.szschoolmanager.auth.dto.AuthResponseDTO;
import com.szschoolmanager.auth.dto.UtilisateurCreateDTO;
import com.szschoolmanager.auth.model.RefreshToken;
import com.szschoolmanager.auth.model.Utilisateur;
import com.szschoolmanager.auth.security.JwtService;
import com.szschoolmanager.auth.service.RefreshTokenService;
import com.szschoolmanager.auth.service.UtilisateurService;
import com.szschoolmanager.exception.BusinessValidationException;
import com.szschoolmanager.exception.ResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final JwtService jwtService;
  private final UtilisateurService utilisateurService;
  private final RefreshTokenService refreshTokenService;

  // ==========================================================
  // 🔹 LOGIN — Authentifie et génère Access + Refresh Tokens
  // ==========================================================
  @PostMapping("/login")
  public ResponseEntity<ResponseDTO<AuthResponseDTO>> login(
      @Valid @RequestBody AuthRequestDTO dto) {
    try {
      // 🔸 Vérifier si l’utilisateur existe
      Utilisateur utilisateur =
          utilisateurService
              .findByUsername(dto.getUsername())
              .orElseThrow(() -> new BadCredentialsException("Utilisateur introuvable"));

      boolean encoded = utilisateur.getPassword().startsWith("$2a$");
      boolean matches =
          encoded
              ? utilisateurService
                  .passwordEncoder()
                  .matches(dto.getPassword(), utilisateur.getPassword())
              : dto.getPassword().equals(utilisateur.getPassword());

      if (!matches) throw new BadCredentialsException("Identifiants invalides");

      // 🔐 Migration auto si mot de passe non encodé
      if (!encoded) {
        utilisateur.setPassword(utilisateurService.passwordEncoder().encode(dto.getPassword()));
        utilisateurService.save(utilisateur);
      }

      // 🎟️ Génération Access + Refresh tokens
      String accessToken = jwtService.generateAccessToken(utilisateur);

      RefreshToken refreshToken =
          refreshTokenService.createRefreshToken(
              utilisateur.getUsername(), dto.getUsername(), "unknown");

    //           RefreshToken refreshToken = refreshTokenService.createRefreshToken(
    //       utilisateur.getUsername(),
    //       request.getHeader("User-Agent"),  // ✅ Real user agent
    //       getClientIP(request)  // ✅ Real IP
    //   );

    // private String getClientIP(HttpServletRequest request) {
    //   String ip = request.getHeader("X-Forwarded-For");
    //   if (ip == null || ip.isEmpty()) {
    //       ip = request.getRemoteAddr();
    //   }
    //   return ip;
    // }
      // 🌐 Redirection front selon rôle
      String redirectUrl =
          switch (utilisateur.getRole().toUpperCase()) {
            case "ADMIN" -> "/dashboard/admin";
            case "DIRECTION" -> "/dashboard/direction";
            case "SECRETARIAT" -> "/dashboard/secretariat";
            default -> "/dashboard/formateur";
          };

      // 🧩 Construire réponse complète
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

  // ==========================================================
  // 🔹 REFRESH TOKEN — Régénère AccessToken et fait rotation
  // ==========================================================
  @PostMapping("/refresh")
  public ResponseEntity<ResponseDTO<AuthResponseDTO>> refreshToken(HttpServletRequest request) {
    String refreshTokenHeader = request.getHeader("Refresh-Token");
    if (refreshTokenHeader == null)
      throw new BusinessValidationException("Aucun refresh token fourni");

    // 🔍 Validation du refresh token
    RefreshToken token = refreshTokenService.validateRefreshToken(refreshTokenHeader);

    // 🔄 Générer un nouvel access token
    String newAccessToken = jwtService.generateAccessToken(token.getUtilisateur());

    // 🔁 Rotation : invalider ancien + créer un nouveau refresh token
    refreshTokenService.revokeRefreshToken(refreshTokenHeader);
    RefreshToken newRefresh =
        refreshTokenService.createRefreshToken(
            token.getUtilisateur().getUsername(),
            request.getHeader("User-Agent"),
            request.getRemoteAddr());

    // 🧩 Réponse unifiée
    AuthResponseDTO response =
        new AuthResponseDTO(
            newAccessToken,
            newRefresh.getToken(),
            token.getUtilisateur().getUsername(),
            token.getUtilisateur().getRole(),
            false,
            null);

    return ResponseEntity.ok(ResponseDTO.success("Token régénéré avec succès", response));
  }

  // ==========================================================
  // 🔹 LOGOUT — Révoque le refresh token courant
  // ==========================================================
  @PostMapping("/logout")
  public ResponseEntity<ResponseDTO<Void>> logout(
      @RequestHeader("Refresh-Token") String refreshToken) {
    refreshTokenService.revokeRefreshToken(refreshToken);
    return ResponseEntity.ok(ResponseDTO.success("Déconnexion réussie", null));
  }

  // ==========================================================
  // 🔹 Création manuelle d’un utilisateur (Admin/Direction)
  // ==========================================================
  @PostMapping("/admin/create-user")
  @PreAuthorize("hasRole('DIRECTION') or hasRole('ADMIN')")
  public ResponseEntity<?> adminCreateUser(@Valid @RequestBody UtilisateurCreateDTO dto) {
    return utilisateurService.create(dto);
  }
}
