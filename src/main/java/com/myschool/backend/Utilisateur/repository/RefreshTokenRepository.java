package com.myschool.backend.utilisateur.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.myschool.backend.utilisateur.model.RefreshToken;

import java.util.Optional;
import java.util.List;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUtilisateurIdAndRevokedFalse(Long utilisateurId);

    void deleteByUtilisateurId(Long utilisateurId);
}
