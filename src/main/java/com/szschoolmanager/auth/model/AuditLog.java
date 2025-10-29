// package com.szschoolmanager.auth.model;

// import jakarta.persistence.*;
// import lombok.*;
// import java.time.LocalDateTime;

// @Entity
// @Table(
//     name = "auditLogs"
//     // indexes = {
//     //     @Index(name = "idx_username", columnList = "username"),
//     //     @Index(name = "idx_action", columnList = "action"),
//     //     @Index(name = "idx_created_at", columnList = "created_at")
//     // }
// )
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// public class AuditLog {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @Column(name = "username", nullable = false)
//     private String username;

//     @Column(name = "action", nullable = false)
//     private String action; // LOGIN_SUCCESS, LOGIN_FAILED, PASSWORD_CHANGED, etc.

//     @Column(name = "ipAddress")
//     private String ipAddress;

//     @Column(name = "userAgent")
//     private String userAgent;

//     @Column(name = "details", columnDefinition = "TEXT")
//     private String details;

//     @Column(name = "severity")
//     @Enumerated(EnumType.STRING)
//     private Severity severity = Severity.INFO;

//     @Column(name = "createdAt", nullable = false)
//     private LocalDateTime createdAt;

//     @PrePersist
//     protected void onCreate() {
//         if (createdAt == null) {
//             createdAt = LocalDateTime.now();
//         }
//     }

//     public enum Severity {
//         INFO, WARNING, ERROR, CRITICAL
//     }
// }