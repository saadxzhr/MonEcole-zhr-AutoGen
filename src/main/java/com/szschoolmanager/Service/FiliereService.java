package com.szschoolmanager.Service;

import com.szschoolmanager.DTO.FiliereDTO;
import com.szschoolmanager.Model.Filiere;
import com.szschoolmanager.Repository.EmployeRepository;
import com.szschoolmanager.Repository.FiliereRepository;
import com.szschoolmanager.projection.FiliereProjection;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class FiliereService {

  private final FiliereRepository filiereRepository;
  private final EmployeRepository employeRepository;

  public FiliereService(FiliereRepository filiereRepository, EmployeRepository employeRepository) {
    this.filiereRepository = filiereRepository;
    this.employeRepository = employeRepository;
  }

  public List<FiliereDTO> getAllFilieres() {
    return filiereRepository.findAll().stream()
        .map(
            f -> {
              FiliereDTO dto = new FiliereDTO();
              dto.setId(f.getId());
              dto.setCodeFiliere(f.getCodeFiliere());
              dto.setNomFiliere(f.getNomFiliere());
              dto.setNiveau(f.getNiveau());
              dto.setDureeHeures(f.getDureeHeures());
              dto.setDescription(f.getDescription());
              dto.setResponsableCin(f.getResponsableCin());
              dto.setPlanninType(f.getPlanninType());
              dto.setActif(f.getActif());

              // Nom complet du responsable
              if (f.getResponsableCin() != null) {
                employeRepository
                    .findByCin(f.getResponsableCin())
                    .ifPresent(
                        emp -> {
                          dto.setResponsableNomComplet(emp.getNom() + " " + emp.getPrenom());
                        });
              }

              return dto;
            })
        .collect(Collectors.toList());
  }

  public Filiere createFiliere(Filiere filiere) {
    return filiereRepository.save(filiere);
  }

  public Filiere updateFiliere(Long id, Filiere filiere) {
    Filiere existing =
        filiereRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Filiere non trouvée"));
    existing.setCodeFiliere(filiere.getCodeFiliere());
    existing.setNomFiliere(filiere.getNomFiliere());
    existing.setNiveau(filiere.getNiveau());
    existing.setDureeHeures(filiere.getDureeHeures());
    existing.setDescription(filiere.getDescription());
    existing.setResponsableCin(filiere.getResponsableCin());
    existing.setPlanninType(filiere.getPlanninType());
    existing.setActif(filiere.getActif());
    return filiereRepository.save(existing);
  }

  public void deleteFiliere(Long id) {
    filiereRepository.deleteById(id);
  }

  // public List<EmployeProjection> getEmployesProjection() {
  //     return employeRepository.findEmployesByP();
  // }

  public List<String> getUniqueNiveaux() {
    return filiereRepository.findUniqueNiveau();
  }

  public List<FiliereProjection> getFilieresProjection() {
    return filiereRepository.findFilieresProjection();
  }

  public Filiere getByCodeFiliere(String code) {
    return filiereRepository
        .findByCodeFiliere(code)
        .orElseThrow(() -> new EntityNotFoundException("Filière non trouvée: " + code));
  }

  public boolean existsByCodeFiliere(String codeFiliere) {
    return filiereRepository.existsByCodeFiliere(codeFiliere);
  }
}
