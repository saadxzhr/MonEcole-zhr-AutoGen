// package com.myschool.backend.Model;

// import jakarta.persistence.*;


// @Entity
// @Table(name = "etatdavancement")
// public class EtatDavancement {
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     public Long getId() {
//         return id;
//     }

//     public void setId(Long id) {
//         this.id = id;
//     }

//     @ManyToOne
//     @JoinColumn(name = "id_emploi", referencedColumnName = "id")
//     private EmploiDuTemps emploiDuTemps;



//     @ManyToOne
//     @JoinColumn(name = "cin", referencedColumnName = "cin", insertable = false, updatable = false)
//     private Employe employe;

//     private String cin;
//     private String type_activite;
//     private String objectif;
//     private String descriptif;
//     private String statut;
//     private String observations;
//     private String suivant;
    

//     // Getters and setters
//     public EmploiDuTemps getEmploiDuTemps() {
//         return emploiDuTemps;
//     }

//     public void setEmploiDuTemps(EmploiDuTemps emploiDuTemps) {
//         this.emploiDuTemps = emploiDuTemps;
//     }    


//     public Employe getEmploye() {
//         return employe;
//     }

//     public void setEmploye(Employe employe) {
//         this.employe = employe;
//     }
//       public String getCin() {
//         return cin;
//     }

//     public void setCin(String cin) {
//         this.cin = cin;
//     }
//     public String getType_activite() {
//         return type_activite;
//     }

//     public void setType_activite(String type_activite) {
//         this.type_activite = type_activite;
//     }

//     public String getObjectif() {
//         return objectif;
//     }

//     public void setObjectif(String objectif) {
//         this.objectif = objectif;
//     }

//     public String getDescriptif() {
//         return descriptif;
//     }

//     public void setDescriptif(String descriptif) {
//         this.descriptif = descriptif;
//     }

//     public String getStatut() {
//         return statut;
//     }

//     public void setStatut(String statut) {
//         this.statut = statut;
//     }

//     public String getObservations() {
//         return observations;
//     }

//     public void setObservations(String observations) {
//         this.observations = observations;
//     }

//     public String getSuivant() {
//         return suivant;
//     }

//     public void setSuivant(String suivant) {
//         this.suivant = suivant;
//     }

//     public String getNomCompletEmploye() {
//         return employe != null ? employe.getNom() + " " + employe.getPrenom() : null;
//     }

   

    
// }
