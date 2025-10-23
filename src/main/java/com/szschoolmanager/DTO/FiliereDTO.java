package com.szschoolmanager.DTO;

import com.szschoolmanager.Model.PlanningType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
