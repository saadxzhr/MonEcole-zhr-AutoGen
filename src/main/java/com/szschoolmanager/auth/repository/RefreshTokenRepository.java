package com.szschoolmanager.auth.repository;

import com.szschoolmanager.auth.model.RefreshToken;
import com.szschoolmanager.auth.model.Utilisateur;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  @Query("SELECT r FROM RefreshToken r JOIN FETCH r.utilisateur WHERE r.token = :token")
  Optional<RefreshToken> findRawByToken(@Param("token") String token);

  @Query("SELECT r FROM RefreshToken r JOIN FETCH r.utilisateur u WHERE r.token = :token")
  Optional<RefreshToken> findDetailedByToken(@Param("token") String token);

  List<RefreshToken> findAllByUtilisateurIdAndRevokedFalse(Long userId);

  // JPQL bulk update to revoke all valid tokens for a user
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      "UPDATE RefreshToken r SET r.revoked = true WHERE r.utilisateur.id = :userId AND r.revoked = false")
  int revokeAllByUserId(Long userId);

  // Deletes all tokens whose expiry time has passed
  @Modifying
  @Transactional
  @Query("DELETE FROM RefreshToken t WHERE t.expiresAt < :now")
  int deleteByExpiresAtBefore(LocalDateTime now);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT r FROM RefreshToken r WHERE r.utilisateur = :utilisateur AND r.revoked = false ORDER BY r.createdAt ASC")
  List<RefreshToken> findActiveTokensForUpdate(@Param("utilisateur") Utilisateur utilisateur);

  @Modifying
  @Query("UPDATE RefreshToken r SET r.accessJti = :accessJti WHERE r.id = :id")
  void updateAccessJti(@Param("id") Long id, @Param("accessJti") String accessJti);

  @Query("SELECT r FROM RefreshToken r WHERE r.token = :token")
  Optional<RefreshToken> findLightByToken(@Param("token") String token);
}
