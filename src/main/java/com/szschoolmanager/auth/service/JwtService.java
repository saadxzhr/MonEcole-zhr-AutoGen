package com.szschoolmanager.auth.service;

import com.szschoolmanager.auth.dto.TokensDTO;
import com.szschoolmanager.auth.model.RefreshToken;
import com.szschoolmanager.auth.model.Utilisateur;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
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
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;



@Slf4j
@Service
public class JwtService {

  // --- Configuration JWT ---
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

  // --- Runtime state ---
  private PrivateKey privateKey;
  @Getter private RSAPublicKey publicKey;

  // --- Dependencies ---
  private final StringRedisTemplate stringRedisTemplate;
  private final RefreshTokenService refreshTokenService;

  public JwtService(StringRedisTemplate stringRedisTemplate,
                    RefreshTokenService refreshTokenService) {
      this.stringRedisTemplate = stringRedisTemplate;
      this.refreshTokenService = refreshTokenService;
  }

  // --- Initialization ---
  @PostConstruct
  public void init() {
    try {
      this.privateKey = loadPrivateKey(privateKeyPath);
      this.publicKey = (RSAPublicKey) loadPublicKey(publicKeyPath);
      if (privateKey == null || publicKey == null)
        throw new IllegalStateException("RSA keys not loaded correctly");
      log.info("🔐 JWT initialized kid={} issuer={} audience={}", configuredKid, issuer, audience);
    } catch (Exception e) {
      log.error("Failed to init JwtService: {}", e.getMessage(), e);
      throw new IllegalStateException(e);
    }
  }

  // --- Key loaders ---
  private PrivateKey loadPrivateKey(String path) throws Exception {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(path.replace("classpath:", ""))) {
      if (is == null) throw new FileNotFoundException("Private key not found at " + path);
      byte[] normalized = normalizeKey(is.readAllBytes(), true);
      return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(normalized));
    }
  }

  private PublicKey loadPublicKey(String path) throws Exception {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(path.replace("classpath:", ""))) {
      if (is == null) throw new FileNotFoundException("Public key not found at " + path);
      byte[] normalized = normalizeKey(is.readAllBytes(), false);
      return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(normalized));
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

  // --- JWT Generation ---
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
        .claim("authorities", List.of("ROLE_" + user.getRole().toUpperCase()))
        .setIssuedAt(Date.from(now))
        .setNotBefore(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(accessExpirationSeconds)))
        .signWith(privateKey, SignatureAlgorithm.RS256)
        .compact();
  }

  /**
   * Génère un couple (access + refresh) complet et prêt à production.
   */
  public TokensDTO generateTokens(Utilisateur user, HttpServletRequest request) {
      String accessToken = generateAccessToken(user);
      String clientIp = extractClientIp(request);
      String userAgent = (request != null && request.getHeader("User-Agent") != null)
              ? request.getHeader("User-Agent") : "unknown-client";

      RefreshToken refreshEntity = refreshTokenService.createRefreshToken(user, userAgent, clientIp);
      String rawRefresh = refreshEntity.getToken();

      log.info("🔐 Tokens générés pour [{}] depuis IP [{}], agent [{}]",
              user.getUsername(), clientIp, userAgent);

      long refreshExpiresIn = refreshEntity.getExpiresAt() != null
          ? Duration.between(
                Instant.now(),
                refreshEntity.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant()
            ).getSeconds()
          : Duration.ofDays(30).getSeconds();


      return new TokensDTO(accessToken, rawRefresh, accessExpirationSeconds, refreshExpiresIn);
  }

  public TokensDTO generateTokens(Utilisateur user) {
      return generateTokens(user, null);
  }

  // --- Helpers ---
  private String extractClientIp(HttpServletRequest request) {
      try {
          if (request == null) return "unknown";
          String xfHeader = request.getHeader("X-Forwarded-For");
          if (xfHeader != null && !xfHeader.isBlank())
              return xfHeader.split(",")[0].trim();
          return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
      } catch (Exception e) {
          log.warn("Failed to extract IP: {}", e.getMessage());
          return "unknown";
      }
  }

  // --- JWT parsing ---
  public Jws<Claims> parseToken(String token) {
    JwtParser parser = Jwts.parserBuilder().setSigningKey(publicKey).build();
    Jws<Claims> jws = parser.parseClaimsJws(token);
    Claims c = jws.getBody();
    if (!issuer.equals(c.getIssuer())) throw new JwtException("Invalid issuer");
    if (c.getAudience() == null || !c.getAudience().contains(audience)) throw new JwtException("Invalid audience");
    return jws;
  }

  public String getUsernameFromToken(String token) { return parseToken(token).getBody().getSubject(); }
  public String getRoleFromToken(String token) { return parseToken(token).getBody().get("role", String.class); }
  // public String getCinFromToken(String token) { return parseToken(token).getBody().get("cin", String.class); }
  public String getJti(String token) { return parseToken(token).getBody().getId(); }

  public boolean isTokenValid(String token, String username) {
    try {
      Claims claims = parseToken(token).getBody();
      return claims.getSubject().equals(username)
          && claims.getExpiration().after(new Date());
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("Invalid JWT: {}", e.getMessage());
      return false;
    }
  }

  public void blacklistAccessTokenJti(String jti, Duration ttl) {
    try {
      if (ttl == null || ttl.isZero() || ttl.isNegative()) return;
      stringRedisTemplate.opsForValue().set("blacklist:access:" + jti, "revoked", ttl);
    } catch (Exception e) {
      log.warn("Failed to write blacklist jti={} into redis: {}", jti, e.getMessage());
    }
  }
}
