package com.myschool.backend.Utilisateur.controller;

import com.myschool.backend.Exception.ResponseDTO;
import com.myschool.backend.Utilisateur.dto.AuthRequestDTO;
import com.myschool.backend.Utilisateur.dto.AuthResponseDTO;
import com.myschool.backend.Utilisateur.dto.UtilisateurCreateDTO;
import com.myschool.backend.Utilisateur.Model.Utilisateur;
import com.myschool.backend.Utilisateur.security.JwtService;
import com.myschool.backend.Utilisateur.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final JwtService jwtService;
    private final UtilisateurService utilisateurService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDTO dto) {
        try {
            Utilisateur utilisateur = utilisateurService.findByUsername(dto.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("Utilisateur introuvable"));

            boolean isEncoded = utilisateur.getPassword().startsWith("$2a$");
            boolean matches;

            if (isEncoded) {
                // 🔹 Cas normal : mot de passe encodé (bcrypt)
                matches = utilisateurService.passwordEncoder().matches(dto.getPassword(), utilisateur.getPassword());
            } else {
                // 🔹 Cas legacy : mot de passe non encodé
                matches = dto.getPassword().equals(utilisateur.getPassword());
            }

            if (!matches) {
                throw new BadCredentialsException("Identifiants invalides");
            }

            // 🔹 Si le mot de passe était en clair, on le convertit immédiatement
            if (!isEncoded) {
                utilisateur.setPassword(utilisateurService.passwordEncoder().encode(dto.getPassword()));
                utilisateurService.save(utilisateur);
            }

            // 🔹 Génération du token JWT
            String token = jwtService.generateToken(utilisateur.getUsername(), utilisateur.getRole());

            // 🔹 URL de redirection selon le rôle
            String redirectUrl = switch (utilisateur.getRole().toUpperCase()) {
                case "ADMIN" -> "/dashboard/admin";
                case "DIRECTION" -> "/dashboard/direction";
                case "SECRETARIAT" -> "/dashboard/secretariat";
                default -> "/dashboard/formateur";
            };

            // 🔹 Réponse
            AuthResponseDTO resp = AuthResponseDTO.builder()
                    .token(token)
                    .username(utilisateur.getUsername())
                    .role(utilisateur.getRole())
                    .forceChangePassword(utilisateur.getForceChangePassword())
                    .redirectUrl(redirectUrl)
                    .build();

            return ResponseEntity.ok(ResponseDTO.success("Authentification réussie", resp));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(ResponseDTO.error("Identifiants invalides"));
        }
    }

    // 🔹 Création manuelle par admin/direction
    @PostMapping("/admin/create-user")
    @PreAuthorize("hasRole('DIRECTION') or hasRole('ADMIN')")
    public ResponseEntity<?> adminCreateUser(@Valid @RequestBody UtilisateurCreateDTO dto) {
        return utilisateurService.create(dto);
    }
}
