package com.szschoolmanager.Mapper;

import com.szschoolmanager.DTO.FiliereDTO;
import com.szschoolmanager.Model.Employe;
import com.szschoolmanager.Model.Filiere;

public class FiliereMapper {

  // Entity → DTO
  public static FiliereDTO toDTO(Filiere f, Employe responsable) {
    FiliereDTO dto = new FiliereDTO();
    dto.setId(f.getId());
    dto.setCodeFiliere(f.getCodeFiliere());
    dto.setNomFiliere(f.getNomFiliere());
    dto.setNiveau(f.getNiveau());
    dto.setDureeHeures(f.getDureeHeures());
    dto.setDescription(f.getDescription());

    if (responsable != null) {
      dto.setResponsableCin(responsable.getCin());
      dto.setResponsableCin(responsable.getNom() + " " + responsable.getPrenom());
    }

    return dto;
  }

  // DTO → Entity
  public static Filiere toEntity(FiliereDTO dto, Filiere existing) {
    if (existing == null) existing = new Filiere();

    existing.setCodeFiliere(dto.getCodeFiliere());
    existing.setNomFiliere(dto.getNomFiliere());
    existing.setNiveau(dto.getNiveau());
    existing.setDureeHeures(dto.getDureeHeures());
    existing.setDescription(dto.getDescription());
    existing.setResponsableCin(dto.getResponsableCin());

    return existing;
  }
}
