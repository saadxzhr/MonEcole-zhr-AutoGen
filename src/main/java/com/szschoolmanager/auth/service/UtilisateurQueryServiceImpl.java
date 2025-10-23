package com.szschoolmanager.auth.service;

import com.szschoolmanager.auth.model.Utilisateur;
import com.szschoolmanager.auth.repository.UtilisateurRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UtilisateurQueryServiceImpl implements UtilisateurQueryService {

  private final UtilisateurRepository repository;

  @Override
  public Optional<Utilisateur> findByUsername(String username) {
    return repository.findByUsername(username);
  }
}
