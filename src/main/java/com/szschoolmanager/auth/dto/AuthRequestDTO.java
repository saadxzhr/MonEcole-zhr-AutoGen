package com.szschoolmanager.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRequestDTO {
  @NotBlank private String username;
  @NotBlank private String password;
}
