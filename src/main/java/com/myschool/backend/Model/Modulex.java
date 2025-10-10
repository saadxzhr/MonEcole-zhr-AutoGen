package com.myschool.backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Entity
@Table(name = "modulex")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Modulex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codemodule", unique = true, nullable = false)
    private String codeModule;

    @Column(name = "nommodule", nullable = false)
    private String nomModule;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "nombreheures")
    private Integer nombreHeures;

    @Column(name = "coefficient")
    private Float coefficient;

    @Column(name = "departementdattache")
    private String departementDattache;

    @Column(name = "semestre")
    private String semestre;

    @Column(name = "optionmodule")
    private String optionModule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordonateur", referencedColumnName = "cin")
    private Employe coordonateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codefiliere", referencedColumnName = "codefiliere")
    private Filiere filiere;
}
