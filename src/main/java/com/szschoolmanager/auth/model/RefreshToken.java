package com.szschoolmanager.auth.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "refreshtoken")
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


  @Column(nullable = false, length = 100)
  private String userAgent;

  @Column(nullable = false, length = 100)
  private String ipAddress;

  @Column(nullable = false, unique = true, length = 255)
  private String jti;

  @Builder.Default
  @Column(nullable = false)
  private boolean reused = false;

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiresAt);
  }
}
