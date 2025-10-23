package com.szschoolmanager.auth.repository;

import com.szschoolmanager.auth.model.Utilisateur;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

  @Query(
      "SELECT new Utilisateur(u.id, u.username, u.password, u.role, u.cin, u.forceChangePassword) "
          + "FROM Utilisateur u WHERE u.username = :username")
  Optional<Utilisateur> findByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByCinAndUsername(String cin, String username);

  Page<Utilisateur> findByRole(String role, Pageable pageable);

  Page<Utilisateur> findByCin(String cin, Pageable pageable);
}
