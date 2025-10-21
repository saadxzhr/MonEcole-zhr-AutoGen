package com.myschool.backend.Utilisateur.dto;

import com.myschool.backend.Utilisateur.Model.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurDTO {

    private Long id;

    @NotBlank(message = "Le nom d’utilisateur est obligatoire")
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;

    @NotBlank(message = "Le CIN est obligatoire")
    private String cin;
}
