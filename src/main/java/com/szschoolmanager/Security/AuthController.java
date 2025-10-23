// package com.szschoolmanager.Security;

// import org.springframework.http.ResponseEntity;
// import org.springframework.security.authentication.BadCredentialsException;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.szschoolmanager.Exception.ResponseDTO;
// import com.szschoolmanager.Utilisateurs.MyAppUserService;

// import lombok.Data;
// import lombok.RequiredArgsConstructor;

// @RestController
// @RequestMapping("/api/v1/auth")
// @RequiredArgsConstructor
// public class AuthController {

//     private final MyAppUserService userService;
//     private final JwtUtil jwtUtil;

//     @PostMapping("/login")
//     public ResponseEntity<ResponseDTO<String>> login(@RequestBody LoginRequest request) {
//         UserDetails userDetails = userService.loadUserByUsername(request.getUsername());

//         if (!new BCryptPasswordEncoder().matches(request.getPassword(),
// userDetails.getPassword())) {
//             throw new BadCredentialsException("Mot de passe incorrect");
//         }

//         String token = jwtUtil.generateToken(userDetails);
//         return ResponseEntity.ok(ResponseDTO.success("Connexion réussie", token));
//     }
// }

// @Data
// class LoginRequest {
//     private String username;
//     private String password;
// }
