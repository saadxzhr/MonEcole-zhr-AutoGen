// package com.szschoolmanager.auth.service;

// import com.szschoolmanager.auth.model.AuditLog;
// import com.szschoolmanager.auth.repository.AuditLogRepository;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.stereotype.Service;

// @Service
// @RequiredArgsConstructor
// @Slf4j
// public class AuditLogService {

//     private final AuditLogRepository auditLogRepository;

//     @Async
//     public void log(String action, String username, String ip, String userAgent) {
//         log(action, username, ip, userAgent, null, AuditLog.Severity.INFO);
//     }

//     @Async
//     public void log(String action, String username, String ip, String userAgent, 
//                     String details, AuditLog.Severity severity) {
//         try {
//             AuditLog entry = AuditLog.builder()
//                 .username(username)
//                 .action(action)
//                 .ipAddress(ip)
//                 .userAgent(userAgent)
//                 .details(details)
//                 .severity(severity)
//                 .build();
            
//             auditLogRepository.save(entry);
//             log.info("📝 Audit: {} - {} from {}", action, username, ip);
//         } catch (Exception e) {
//             log.error("❌ Failed to save audit log: {}", e.getMessage());
//         }
//     }
// }