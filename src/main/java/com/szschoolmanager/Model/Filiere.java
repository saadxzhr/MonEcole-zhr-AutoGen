package com.szschoolmanager.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "filiere")
public class Filiere {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String codeFiliere;

  @Column(nullable = false)
  private String nomFiliere;

  private String niveau;

  private Integer dureeHeures;

  private String description;

  private String responsableCin;

  @Enumerated(EnumType.STRING)
  @Column(name = "planninType", columnDefinition = "planningTypeEnum")
  private PlanningType planninType;

  private Boolean actif = true;

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
