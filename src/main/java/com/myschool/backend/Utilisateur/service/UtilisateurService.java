// com/myschool/backend/utilisateur/service/UtilisateurService.java
package com.myschool.backend.Utilisateur.service;

import com.myschool.backend.Exception.ResponseDTO;
import com.myschool.backend.Utilisateur.dto.*;
import com.myschool.backend.Utilisateur.mapper.UtilisateurMapper;
import com.myschool.backend.Utilisateur.Model.*;
import com.myschool.backend.Utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository repo;
    private final UtilisateurMapper mapper;
    private final PasswordEncoder passwordEncoder;



    public PasswordEncoder passwordEncoder() {
        return passwordEncoder;
    }

    public void save(Utilisateur utilisateur) {
        repo.save(utilisateur);
    }

    public UserDetails buildUserDetails(Utilisateur u) {
        return org.springframework.security.core.userdetails.User
                .builder()
                .username(u.getUsername())
                .password(u.getPassword())
                .authorities("ROLE_" + u.getRole().toUpperCase())
                .build();
    }


    // Pagination + filtre par rôle
    public ResponseEntity<ResponseDTO<Page<UtilisateurResponseDTO>>> list(int page, int size, String roleFilter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Utilisateur> p;
        if (roleFilter != null && !roleFilter.isBlank()) {
            // normaliser si besoin
            Role rEnum = null;
            try { rEnum = Role.valueOf(roleFilter.toUpperCase()); } catch (Exception ignored) {}
            String roleStr = (rEnum != null) ? rEnum.name() : roleFilter;
            p = repo.findByRole(roleStr, pageable);
        } else {
            p = repo.findAll(pageable);
        }
        Page<UtilisateurResponseDTO> res = p.map(mapper::toResponseDTO);
        return ResponseEntity.ok(ResponseDTO.success("Liste utilisateurs", res));
    }

    // Create (admin/direction) or manual (front/back)
    public ResponseEntity<ResponseDTO<UtilisateurResponseDTO>> create(UtilisateurCreateDTO dto) {
        if (repo.existsByUsername(dto.getUsername())) {
            return ResponseEntity.badRequest().body(ResponseDTO.error("Nom d'utilisateur déjà existant"));
        }
        // username unique; cin peut être dupliqué (multi comptes)
        Utilisateur utilisateur = mapper.toEntity(dto);
        utilisateur.setPassword(passwordEncoder.encode(dto.getPassword()));
        // normalise role
        try { Role.normalize(dto.getRole()); } catch (Exception ignored) {}
        utilisateur.setRole(dto.getRole().toUpperCase());
        utilisateur.setForceChangePassword(true);
        repo.save(utilisateur);
        return ResponseEntity.ok(ResponseDTO.success("Utilisateur créé", mapper.toResponseDTO(utilisateur)));
    }

    // Update full (Direction/Admin can tout modifier)
    public ResponseEntity<ResponseDTO<UtilisateurResponseDTO>> update(Long id, UtilisateurUpdateDTO dto) {
        Optional<Utilisateur> opt = repo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body(ResponseDTO.error("Utilisateur introuvable"));
        }
        Utilisateur u = opt.get();

        // vérif username unique si changé
        if (!u.getUsername().equals(dto.getUsername()) && repo.existsByUsername(dto.getUsername())) {
            return ResponseEntity.badRequest().body(ResponseDTO.error("Nom d'utilisateur déjà existant"));
        }

        // applique mapping (username, role, cin) sans toucher au password/timestamps/id
        mapper.updateFromDto(dto, u);

        // si password fourni (admin change via update), on encode et set
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            u.setPassword(passwordEncoder.encode(dto.getPassword()));
            // si admin souhaite forcer le changement après reset, on le prend en compte
            if (dto.getForceChangePassword() != null) {
                u.setForceChangePassword(dto.getForceChangePassword());
            } else {
                // si admin change password sans préciser force flag -> on peut laisser false
                u.setForceChangePassword(false);
            }
        } else {
            // ne pas toucher au password ; si forceChangePassword indiqué sans password, on l'applique
            if (dto.getForceChangePassword() != null) {
                u.setForceChangePassword(dto.getForceChangePassword());
            }
        }

        // normaliser role (ex: toUpperCase)
        if (dto.getRole() != null) {
            u.setRole(dto.getRole());
        }

        repo.save(u);
        return ResponseEntity.ok(ResponseDTO.success("Utilisateur mis à jour avec succès", mapper.toResponseDTO(u)));
    }


    // Change password (user action) - if forceChangePassword true, oldPassword may be optional if admin reset
    public ResponseEntity<ResponseDTO<String>> changePassword(Long id, ChangePasswordDTO dto, boolean verifyOld) {
        Optional<Utilisateur> opt = repo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.badRequest().body(ResponseDTO.error("Utilisateur introuvable"));
        Utilisateur u = opt.get();

        if (verifyOld) {
            if (!passwordEncoder.matches(dto.getOldPassword(), u.getPassword()))
                return ResponseEntity.badRequest().body(ResponseDTO.error("Ancien mot de passe incorrect"));
        }
        u.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        u.setForceChangePassword(false);
        repo.save(u);
        return ResponseEntity.ok(ResponseDTO.success("Mot de passe modifié", null));
    }

    // Delete
    public ResponseEntity<ResponseDTO<String>> delete(Long id) {
        if (!repo.existsById(id)) return ResponseEntity.badRequest().body(ResponseDTO.error("Utilisateur introuvable"));
        repo.deleteById(id);
        return ResponseEntity.ok(ResponseDTO.success("Utilisateur supprimé", null));
    }

    // Find by username (used by security)
    public Optional<Utilisateur> findByUsername(String username) {
            return repo.findByUsername(username);
        }

        public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }




}
