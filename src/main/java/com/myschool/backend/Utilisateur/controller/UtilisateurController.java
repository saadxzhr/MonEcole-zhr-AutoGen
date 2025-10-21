// com/myschool/backend/utilisateur/controller/UtilisateurController.java
package com.myschool.backend.Utilisateur.controller;

import com.myschool.backend.Utilisateur.dto.*;
import com.myschool.backend.Utilisateur.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService service;


    @PreAuthorize("hasRole('DIRECTION')")
    @GetMapping("/direction")
    public ResponseEntity<?> getDirectionDashboard() {
        return ResponseEntity.ok("Bienvenue Direction !");
    }
     

    
    @GetMapping
    @PreAuthorize("hasRole('DIRECTION') or hasRole('ADMIN') or hasRole('SECRETARIAT')")
    public ResponseEntity<?> list(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) String role) {
        return service.list(page, size, role);
    }

    @PostMapping
    @PreAuthorize("hasRole('DIRECTION') or hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody UtilisateurCreateDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DIRECTION') or hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody UtilisateurUpdateDTO dto) {
        return service.update(id, dto);
    }

    @PutMapping("/{id}/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordDTO dto) {
        // si l'utilisateur change son propre mdp => verifyOld true, sinon si admin => verifyOld false
        // Ici on suppose que contrôles en place côté frontend/back (ou vérifier SecurityContext)
        return service.changePassword(id, dto, true);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DIRECTION') or hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
