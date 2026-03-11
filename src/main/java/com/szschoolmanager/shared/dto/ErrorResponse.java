 package com.szschoolmanager.shared.dto;

 import java.time.LocalDateTime;
 import java.util.Map;
 import lombok.AllArgsConstructor;
 import lombok.Builder;
 import lombok.Data;
 import lombok.NoArgsConstructor;

 @Data
 @Builder
 @NoArgsConstructor
 @AllArgsConstructor
 public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;         //HTTP status code integer // populated manual
    private String error;      // Short code like NOT_FOUND
    private String message;    // Human-readable message
    private Map<String, String> errors; // Field-level Validation failure
    private String path;       // Request URI
 }
