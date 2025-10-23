package com.szschoolmanager.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
  private String token; // Access Token
  private String refreshToken; // Refresh Token
  private String username; // Nom d’utilisateur
  private String role; // Rôle unique
  private boolean forceChangePassword; // Doit changer son mot de passe
  private String redirectUrl; // Redirection frontend

  // // ✅ Constructeur pour /refresh
  // public AuthResponseDTO(String token, String refreshToken, String username, String role,
  //                        boolean forceChangePassword, String redirectUrl) {
  //     this.token = token;
  //     this.refreshToken = refreshToken;
  //     this.username = username;
  //     this.role = role;
  //     this.forceChangePassword = forceChangePassword;
  //     this.redirectUrl = redirectUrl;
  // }
}
