package com.szschoolmanager.auth.controller;

import java.math.BigInteger;
import java.util.Base64;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.szschoolmanager.auth.service.JwtService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/.well-known")
@RequiredArgsConstructor
public class JwksController {

  private final JwtService jwtService;

  @GetMapping("/jwks.json")
  public Map<String, Object> jwks() {
    var rsa = jwtService.getPublicKey();
    // n and e in Base64URL
    String n = base64Url(rsa.getModulus());
    String e = base64Url(rsa.getPublicExponent());
    Map<String, Object> jwk = Map.of(
        "kty", "RSA",
        "kid", jwtService.getConfiguredKid(), // add getter or expose configuredKid
        "use", "sig",
        "alg", "RS256",
        "n", n,
        "e", e
    );
    return Map.of("keys", java.util.List.of(jwk));
  }

  private String base64Url(BigInteger bigInt) {
    byte[] bytes = bigInt.toByteArray();
    // strip leading zero if present
    if (bytes.length > 1 && bytes[0] == 0) {
      byte[] tmp = new byte[bytes.length - 1];
      System.arraycopy(bytes, 1, tmp, 0, tmp.length);
      bytes = tmp;
    }
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}