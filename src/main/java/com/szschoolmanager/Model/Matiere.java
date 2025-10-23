// package com.szschoolmanager.Model;

// import jakarta.persistence.*;

// @Entity
// @Table(name = "matiere")
// public class Matiere {

//         // @Id
//         // @Column(name = "code_matiere", unique = true, nullable = false) // Ensure 'cin' is
// unique and not nullable

//         @Id
//         @GeneratedValue(strategy = GenerationType.IDENTITY)
//         private Long id;

//         @Column(name = "code_matiere")
//         private String code_matiere;

//         private String nom_matiere;
//         private String description;
//         private int nombre_heures;
//         private float coefficient;
//         private String code_filiere;

//         @ManyToOne
//         @JoinColumn(name = "code_filiere", referencedColumnName = "code_filiere", insertable =
// false, updatable = false)
//         private Filiere filiere;

//         public Filiere getFiliere() {
//             return filiere;
//         }

//         public Long getId() {
//                     return id;
//                 }

//         public void setId(Long id) {
//             this.id = id;
//         }
//         public String getCode_matiere() {
//             return code_matiere;
//         }

//         public void setCode_matiere(String code_matiere) {
//             this.code_matiere = code_matiere;
//         }

//         public String getNom_matiere() {
//             return nom_matiere;
//         }

//         public void setNom_matiere(String nom_matiere) {
//             this.nom_matiere = nom_matiere;
//         }

//         public String getDescription() {
//             return description;
//         }

//         public void setDescription(String description) {
//             this.description = description;
//         }

//         public int getNombre_heures() {
//             return nombre_heures;
//         }

//         public void setNombre_heures(int nombre_heures) {
//             this.nombre_heures = nombre_heures;
//         }

//         public float getCoefficient() {
//             return coefficient;
//         }

//         public void setCoefficient(float coefficient) {
//             this.coefficient = coefficient;
//         }

//         public String getCode_filiere() {
//             return code_filiere;
//         }

//         public void setCode_filiere(String code_filiere) {
//             this.code_filiere = code_filiere;
//         }

//     }
