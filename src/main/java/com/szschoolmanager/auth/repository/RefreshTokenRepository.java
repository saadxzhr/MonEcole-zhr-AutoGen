package com.szschoolmanager.auth.repository;

import com.szschoolmanager.auth.model.RefreshToken;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByToken(String token);

  List<RefreshToken> findAllByUtilisateurIdAndRevokedFalse(Long utilisateurId);

  void deleteByUtilisateurId(Long utilisateurId);

  List<RefreshToken> findAllByUtilisateurId(Long utilisateurId);
}
