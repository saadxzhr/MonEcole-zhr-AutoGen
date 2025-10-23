package com.szschoolmanager.auth.security;

import com.szschoolmanager.auth.model.Utilisateur;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JwtService {

  // ==========================================================
  // 🔧 Configuration RSA uniquement (aucun HS256 ici)
  // ==========================================================

  @Value("${jwt.private-key-path}")
  private String privateKeyPath;

  @Value("${jwt.public-key-path}")
  private String publicKeyPath;

  @Value("${jwt.expiration:3600000}") // 1h par défaut
  private long jwtExpirationMs;

  private PrivateKey privateKey;
  private PublicKey publicKey;

  // ==========================================================
  // 🧩 INITIALISATION DES CLÉS RSA
  // ==========================================================
  @PostConstruct
  public void init() {
    try {
      this.privateKey = loadPrivateKey(privateKeyPath);
      this.publicKey = loadPublicKey(publicKeyPath);

      if (privateKey == null || publicKey == null) {
        throw new IllegalStateException("RSA keys not loaded correctly");
      }

      log.info("🔐 JWT Service initialized with RSA keys (RS256)");
    } catch (Exception e) {
      log.error("❌ Failed to initialize RSA keys: {}", e.getMessage(), e);
      throw new IllegalStateException("RSA key initialization failed: " + e.getMessage(), e);
    }
  }

  // ==========================================================
  // 🔑 Chargement des clés RSA
  // ==========================================================
  private PrivateKey loadPrivateKey(String path)
            throws IOException, GeneralSecurityException {
        try (InputStream is =
                getClass().getClassLoader().getResourceAsStream(path.replace("classpath:", ""))) {

            if (is == null) {
                throw new FileNotFoundException("Private key not found at " + path);
            }

            byte[] keyBytes = is.readAllBytes();
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);

        } catch (InvalidKeySpecException e) {
            throw new GeneralSecurityException("Invalid private key format", e);
        }
    }


  private PublicKey loadPublicKey(String path)
            throws IOException, GeneralSecurityException {
        try (InputStream is =
                getClass().getClassLoader().getResourceAsStream(path.replace("classpath:", ""))) {

            if (is == null) {
                throw new FileNotFoundException("Public key not found at " + path);
            }

            byte[] keyBytes = is.readAllBytes();
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);

        } catch (InvalidKeySpecException e) {
            throw new GeneralSecurityException("Invalid public key format", e);
        }
    }


  // ==========================================================
  // 🎟️ GÉNÉRATION D’UN TOKEN
  // ==========================================================
  public String generateAccessToken(Utilisateur user) {
    if (user == null || user.getUsername() == null)
      throw new IllegalArgumentException("Utilisateur invalide pour JWT");

    return Jwts.builder()
        .setSubject(user.getUsername())
        .claim("role", user.getRole())
        .claim("cin", user.getCin())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();
  }

  // ==========================================================
  // 🧠 EXTRACTION D’INFORMATIONS DU TOKEN
  // ==========================================================
  public Jws<Claims> parseToken(String token) {
    return Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token);
  }

  public String getUsernameFromToken(String token) {
    return parseToken(token).getBody().getSubject();
  }

  public String getRoleFromToken(String token) {
    return parseToken(token).getBody().get("role", String.class);
  }

  public String getCinFromToken(String token) {
    return parseToken(token).getBody().get("cin", String.class);
  }

  // ==========================================================
  // ✅ VALIDATION DU TOKEN
  // ==========================================================
  public boolean isTokenValid(String token, String username) {
    try {
      Claims claims = parseToken(token).getBody();
      String tokenUsername = claims.getSubject();
      Date expiration = claims.getExpiration();
      return tokenUsername.equals(username) && expiration.after(new Date());
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("⚠️ Invalid JWT token: {}", e.getMessage());
      return false;
    }
  }
}
