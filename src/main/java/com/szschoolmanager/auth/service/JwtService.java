package com.szschoolmanager.auth.service;

import com.szschoolmanager.auth.model.Utilisateur;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class JwtService {

  @Value("${jwt.private-key-path}")
  private String privateKeyPath;

  @Value("${jwt.public-key-path}")
  private String publicKeyPath;

  @Value("${jwt.kid}")
  private String configuredKid;

  @Value("${jwt.issuer}")
  private String issuer;

  @Value("${jwt.audience}")
  private String audience;

  @Value("${jwt.access-expiration-seconds:900}")
  private long accessExpirationSeconds;

  private PrivateKey privateKey;
  @Getter private RSAPublicKey publicKey;

  public String getConfiguredKid() {
    return configuredKid;
  }

  @PostConstruct
  public void init() {
    try {
      this.privateKey = loadPrivateKey(privateKeyPath);
      this.publicKey = (RSAPublicKey) loadPublicKey(publicKeyPath);
      if (privateKey == null || publicKey == null) {
        throw new IllegalStateException("RSA keys not loaded correctly");
      }
      log.info("🔐 JWT initialized with PEM/DER support (kid={}, issuer={}, aud={})", configuredKid, issuer, audience);
    } catch (Exception e) {
      log.error("❌ Failed to initialize RSA keys: {}", e.getMessage(), e);
      throw new IllegalStateException(e);
    }
  }

  private PrivateKey loadPrivateKey(String path) throws Exception {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(path.replace("classpath:", ""))) {
      if (is == null) throw new FileNotFoundException("Private key not found at " + path);
      byte[] bytes = is.readAllBytes();
      byte[] normalized = normalizeKey(bytes, true);
      return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(normalized));
    }
  }

  private PublicKey loadPublicKey(String path) throws Exception {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(path.replace("classpath:", ""))) {
      if (is == null) throw new FileNotFoundException("Public key not found at " + path);
      byte[] bytes = is.readAllBytes();
      byte[] normalized = normalizeKey(bytes, false);
      return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(normalized));
    }
  }

  private byte[] normalizeKey(byte[] input, boolean isPrivate) {
    String content = new String(input, StandardCharsets.UTF_8).trim();
    if (content.startsWith("-----BEGIN")) {
      String header = isPrivate ? "-----BEGIN PRIVATE KEY-----" : "-----BEGIN PUBLIC KEY-----";
      String footer = isPrivate ? "-----END PRIVATE KEY-----" : "-----END PUBLIC KEY-----";
      String base64 = content.replace(header, "").replace(footer, "").replaceAll("\\s", "");
      return Base64.getDecoder().decode(base64);
    }
    return input; // DER format
  }

  public String generateAccessToken(Utilisateur user) {
    Instant now = Instant.now();
    String jti = UUID.randomUUID().toString();

    return Jwts.builder()
        .setHeaderParam("kid", configuredKid)
        .setIssuer(issuer)
        .setAudience(audience)
        .setSubject(user.getUsername())
        .setId(jti)
        .claim("role", user.getRole())
        .claim("cin", user.getCin())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(accessExpirationSeconds)))
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();
  }

  public Jws<Claims> parseToken(String token) {
    return Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token);
  }

  public boolean isTokenValid(String token, String username) {
    try {
      Claims c = parseToken(token).getBody();
      return c.getSubject().equals(username) && c.getExpiration().after(new Date());
    } catch (JwtException e) {
      log.warn("⚠️ Invalid token: {}", e.getMessage());
      return false;
    }
  }
}
