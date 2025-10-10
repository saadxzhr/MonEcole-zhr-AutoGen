package com.myschool.backend.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.myschool.backend.DTO.FiliereDTO;
import com.myschool.backend.Model.Filiere;
import com.myschool.backend.Projection.EmployeProjection;
import com.myschool.backend.Repository.EmployeRepository;
import com.myschool.backend.Repository.FiliereRepository;

@Service
public class FiliereService {

    private final FiliereRepository filiereRepository;
    private final EmployeRepository employeRepository;

    public FiliereService(FiliereRepository filiereRepository, EmployeRepository employeRepository) {
        this.filiereRepository = filiereRepository;
        this.employeRepository = employeRepository;
    }

    public List<FiliereDTO> getAllFilieres() {
        return filiereRepository.findAll().stream().map(f -> {
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
                employeRepository.findByCin(f.getResponsableCin()).ifPresent(emp -> {
                    dto.setResponsableNomComplet(emp.getNom() + " " + emp.getPrenom());
                });
            }

            return dto;
        }).collect(Collectors.toList());
    }

    public Filiere createFiliere(Filiere filiere) {
        return filiereRepository.save(filiere);
    }

    public Filiere updateFiliere(Long id, Filiere filiere) {
        Filiere existing = filiereRepository.findById(id)
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

    public List<EmployeProjection> getEmployesProjection() {
        return employeRepository.findAllEmployes();
    }

    public List<String> getUniqueNiveaux() {
        return filiereRepository.getUniqueNiveau();
    }

    public Filiere getByCode(String codeFiliere) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getByCode'");
    }

    
}
