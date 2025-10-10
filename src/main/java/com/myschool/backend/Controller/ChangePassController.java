package com.myschool.backend.Controller;

import java.security.Principal;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.myschool.backend.Model.MyAppUser;

import com.myschool.backend.Service.MyAppUserService;

@RestController
public class ChangePassController {

    @Autowired
    private MyAppUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/req/changepass")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody Map<String, String> payload,
            Principal principal) {

        String currentPass = payload.get("password");
        String newPass = payload.get("newpass");

        if (currentPass == null || newPass == null || currentPass.isBlank() || newPass.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Tous les champs sont obligatoires."));
        }

        // Charger l'utilisateur actuel
        MyAppUser user = userService.repository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));

        // Vérifier mot de passe actuel
        if (!passwordEncoder.matches(currentPass, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Mot de passe actuel incorrect."));
        }

        // Optionnel : vérifier complexité du mot de passe
        if (newPass.length() < 8) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Le mot de passe doit contenir au moins 8 caractères."));
        }

        // Encoder et sauvegarder
        user.setPassword(passwordEncoder.encode(newPass));
        userService.repository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "Mot de passe modifié avec succès."));
    }

}