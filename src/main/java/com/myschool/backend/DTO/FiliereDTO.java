package com.myschool.backend.DTO;


import com.myschool.backend.Model.PlanningType;


public class FiliereDTO {
    private Long id;
    private String codeFiliere;
    private String nomFiliere;
    private String niveau;
    private Integer dureeHeures;
    private String description;
    private String responsableCin;
    private String responsableNomComplet; // concat nom + prenom
    private PlanningType planninType;
    private Boolean actif;

    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCodeFiliere() {
        return codeFiliere;
    }
    public void setCodeFiliere(String codeFiliere) {
        this.codeFiliere = codeFiliere;
    }
    public String getNomFiliere() {
        return nomFiliere;
    }
    public void setNomFiliere(String nomFiliere) {
        this.nomFiliere = nomFiliere;
    }
    public String getNiveau() {
        return niveau;
    }
    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }
    public Integer getDureeHeures() {
        return dureeHeures;
    }
    public void setDureeHeures(Integer dureeHeures) {
        this.dureeHeures = dureeHeures;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getResponsableCin() {
        return responsableCin;
    }
    public void setResponsableCin(String responsableCin) {
        this.responsableCin = responsableCin;
    }
    public String getResponsableNomComplet() {
        return responsableNomComplet;
    }
    public void setResponsableNomComplet(String responsableNomComplet) {
        this.responsableNomComplet = responsableNomComplet;
    }
    public PlanningType getPlanninType() {
        return planninType;
    }
    public void setPlanninType(PlanningType planninType) {
        this.planninType = planninType;
    }
    public Boolean getActif() {
        return actif;
    }
    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    // Getters & Setters
    // ...
}
