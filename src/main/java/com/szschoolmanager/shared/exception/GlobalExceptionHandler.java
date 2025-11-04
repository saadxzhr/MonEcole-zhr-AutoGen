package com.szschoolmanager.shared.exception;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.szschoolmanager.shared.dto.ResponseDTO;

/**
 * 🌍 GlobalExceptionHandler Centralise toutes les exceptions pour fournir des réponses API
 * cohérentes. Chaque erreur renvoie un objet ResponseDTO standardisé.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // 🔁 Doublon (ex: code déjà existant)
  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ResponseDTO<Void>> handleDuplicate(DuplicateResourceException ex) {
    log.warn("Duplicate error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ResponseDTO<>("DUPLICATE_RESOURCE", ex.getMessage(), null, LocalDateTime.now()));
  }

  // 🚫 Ressource non trouvée
  @ExceptionHandler({ResourceNotFoundException.class, EntityNotFoundException.class})
  public ResponseEntity<ResponseDTO<Void>> handleNotFound(RuntimeException ex) {
    log.warn("Resource not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ResponseDTO<>("NOT_FOUND", ex.getMessage(), null, LocalDateTime.now()));
  }

  // ⚠️ Erreurs de validation DTO (ex: @NotNull, @Size...)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResponseDTO<Map<String, String>>> handleValidation(
      MethodArgumentNotValidException ex) {

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

    // ✅ Message utilisateur principal
    String message =
        errors.values().stream().findFirst().orElse("Les données envoyées sont invalides.");

    log.warn("Validation error: {}", errors);

    return ResponseEntity.badRequest()
        .body(new ResponseDTO<>("VALIDATION_ERROR", message, errors, LocalDateTime.now()));
  }

  // 🧩 Mauvais type (ex: ID non numérique dans l’URL)
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ResponseDTO<Void>> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex) {
    String message = "Type de paramètre invalide pour '" + ex.getName() + "'";
    log.warn("Type mismatch: {}", message);
    return ResponseEntity.badRequest()
        .body(new ResponseDTO<>("TYPE_MISMATCH", message, null, LocalDateTime.now()));
  }

  // 🧠 Erreurs de validation métier (Business rules)
  @ExceptionHandler(BusinessValidationException.class)
  public ResponseEntity<ResponseDTO<Void>> handleBusinessValidation(
      BusinessValidationException ex) {
    log.warn("Business validation error: {}", ex.getMessage());
    return ResponseEntity.badRequest()
        .body(new ResponseDTO<>("VALIDATION_ERROR", ex.getMessage(), null, LocalDateTime.now()));
  }

  // 💣 Cas inattendu (NullPointerException, SQL, etc.)
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ResponseDTO<Void>> handleGeneric(Exception ex) {
    log.error("Unexpected error:", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            new ResponseDTO<>(
                "INTERNAL_ERROR", "Erreur interne du serveur.", null, LocalDateTime.now()));
  }

  // 🧩 Fallback ultime : capture les erreurs non interceptées (conversion, content-type...)
  @ExceptionHandler(HttpMessageNotWritableException.class)
  public ResponseEntity<ResponseDTO<Void>> handleConverterIssue(
      HttpMessageNotWritableException ex) {
    log.error("Erreur de conversion HTTP (fallback): {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            new ResponseDTO<>(
                "INTERNAL_ERROR",
                "Erreur de conversion de réponse JSON.",
                null,
                LocalDateTime.now()));
  }

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<ResponseDTO<Void>> handleAccessDenied(AuthorizationDeniedException ex) {
    log.warn("Accès refusé : {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(
            ResponseDTO.error("Accès refusé : vous n'êtes pas autorisé à effectuer cette action"));
  }
}
