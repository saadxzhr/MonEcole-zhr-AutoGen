package com.myschool.backend.utilisateur.service;


import java.util.Optional;

import com.myschool.backend.utilisateur.model.Utilisateur;

public interface UtilisateurQueryService {
    Optional<Utilisateur> findByUsername(String username);
}
