package com.myschool.backend.utilisateur.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.myschool.backend.utilisateur.model.RefreshToken;
import com.myschool.backend.utilisateur.model.Utilisateur;
import com.myschool.backend.utilisateur.repository.RefreshTokenRepository;
import com.myschool.backend.utilisateur.repository.UtilisateurRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UtilisateurRepository utilisateurRepository;

    private static final int REFRESH_TOKEN_EXPIRATION_DAYS = 7;

    @Transactional
    public RefreshToken createRefreshToken(String username, String userAgent, String ip) {
        Utilisateur user = utilisateurRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Révoquer anciens tokens
        refreshTokenRepository.findAllByUtilisateurIdAndRevokedFalse(user.getId())
                .forEach(t -> { t.setRevoked(true); refreshTokenRepository.save(t); });

        RefreshToken refreshToken = RefreshToken.builder()
                .utilisateur(user)
                .token(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRATION_DAYS))
                .revoked(false)
                .userAgent(userAgent)
                .ipAddress(ip)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (refreshToken.isExpired() || refreshToken.isRevoked()) {
            throw new RuntimeException("Token expiré ou révoqué");
        }
        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    @Transactional
    public void revokeAllForUser(Long userId) {
        refreshTokenRepository.findAllByUtilisateurIdAndRevokedFalse(userId)
                .forEach(t -> { t.setRevoked(true); refreshTokenRepository.save(t); });
    }
}
