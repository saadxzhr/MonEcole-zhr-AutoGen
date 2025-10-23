// package com.szschoolmanager.Model;

// import jakarta.persistence.*;
// import java.time.LocalDate;
// import java.time.LocalTime;

// @Entity
// @Table(name = "emploidutemps")
// public class EmploiDuTemps {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     private String jour_semaine;
//     private LocalTime heure_debut;
//     private LocalTime heure_fin;
//     private LocalDate date;
//     private String salle;
//     private String code_matiere;
//     private String cin;

//     //
//     @Column(name = "semestre", nullable = false, length = 20)
//     private String semestre;

//     @ManyToOne
//     @JoinColumn(name = "code_matiere", referencedColumnName = "code_matiere", insertable = false,
// updatable = false)
//     private Matiere matiere;

//     public Matiere getMatiere() {
//         return matiere;
//     }

//     @ManyToOne
//     @JoinColumn(name = "cin", referencedColumnName = "cin", insertable = false, updatable =
// false)
//     private Employe employe;

//     public Employe getEmploye() {
//         return employe;
//     }

//     public String getNomCompletEmploye() {
//         return employe != null ? employe.getNom() + " " + employe.getPrenom() : null;
//     }

//     public String getJour_semaine() {
//         return jour_semaine;
//     }

//     public void setJour_semaine(String jour_semaine) {
//         this.jour_semaine = jour_semaine;
//     }

//     public LocalTime getHeure_debut() {
//         return heure_debut;
//     }

//     public void setHeure_debut(LocalTime heure_debut) {
//         this.heure_debut = heure_debut;
//     }

//     public LocalTime getHeure_fin() {
//         return heure_fin;
//     }

//     public void setHeure_fin(LocalTime heure_fin) {
//         this.heure_fin = heure_fin;
//     }

//     public String getSalle() {
//         return salle;
//     }

//     public void setSalle(String salle) {
//         this.salle = salle;
//     }

//     public String getCode_matiere() {
//         return code_matiere;
//     }

//     public void setCode_matiere(String code_matiere) {
//         this.code_matiere = code_matiere;
//     }

//     public String getCin() {
//         return cin;
//     }

//     public void setCin(String cin) {
//         this.cin = cin;
//     }

//     public String getSemestre() {
//         return semestre;
//     }

//     public void setSemestre(String semestre) {
//         this.semestre = semestre;
//     }

//     public LocalDate getDate() {
//         return date;
//     }

//     public void setdate(LocalDate date) {
//         this.date = date;
//     }

//     public Long getId() {
//         return id;
//     }

//     public void setId(Long id) {
//         this.id = id;
//     }

// }
