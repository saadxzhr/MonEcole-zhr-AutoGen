package com.szschoolmanager.modulex;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.szschoolmanager.employe.Employe;
import com.szschoolmanager.filiere.Filiere;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "modulex")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
// @Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Modulex {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private Integer version;

  @Column(name = "code_module", unique = true, nullable = false)
  private String codeModule;

  @Column(name = "nom_module", nullable = false)
  private String nomModule;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "nombre_heures")
  private Float nombreHeures;

  @Column(name = "coefficient")
  private Float coefficient;

  @Column(name = "departement_dattache")
  private String departementDattache;

  @Column(name = "semestre")
  private Integer semestre;

  @Column(name = "option_module")
  private String optionModule;

  @Column(name = "code_filiere", insertable = false, updatable = false)
  private String codeFiliere;


  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "coordinateur", referencedColumnName = "cin", nullable = false)
  @JsonIgnore
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private Employe coordinateur;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "code_filiere", referencedColumnName = "codefiliere", nullable = false)
  @JsonIgnore
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private Filiere filiere;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  @PreUpdate
  private void normalizeData() {
    if (codeModule != null) codeModule = codeModule.trim();
    if (departementDattache != null) departementDattache = departementDattache.trim();
  }
}
