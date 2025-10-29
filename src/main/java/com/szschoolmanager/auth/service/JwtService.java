package com.szschoolmanager.auth.service;

import com.szschoolmanager.auth.model.Utilisateur;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
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


  @Value("${jwt.issuer}")
  private String issuer;

  @Value("${jwt.audience}")
  private String audience;

  @Value("${jwt.access-expiration-seconds:900}")
  private long accessExpirationSeconds;

  @Getter
  @Value("${jwt.kid}")
  private String configuredKid;

  private PrivateKey privateKey;
  @Getter private RSAPublicKey publicKey;

  private final StringRedisTemplate stringRedisTemplate;

  public JwtService(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @PostConstruct
  public void init() {
    try {
      this.privateKey = loadPrivateKey(privateKeyPath);
      this.publicKey = (RSAPublicKey) loadPublicKey(publicKeyPath);
      if (privateKey == null || publicKey == null) {
        throw new IllegalStateException("RSA keys not loaded correctly");
      }
      log.info("🔐 JWT Service initialized with kid={} issuer={} audience={}", configuredKid, issuer, audience);
    } catch (Exception e) {
      log.error("Failed to init JwtService: {}", e.getMessage(), e);
      throw new IllegalStateException(e);
    }
  }

  private PrivateKey loadPrivateKey(String path) throws Exception {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(path.replace("classpath:", ""))) {
      if (is == null) throw new FileNotFoundException("Private key not found at " + path);
      byte[] bytes = is.readAllBytes();
      byte[] normalized = normalizeKey(bytes, true);
      PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(normalized);
      return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }
  }

  private PublicKey loadPublicKey(String path) throws Exception {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(path.replace("classpath:", ""))) {
      if (is == null) throw new FileNotFoundException("Public key not found at " + path);
      byte[] bytes = is.readAllBytes();
      byte[] normalized = normalizeKey(bytes, false);
      X509EncodedKeySpec spec = new X509EncodedKeySpec(normalized);
      return KeyFactory.getInstance("RSA").generatePublic(spec);
    }
  }

  private byte[] normalizeKey(byte[] input, boolean isPrivate) {
    String asText = new String(input, StandardCharsets.UTF_8).trim();
    if (asText.startsWith("-----BEGIN")) {
      String header = isPrivate ? "-----BEGIN PRIVATE KEY-----" : "-----BEGIN PUBLIC KEY-----";
      String footer = isPrivate ? "-----END PRIVATE KEY-----" : "-----END PUBLIC KEY-----";
      String base64 = asText.replace(header, "").replace(footer, "").replaceAll("\\s", "");
      return Base64.getDecoder().decode(base64);
    }
    return input;
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

  /**
   * Parse token using currently loaded public key. Caller must handle exceptions.
   * If you later add multi-key rotation, implement SigningKeyResolver here to pick key by kid.
   */
  public Jws<Claims> parseToken(String token) {
    JwtParser parser = Jwts.parserBuilder()
        .setSigningKey(publicKey)
        .build();
    Jws<Claims> jws = parser.parseClaimsJws(token);

    Claims c = jws.getBody();
    if (!issuer.equals(c.getIssuer())) throw new JwtException("Invalid issuer");
    if (c.getAudience() == null || !c.getAudience().contains(audience)) throw new JwtException("Invalid audience");
    return jws;
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

  public String getJti(String token) {
    return parseToken(token).getBody().getId();
  }

  public boolean isTokenValid(String token, String username) {
    try {
      Claims claims = parseToken(token).getBody();
      return claims.getSubject().equals(username)
          && claims.getExpiration().after(new Date());
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("Invalid JWT token: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Blacklist an access token jti in Redis atomically with TTL.
   */
  public void blacklistAccessTokenJti(String jti, java.time.Duration ttl) {
    try {
      if (ttl.isNegative() || ttl.isZero()) return;
      stringRedisTemplate.opsForValue().set("blacklist:access:" + jti, "revoked", ttl);
    } catch (Exception e) {
      log.warn("Failed to write blacklist jti={} into redis: {}", jti, e.getMessage());
    }
  }
}
