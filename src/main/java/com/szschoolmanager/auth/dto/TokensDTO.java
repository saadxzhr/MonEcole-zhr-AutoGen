package com.szschoolmanager.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokensDTO {
    private String accessToken;
    private String refreshToken;
    private long accessExpiresIn;
    private long refreshExpiresIn;
}
