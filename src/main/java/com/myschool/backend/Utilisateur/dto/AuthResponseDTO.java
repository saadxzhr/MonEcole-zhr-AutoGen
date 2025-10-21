package com.myschool.backend.Utilisateur.dto;


import lombok.*;

 

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    private String token;
    private String username;
    private String role;
    private Boolean forceChangePassword;
    private String redirectUrl;
}
