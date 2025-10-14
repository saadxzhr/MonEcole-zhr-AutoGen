package com.myschool.backend.Modulex;

import org.hibernate.annotations.BatchSize;

import com.myschool.backend.Model.Employe;
import com.myschool.backend.Model.Filiere;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
    private Float nombreHeures;

    @Column(name = "coefficient")
    private Float coefficient;

    @Column(name = "departementdattache")
    private String departementDattache;

    @Column(name = "semestre")
    private Integer semestre;

    @Column(name = "optionmodule")
    private String optionModule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinateur", referencedColumnName = "cin", nullable = false)
    private Employe coordinateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codefiliere", referencedColumnName = "codefiliere", nullable = false)
    private Filiere filiere;
}
