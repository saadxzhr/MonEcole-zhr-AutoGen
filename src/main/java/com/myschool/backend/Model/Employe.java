package com.myschool.backend.Model;


import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "employe")
public class Employe {

    // @Id  // Mark 'cin' as the primary key
    // @Column(name = "cin", unique = true, nullable = false)// Ensure 'cin' is unique and not nullable
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    private String cin;
    private String nom;
    private String prenom;
    private String adresse;
    private String telephone;
    private String email;
    private LocalDate date_embauche;
    private String role;
    private String specialite;
    private String niveau_etude;
    private Float salaire;

    public String getCin() {
        return cin;
    }
    public void setCin(String cin) {
        this.cin = cin;
    }
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public String getPrenom() {
        return prenom;
    }
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    public String getAdresse() {
        return adresse;
    }
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
    public String getTelephone() {
        return telephone;
    }
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public LocalDate getDate_embauche() {
        return date_embauche;
    }
    public void setDate_embauche(LocalDate date_embauche) {
        this.date_embauche = date_embauche;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getSpecialite() {
        return specialite;
    }
    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }
    public String getNiveau_etude() {
        return niveau_etude;
    }
    public void setNiveau_etude(String niveau_etude) {
        this.niveau_etude = niveau_etude;
    }
    public Float getSalaire() {
        return salaire;
    }
    public void setSalaire(Float salaire) {
        this.salaire = salaire;
    }

     

    // public String getNomCompletEmploye() {
    //     return nonCompletEmploye != null ? this.getNom() + " " + this.getPrenom() : null;
    // }
    
    
}