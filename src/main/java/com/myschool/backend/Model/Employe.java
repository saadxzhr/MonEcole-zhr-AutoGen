package com.myschool.backend.Model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employe")
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cin;

    private String nom;

    private String prenom;

    private String adresse;

    private String telephone;


    private String email;

    private LocalDate dateEmbauche;

    private String role;
    private String specialite;
    private String niveauEtude;

    private BigDecimal salaire;


    private int maxHeuresSemaine;


    private Boolean disponibleWeekend;


    private Boolean seulementWeekend;

    private Boolean actif = true;

    // =================== Getters & Setters ===================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getDateEmbauche() { return dateEmbauche; }
    public void setDateEmbauche(LocalDate dateEmbauche) { this.dateEmbauche = dateEmbauche; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getNiveauEtude() { return niveauEtude; }
    public void setNiveauEtude(String niveauEtude) { this.niveauEtude = niveauEtude; }

    public BigDecimal getSalaire() { return salaire; }
    public void setSalaire(BigDecimal salaire) { this.salaire = salaire; }

    public Integer getMaxHeuresSemaine() { return maxHeuresSemaine; }
    public void setMaxHeuresSemaine(Integer maxHeuresSemaine) { this.maxHeuresSemaine = maxHeuresSemaine; }

    public Boolean getDisponibleWeekend() { return disponibleWeekend; }
    public void setDisponibleWeekend(Boolean disponibleWeekend) { this.disponibleWeekend = disponibleWeekend; }

    public Boolean getSeulementWeekend() { return seulementWeekend; }
    public void setSeulementWeekend(Boolean seulementWeekend) { this.seulementWeekend = seulementWeekend; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }


}
