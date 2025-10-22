package com.myschool.backend.utilisateur.service;

import com.myschool.backend.utilisateur.model.Utilisateur;
import com.myschool.backend.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UtilisateurQueryServiceImpl implements UtilisateurQueryService {

    private final UtilisateurRepository repository;

    @Override
    public Optional<Utilisateur> findByUsername(String username) {
        return repository.findByUsername(username);
    }
}
