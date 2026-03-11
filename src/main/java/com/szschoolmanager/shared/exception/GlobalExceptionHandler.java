package com.szschoolmanager.shared.exception;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.servlet.http.HttpServletRequest;

import com.szschoolmanager.shared.dto.ErrorResponse;

/**
 * 🌍 GlobalExceptionHandler Centralise toutes les exceptions pour fournir des réponses API
 * cohérentes. Chaque erreur renvoie un objet ResponseDTO standardisé.
 */
@Slf4j
@RestControllerAdvice //combines @ControllerAdvice + @ResponseBody so you don't need @ResponseBody on each handler method
public class GlobalExceptionHandler {


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(401)
                        .error("BAD_CREDENTIALS")
                        .message("Identifiants invalides")
                        .path(request.getRequestURI())
                        .build());
    }



  // Doublon
  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ErrorResponse> handleDuplicate(
          DuplicateResourceException ex,
          HttpServletRequest request) {

      log.warn("Duplicate error: {}", ex.getMessage());

      return ResponseEntity.status(HttpStatus.CONFLICT)
              .body(
                      ErrorResponse.builder()
                              .timestamp(LocalDateTime.now())
                              .status(HttpStatus.CONFLICT.value())
                              .error("DUPLICATE_RESOURCE")
                              .message(ex.getMessage())
                              .path(request.getRequestURI())
                              .build()
              );
  }


  //Plusieurs échecs
    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLocked(
            AccountLockedException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(429)
                        .error("ACCOUNT_LOCKED")
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .build());
    }

  // Non trouvée
  @ExceptionHandler({ResourceNotFoundException.class, EntityNotFoundException.class})
  public ResponseEntity<ErrorResponse> handleNotFound(
          Exception ex,
          HttpServletRequest request) {
      log.warn("Resource not found: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(
                      ErrorResponse.builder()
                              .timestamp(LocalDateTime.now())
                              .status(HttpStatus.NOT_FOUND.value())
                              .error("NOT_FOUND")
                              .message(ex.getMessage())
                              .path(request.getRequestURI())
                              .build()
              );
  }

  // Erreurs de validation DTO
  @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
        MethodArgumentNotValidException ex,
        HttpServletRequest request) {

    Map<String, String> errors = new HashMap<>();

    ex.getBindingResult()
        .getFieldErrors()
        .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

    String message = errors.size() + " validation error(s)";

    log.warn("Validation error: {}", errors);

    return ResponseEntity.badRequest()
        .body(
            ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message(message)
                .errors(errors)
                .path(request.getRequestURI())
                .build()
        );
    }



  // Mauvais type
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String message = "Type de paramètre invalide pour '" + ex.getName() + "'";

        log.warn("Type mismatch: {}", message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error("TYPE_MISMATCH")
                    .message(message)
                    .path(request.getRequestURI())
                    .build()
            );
    }


  // Erreurs de validation métier
  @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessValidation(
            BusinessValidationException ex,
            HttpServletRequest request) {

        log.warn("Business validation error: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error("BUSINESS_VALIDATION_ERROR")
                    .message(ex.getMessage())
                    .path(request.getRequestURI())
                    .build()
            );
    }



  // Cas inattendu
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(
          Exception ex,
          HttpServletRequest request) {

      log.error("Unexpected error:", ex);

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(
                      ErrorResponse.builder()
                              .timestamp(LocalDateTime.now())
                              .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                              .error("INTERNAL_ERROR")
                              .message("Erreur interne du serveur.")
                              .path(request.getRequestURI())
                              .build()
              );
  }

  // Fallback ultime : capture les erreurs non interceptées
  @ExceptionHandler(HttpMessageNotWritableException.class)
  public ResponseEntity<ErrorResponse> handleConverterIssue(
          HttpMessageNotWritableException ex,
          HttpServletRequest request) {

      log.error("Erreur de conversion HTTP (fallback): {}", ex.getMessage());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(
                      ErrorResponse.builder()
                              .timestamp(LocalDateTime.now())
                              .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                              .error("INTERNAL_ERROR")
                              .message("Erreur de conversion de réponse JSON.")
                              .path(request.getRequestURI())
                              .build()
              );
  }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AuthorizationDeniedException ex,
            HttpServletRequest request) {

        log.warn("Accès refusé : {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(
                        ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.FORBIDDEN.value())
                                .error("FORBIDDEN")
                                .message("Accès refusé : vous n'êtes pas autorisé à effectuer cette action")
                                .path(request.getRequestURI())
                                .build()
                );
    }


}
