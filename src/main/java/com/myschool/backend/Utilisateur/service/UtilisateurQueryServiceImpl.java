package com.myschool.backend.Utilisateur.service;

import com.myschool.backend.Utilisateur.Model.Utilisateur;
import com.myschool.backend.Utilisateur.repository.UtilisateurRepository;
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
