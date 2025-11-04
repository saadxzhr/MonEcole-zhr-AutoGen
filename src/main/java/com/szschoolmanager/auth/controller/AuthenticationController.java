package com.szschoolmanager.auth.controller;

import com.szschoolmanager.auth.dto.AuthRequestDTO;
import com.szschoolmanager.auth.dto.AuthResponseDTO;
import com.szschoolmanager.auth.service.AuthenticationService;
import com.szschoolmanager.shared.dto.ResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;




@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<AuthResponseDTO>> login(
            @Valid @RequestBody AuthRequestDTO dto,
            HttpServletRequest request,
            HttpServletResponse response) {
        return authenticationService.login(dto, request, response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseDTO<AuthResponseDTO>> refreshToken(
            @RequestHeader(value = "Refresh-Token", required = false) String headerRefresh,
            HttpServletRequest request,
            HttpServletResponse response) {
        return authenticationService.refreshToken(headerRefresh, request, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseDTO<Void>> logout(
            @RequestHeader(value = "Refresh-Token", required = false) String refreshHeader,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            HttpServletRequest request,
            HttpServletResponse response) {
        return authenticationService.logout(refreshHeader, authorizationHeader, request, response);
    }
}
