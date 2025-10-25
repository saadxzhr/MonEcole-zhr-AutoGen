package com.szschoolmanager.auth.service;

import com.szschoolmanager.auth.dto.*;
import com.szschoolmanager.auth.mapper.UtilisateurMapper;
import com.szschoolmanager.auth.model.Utilisateur;
import com.szschoolmanager.auth.repository.UtilisateurRepository;
import com.szschoolmanager.exception.ResponseDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  @Transactional
  public void save(Utilisateur utilisateur) {
    repo.save(utilisateur);
  }

  public UserDetails buildUserDetails(Utilisateur u) {
    return org.springframework.security.core.userdetails.User.builder()
        .username(u.getUsername())
        .password(u.getPassword())
        .authorities("ROLE_" + (u.getRole() == null ? "USER" : u.getRole().toUpperCase()))
        .build();
  }

  public ResponseEntity<ResponseDTO<Page<UtilisateurResponseDTO>>> list(int page, int size, String roleFilter) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
    Page<Utilisateur> p;

    if (roleFilter != null && !roleFilter.isBlank()) {
      String roleStr = roleFilter.trim().toUpperCase();
      p = repo.findByRole(roleStr, pageable);
    } else {
      p = repo.findAll(pageable);
    }

    Page<UtilisateurResponseDTO> res = p.map(mapper::toResponseDTO);
    return ResponseEntity.ok(ResponseDTO.success("Liste des utilisateurs", res));
  }

  @Transactional
  public ResponseEntity<ResponseDTO<UtilisateurResponseDTO>> create(UtilisateurCreateDTO dto) {
    if (repo.existsByUsername(dto.getUsername())) {
      return ResponseEntity.badRequest().body(ResponseDTO.error("Nom d'utilisateur déjà existant"));
    }

    Utilisateur utilisateur = mapper.toEntity(dto);
    utilisateur.setPassword(passwordEncoder.encode(dto.getPassword()));

    // Rôle dynamique (aucun enum)
    if (dto.getRole() == null || dto.getRole().isBlank()) {
      utilisateur.setRole("USER");
    } else {
      utilisateur.setRole(dto.getRole().trim().toUpperCase());
    }

    utilisateur.setForceChangePassword(true);
    repo.save(utilisateur);
    return ResponseEntity.ok(ResponseDTO.success("Utilisateur créé", mapper.toResponseDTO(utilisateur)));
  }

  @Transactional
  public ResponseEntity<ResponseDTO<UtilisateurResponseDTO>> update(Long id, UtilisateurUpdateDTO dto) {
    Optional<Utilisateur> opt = repo.findById(id);
    if (opt.isEmpty()) {
      return ResponseEntity.badRequest().body(ResponseDTO.error("Utilisateur introuvable"));
    }

    Utilisateur u = opt.get();

    if (!u.getUsername().equals(dto.getUsername()) && repo.existsByUsername(dto.getUsername())) {
      return ResponseEntity.badRequest().body(ResponseDTO.error("Nom d'utilisateur déjà existant"));
    }

    mapper.updateFromDto(dto, u);

    if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
      u.setPassword(passwordEncoder.encode(dto.getPassword()));
      if (dto.getForceChangePassword() != null) {
        u.setForceChangePassword(dto.getForceChangePassword());
      } else {
        u.setForceChangePassword(false);
      }
    } else if (dto.getForceChangePassword() != null) {
      u.setForceChangePassword(dto.getForceChangePassword());
    }

    if (dto.getRole() != null && !dto.getRole().isBlank()) {
      u.setRole(dto.getRole().trim().toUpperCase());
    }

    repo.save(u);
    return ResponseEntity.ok(ResponseDTO.success("Utilisateur mis à jour avec succès", mapper.toResponseDTO(u)));
  }

  @Transactional
  public ResponseEntity<ResponseDTO<String>> changePassword(Long id, ChangePasswordDTO dto, boolean verifyOld) {
    Optional<Utilisateur> opt = repo.findById(id);
    if (opt.isEmpty()) {
      return ResponseEntity.badRequest().body(ResponseDTO.error("Utilisateur introuvable"));
    }

    Utilisateur u = opt.get();

    if (verifyOld && !passwordEncoder.matches(dto.getOldPassword(), u.getPassword())) {
      return ResponseEntity.badRequest().body(ResponseDTO.error("Ancien mot de passe incorrect"));
    }

    u.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    u.setForceChangePassword(false);
    repo.save(u);
    return ResponseEntity.ok(ResponseDTO.success("Mot de passe modifié", null));
  }

  @Transactional
  public ResponseEntity<ResponseDTO<String>> delete(Long id) {
    if (!repo.existsById(id)) {
      return ResponseEntity.badRequest().body(ResponseDTO.error("Utilisateur introuvable"));
    }
    repo.deleteById(id);
    return ResponseEntity.ok(ResponseDTO.success("Utilisateur supprimé", null));
  }

  public Optional<Utilisateur> findByUsername(String username) {
    return repo.findByUsername(username);
  }

  public String encodePassword(String rawPassword) {
    return passwordEncoder.encode(rawPassword);
  }
}
