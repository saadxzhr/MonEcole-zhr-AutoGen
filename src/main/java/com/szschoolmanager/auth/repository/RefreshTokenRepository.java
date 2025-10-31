package com.szschoolmanager.auth.repository;

import com.szschoolmanager.auth.model.RefreshToken;
import com.szschoolmanager.auth.model.Utilisateur;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findRawByToken(String token);

    @Query("SELECT r FROM RefreshToken r JOIN FETCH r.utilisateur WHERE r.token = :token")
    Optional<RefreshToken> findDetailedByToken(String token);

    List<RefreshToken> findAllByUtilisateurIdAndRevokedFalse(Long userId);

    // JPQL bulk update to revoke all valid tokens for a user
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.utilisateur.id = :userId AND r.revoked = false")
    int revokeAllByUserId(Long userId);

    // Deletes all tokens whose expiry time has passed
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken t WHERE t.expiresAt < :now")
    int deleteByExpiresAtBefore(LocalDateTime now);

/*************  ✨ Windsurf Command ⭐  *************/
    /**
     * Find all active refresh tokens for a user, ordered by creation time.
     * This method is thread-safe thanks to the PESSIMISTIC_WRITE lock.
     * @param utilisateur the user for which to find active refresh tokens
     * @return a list of active refresh tokens
     */
/*******  c728544a-c91e-4fb0-9f23-c7f5eb8bc80b  *******/
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RefreshToken r WHERE r.utilisateur = :utilisateur AND r.revoked = false ORDER BY r.createdAt ASC")
    List<RefreshToken> findActiveTokensForUpdate(@Param("utilisateur") Utilisateur utilisateur);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.accessJti = :accessJti WHERE r.id = :id")
    void updateAccessJti(@Param("id") Long id, @Param("accessJti") String accessJti);

}
