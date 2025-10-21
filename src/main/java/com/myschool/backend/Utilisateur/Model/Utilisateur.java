// com/myschool/backend/utilisateur/model/Utilisateur.java
package com.myschool.backend.Utilisateur.Model;

import jakarta.persistence.*;
import lombok.*;



@Entity
@Table(name = "utilisateur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String role; // simple String, pas un enum

    @Column(nullable = false, length = 20)
    private String cin;

    @Column(name = "forceChangePassword", nullable = false)
    private Boolean forceChangePassword = true;
}