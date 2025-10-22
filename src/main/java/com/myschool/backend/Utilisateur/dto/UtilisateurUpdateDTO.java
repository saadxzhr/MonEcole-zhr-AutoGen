package com.myschool.backend.utilisateur.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurUpdateDTO {
    @NotBlank private String username;
    @NotBlank private String role;
    @NotBlank private String cin;
    // password optionnel : admin/direction peut fournir un nouveau mot de passe via update
    private String password;
    private Boolean forceChangePassword; // optionnel
}
