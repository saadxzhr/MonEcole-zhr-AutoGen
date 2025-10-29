package com.szschoolmanager.auth.security;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
@Slf4j
public class JwtExceptionHandler {

  @ExceptionHandler({JwtException.class, IllegalArgumentException.class})
  public ResponseEntity<Object> handleJwt(Exception ex) {
    log.warn("JWT error: {}", ex.getMessage());
    var body = java.util.Map.of("error", "invalid_token", "message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
  }
}
