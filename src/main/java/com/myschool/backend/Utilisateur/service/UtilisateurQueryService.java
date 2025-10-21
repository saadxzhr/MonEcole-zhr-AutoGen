package com.myschool.backend.Utilisateur.service;


import java.util.Optional;

import com.myschool.backend.Utilisateur.Model.Utilisateur;

public interface UtilisateurQueryService {
    Optional<Utilisateur> findByUsername(String username);
}
