// package com.myschool.backend.Security;

// import java.sql.Date;

// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Component;

// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.security.Keys;
// import lombok.Value;

// @Component
// public class JwtUtil {

//     @Value("${app.jwt.secret}")
//     private String secret;

//     @Value("${app.jwt.expiration-ms}")
//     private long expirationMs;

//     public String generateToken(UserDetails userDetails) {
//         return Jwts.builder()
//                 .setSubject(userDetails.getUsername())
//                 .claim("role", userDetails.getAuthorities().iterator().next().getAuthority())
//                 .setIssuedAt(new Date())
//                 .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
//                 .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
//                 .compact();
//     }

//     public boolean validateToken(String token, UserDetails userDetails) {
//         String username = extractUsername(token);
//         return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
//     }

//     public String extractUsername(String token) {
//         return Jwts.parserBuilder()
//                 .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody()
//                 .getSubject();
//     }

//     private boolean isTokenExpired(String token) {
//         Date expiration = Jwts.parserBuilder()
//                 .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody()
//                 .getExpiration();
//         return expiration.before(new Date());
//     }
// }

