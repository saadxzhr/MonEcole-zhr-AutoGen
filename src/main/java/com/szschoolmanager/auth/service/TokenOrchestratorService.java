package com.szschoolmanager.auth.service;

import java.time.Duration;

import org.springframework.stereotype.Service;

import com.szschoolmanager.auth.dto.TokensDTO;
import com.szschoolmanager.auth.model.RefreshToken;
import com.szschoolmanager.auth.model.Utilisateur;
import com.szschoolmanager.exception.BusinessValidationException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class TokenOrchestratorService {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    /**
     * Gère la rotation du refresh token, la détection de réutilisation,
     * et le blacklistage du access token lié.
     */
    public RefreshToken rotateWithAccessHandling(String presentedRaw,
                                                 String userAgent,
                                                 String ipAddress,
                                                 String accessJti) {

        try {
            RefreshToken newRt = refreshTokenService.rotateRefreshToken(
                    presentedRaw, userAgent, ipAddress, accessJti);
            return newRt;

        } catch (BusinessValidationException e) {

            // 🔒 Cas : le refresh token a été réutilisé (reuse detected)
            if (e.getMessage().toLowerCase().contains("reuse")) {
                RefreshToken reused = refreshTokenService.findByRawToken(presentedRaw);
                if (reused != null && reused.getAccessJti() != null) {
                    try {
                        jwtService.blacklistAccessTokenJti(
                                reused.getAccessJti(),
                                Duration.ofSeconds(jwtService.getAccessExpirationSeconds())
                        );
                        log.warn("🚫 Access token JTI blacklisted due to refresh reuse for user {}",
                                reused.getUtilisateur().getUsername());
                    } catch (Exception ex) {
                        log.error("❌ Failed to blacklist reused access token JTI: {}", ex.getMessage());
                    }
                }
            }
            throw e; // rethrow pour que le contrôleur sache que c’est une reuse
        }
    }

    
}
