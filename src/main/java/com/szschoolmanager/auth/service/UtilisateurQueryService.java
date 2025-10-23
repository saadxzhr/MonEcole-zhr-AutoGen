package com.szschoolmanager.auth.service;

import com.szschoolmanager.auth.model.Utilisateur;
import java.util.Optional;

public interface UtilisateurQueryService {
  Optional<Utilisateur> findByUsername(String username);
}
