package com.myschool.backend.Contoller;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.myschool.backend.Model.MyAppUser;
import com.myschool.backend.Repository.MyAppUserRepository;

@RestController
public class ChangePassController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MyAppUserRepository myAppUserRepository;

    //Changer le mot de pass d'utilisateur en cours
    @PostMapping(value = "/req/changepass", consumes = "application/json")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> payload, Principal principal) {
        //charger valeurs
        String currentPassword = payload.get("password");
        String newPassword = payload.get("newpass");

        //Obliger tt ls champs
        if (currentPassword == null || newPassword == null || currentPassword.isBlank() || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body("Champs requis manquants.");
        }

        //nom d'utilisateur en cours
        String username = principal.getName(); 
        Optional<MyAppUser> optionalUser = myAppUserRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable.");
        }

        //charger les details d'utilisateur
        MyAppUser user = optionalUser.get();

        //verifier mot de pass actuel
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Mot de passe actuel incorrect.");
        }

        //charger le nv mot de pass dans la table users
        user.setPassword(passwordEncoder.encode(newPassword));
        myAppUserRepository.save(user);
        return ResponseEntity.ok("Mot de passe modifié.");
    }

}