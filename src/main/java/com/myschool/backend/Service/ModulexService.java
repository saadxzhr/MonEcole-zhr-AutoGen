package com.myschool.backend.Service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.myschool.backend.DTO.ModulexDTO;
import com.myschool.backend.Mapper.ModulexMapper;
import com.myschool.backend.Model.Filiere;
import com.myschool.backend.Model.Modulex;
import com.myschool.backend.Model.Employe;
import com.myschool.backend.Repository.ModulexRepository;
import com.myschool.backend.Repository.EmployeRepository;
import com.myschool.backend.Projection.EmployeProjection;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaBuilder;




@Service
@RequiredArgsConstructor
@Transactional
public class ModulexService {

    private final ModulexRepository modulexRepository;
    private final ModulexMapper mapper;
    private final EmployeRepository employeRepository;

    public Page<ModulexDTO> getModulesPage(String filiereCode, String coordonateurCin, String departement, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return modulexRepository.findFiltered(
                (filiereCode == null || filiereCode.isBlank()) ? null : filiereCode.trim(),
                (coordonateurCin == null || coordonateurCin.isBlank()) ? null : coordonateurCin.trim(),
                (departement == null || departement.isBlank()) ? null : departement.trim(),
                pageable
        );
    }

    public ModulexDTO createModule(ModulexDTO dto, Filiere filiere) {
        Modulex entity = mapper.toEntity(dto);
        entity.setFiliere(filiere);
        if (dto.getCoordonateurCin() != null && !dto.getCoordonateurCin().isBlank()) {
            Employe e = employeRepository.findByCin(dto.getCoordonateurCin())
                    .orElseThrow(() -> new RuntimeException("Coordonateur not found: " + dto.getCoordonateurCin()));
            entity.setCoordonateur(e);
        }
        Modulex saved = modulexRepository.save(entity);
        return mapper.toDto(saved);
    }

    public ModulexDTO updateModule(Long id, ModulexDTO dto, Filiere filiere) {
        Modulex ent = modulexRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Modulex not found"));
        mapper.updateEntityFromDto(dto, ent);
        ent.setFiliere(filiere);
        if (dto.getCoordonateurCin() != null && !dto.getCoordonateurCin().isBlank()) {
            Employe e = employeRepository.findByCin(dto.getCoordonateurCin())
                    .orElseThrow(() -> new RuntimeException("Coordonateur not found: " + dto.getCoordonateurCin()));
            ent.setCoordonateur(e);
        } else {
            ent.setCoordonateur(null);
        }
        Modulex saved = modulexRepository.save(ent);
        return mapper.toDto(saved);
    }

    public void deleteModule(Long id) {
        modulexRepository.deleteById(id);
    }

    public List<com.myschool.backend.Projection.EmployeProjection> getEmployesProjection() {
        return employeRepository.findAllEmployes();
    }

    public List<String> getDistinctDepartements() {
        return modulexRepository.findDistinctDepartements();
    }
    
}
