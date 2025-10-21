// com/myschool/backend/utilisateur/dto/UtilisateurCreateDTO.java
package com.myschool.backend.Utilisateur.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurCreateDTO {
    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank private String role; // ex: "DIRECTION" ou "FORMATEUR_PERMANENT"
    @NotBlank private String cin;
}
