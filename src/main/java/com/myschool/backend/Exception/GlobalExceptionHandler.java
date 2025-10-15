package com.myschool.backend.Exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.myschool.backend.Config.DuplicateResourceException;

import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- Duplicate ---
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ResponseDTO<Void>> handleDuplicate(DuplicateResourceException ex) {
        log.warn("Duplicate error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ResponseDTO<>("DUPLICATE_RESOURCE", ex.getMessage(), null));
    }

    // --- Not found ---
    @ExceptionHandler({ResourceNotFoundException.class, EntityNotFoundException.class})
    public ResponseEntity<ResponseDTO<Void>> handleNotFound(RuntimeException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>("NOT_FOUND", ex.getMessage(), null));
    }

    // --- Validation / contrainte ---
   @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(err ->
            errors.put(err.getField(), err.getDefaultMessage())
        );

        // ✅ Récupère le premier message d’erreur lisible
        String message = errors.values().stream().findFirst().orElse("Les données envoyées sont invalides.");

        log.warn("Validation error: {}", errors);

        ResponseDTO<Map<String, String>> response = new ResponseDTO<>(
                "VALIDATION_ERROR",
                message,   // <-- ici on renvoie le vrai message utilisateur
                errors     // tu peux afficher tous les champs invalides si tu veux
        );

        return ResponseEntity.badRequest().body(response);
    }


    // --- Mauvais type (ex: id non numérique) ---
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseDTO<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Type de paramètre invalide pour '" + ex.getName() + "'";
        return ResponseEntity.badRequest()
                .body(new ResponseDTO<>("TYPE_MISMATCH", message, null));
    }

    // --- Autres erreurs inattendues ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<Void>> handleGeneric(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>("INTERNAL_ERROR", "Erreur interne du serveur.", null));
    }


    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ResponseDTO<Void>> handleBusinessValidation(BusinessValidationException ex) {
        return ResponseEntity.badRequest()
                .body(new ResponseDTO<>("VALIDATION_ERROR", ex.getMessage(), null));
    }
}
