package com.szschoolmanager.auth.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurResponseDTO {
  private Long id;
  private String username;
  private String role;
  private String cin;
  private Boolean forceChangePassword;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
