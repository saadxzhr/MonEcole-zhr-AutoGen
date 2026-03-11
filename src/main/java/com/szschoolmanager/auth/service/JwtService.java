package com.szschoolmanager.auth.service;

import com.szschoolmanager.auth.dto.TokensDTO;
import com.szschoolmanager.auth.dto.AccessTokenResult;
import com.szschoolmanager.auth.model.RefreshToken;
import com.szschoolmanager.auth.model.Utilisateur;
import com.szschoolmanager.shared.util.HttpRequestUtils;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JwtService {

  // --- Dependencies ---
  private final StringRedisTemplate stringRedisTemplate;
  private final RefreshTokenService refreshTokenService;

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
  private JwtParser jwtParser;

  public JwtService(
      StringRedisTemplate stringRedisTemplate, RefreshTokenService refreshTokenService) {
    this.stringRedisTemplate = stringRedisTemplate;
    this.refreshTokenService = refreshTokenService;
  }

  // --- Initialization ---
  @PostConstruct
  public void init() {
    try {
      PrivateKey priv = loadPrivateKey(privateKeyPath);
      if (!(priv instanceof RSAPrivateKey))
        throw new IllegalStateException("Clé privée non RSA : " + privateKeyPath);
      this.privateKey = (RSAPrivateKey) priv;

      PublicKey loaded = loadPublicKey(publicKeyPath);
      if (!(loaded instanceof RSAPublicKey)) {
        throw new IllegalStateException(
            "La clé publique fournie n'est pas une clé RSA compatible (chemin="
                + publicKeyPath
                + "). Vérifiez le format PEM/DER et que la clé est une clé RSA.");
      }
      this.publicKey = (RSAPublicKey) loaded;
      this.jwtParser = Jwts.parserBuilder()
                          .setSigningKey(publicKey)
                          .requireIssuer(issuer)
                          .setAllowedClockSkewSeconds(30) // ✅ tolérance horloge 30s
                          .build();

    } catch (Exception e) {
      log.error("Failed to init JwtService: {}", e.getMessage(), e);
      throw new IllegalStateException(e);
    }
  }

  // --- Key loaders ---
  private PrivateKey loadPrivateKey(String path) throws Exception {
    try (InputStream is =
        getClass().getClassLoader().getResourceAsStream(path.replace("classpath:", ""))) {
      if (is == null) throw new FileNotFoundException("Private key not found at " + path);
      byte[] normalized = normalizeKey(is.readAllBytes(), true);
      return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(normalized));
    }
  }

  private PublicKey loadPublicKey(String path) throws Exception {
    try (InputStream is =
        getClass().getClassLoader().getResourceAsStream(path.replace("classpath:", ""))) {
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
  public AccessTokenResult  generateAccessToken(Utilisateur user) {
    Instant now = Instant.now();
    String jti = UUID.randomUUID().toString();

    String token = Jwts.builder()
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
      return new AccessTokenResult(token, jti);
  }

  /** Génère un couple (access + refresh) complet et prêt à production. */
  public TokensDTO generateTokens(Utilisateur user, HttpServletRequest request) {
      AccessTokenResult access = generateAccessToken(user);
      String accessToken = access.token();
    String clientIp = HttpRequestUtils.extractClientIP(request);

    String userAgent =
        (request != null && request.getHeader("User-Agent") != null)
            ? request.getHeader("User-Agent")
            : "unknown-client";
      String accessJti = access.jti();
      RefreshToken refreshEntity =
        refreshTokenService.createRefreshToken(user, userAgent, clientIp, accessJti);
    String rawRefresh = refreshEntity.getToken();

    log.info(
        "🔐 Tokens générés pour [{}] depuis IP [{}], agent [{}]",
        user.getUsername(),
        clientIp,
        userAgent);

    long refreshExpiresIn =
        refreshEntity.getExpiresAt() != null
            ? Duration.between(
                    Instant.now(),
                    refreshEntity.getExpiresAt().atZone(ZoneId.systemDefault()).toInstant())
                .getSeconds()
            : Duration.ofDays(30).getSeconds();

    return new TokensDTO(accessToken, rawRefresh, accessExpirationSeconds, refreshExpiresIn);
  }

  public TokensDTO generateTokens(Utilisateur user) {
    return generateTokens(user, null);
  }

    private String extractJti(Claims claims) { return claims.getId();}

//  private String extractJti(String token) {
//        return parseToken(token).getBody().getId();
//    }

//  // --- JWT parsing ---
//  public Jws<Claims> parseToken(String token) {
//    Jws<Claims> jws = jwtParser.parseClaimsJws(token);
//    Claims c = jws.getBody();
//    if (c.getAudience() == null || !c.getAudience().contains(audience))
//      throw new JwtException("Invalid audience");
//    return jws;
//  }
//
//
//  public boolean isTokenValid(String token, String username) {
//    try {
//      Claims claims = parseToken(token).getBody();
//      return username.equals(claims.getSubject()) && claims.getExpiration().after(new Date());
//    } catch (JwtException | IllegalArgumentException e) {
//      log.warn("Invalid JWT: {}", e.getMessage());
//      return false;
//    }
//  }

    // --- Single validation entry point ---
    public Claims validateAccessToken(String token) {
        try {
            // 1) Cryptographic validation (signature, expiration, issuer, clock skew)
            Jws<Claims> jws = jwtParser.parseClaimsJws(token);
            Claims claims = jws.getBody();

            // 2) Audience validation
            if (claims.getAudience() == null || !claims.getAudience().contains(audience)) {
                throw new JwtException("Invalid audience");
            }

            // 3) Blacklist validation
            String jti = claims.getId();
            if (jti != null && isAccessTokenBlacklisted(jti)) {
                throw new JwtException("Access token revoked");
            }

            return claims;

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            throw e; // Fail fast — filter handles 401
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

  public boolean isAccessTokenBlacklisted(String jti) {
    try {
      String key = "blacklist:access:" + jti;
      return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    } catch (Exception e) {
      log.warn("Failed to check blacklist for jti {}: {}", jti, e.getMessage());
      return false;
    }
  }

  public long getAccessExpirationSeconds() {
    return accessExpirationSeconds;
  }
}
