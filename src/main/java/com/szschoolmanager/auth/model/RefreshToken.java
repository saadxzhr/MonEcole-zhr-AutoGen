package com.szschoolmanager.auth.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "refreshToken")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "utilisateurId", referencedColumnName = "id", nullable = false)
  private Utilisateur utilisateur;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  @Builder.Default
  @Column(nullable = false)
  private boolean revoked = false;

  private String userAgent;
  private String ipAddress;

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiresAt);
  }
}
