package com.myschool.backend.Service;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.myschool.backend.DTO.ModulexDTO;
import com.myschool.backend.Mapper.ModulexMapper;
import com.myschool.backend.Model.Employe;
import com.myschool.backend.Model.Filiere;
import com.myschool.backend.Model.Modulex;
import com.myschool.backend.Projection.EmployeProjection;
// import com.myschool.backend.Projection.ModulexProjection;
import com.myschool.backend.Repository.EmployeRepository;
import com.myschool.backend.Repository.ModulexRepository;
import com.myschool.backend.Service.EmployeService;




import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ModulexService {

    private final ModulexRepository modulexRepository;
    private final ModulexMapper mapper;
    private final EmployeRepository employeRepository;
    private final EmployeService employeService; // for getting full name

    public List<ModulexDTO> getAllModules() {
        return modulexRepository.findAll().stream()
                .map(modulex -> {
                    ModulexDTO dto = mapper.toDto(modulex);
                    fillCoordonateurNomPrenom(dto, modulex);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // // Return projection list directly for Thymeleaf
    // public List<ModulexProjection> getAllModulex() {
    //     return modulexRepository.findAllModulexWithFiliereAndCoordonateur();
    // }

    // Helper to get full name if needed in DTO
    public String getCoordonateurNomPrenom(String cin) {
        return employeService.getEmployeByCin(cin)
                .map(e -> e.getNom() + " " + e.getPrenom())
                .orElse("");
    }

    public ModulexDTO getModuleById(Long id) {
        Modulex modulex = modulexRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Modulex not found"));
        ModulexDTO dto = mapper.toDto(modulex);
        fillCoordonateurNomPrenom(dto, modulex);
        return dto;
    }

    public ModulexDTO createModule(ModulexDTO dto, Filiere filiere) {
        Modulex entity = mapper.toEntity(dto);
        entity.setFiliere(filiere);

        Employe coordonateur = employeRepository.findByCin(dto.getCoordonateurCin())
                .orElseThrow(() -> new RuntimeException("Coordonateur not found: " + dto.getCoordonateurCin()));
        entity.setCoordonateur(coordonateur);

        Modulex saved = modulexRepository.save(entity);

        ModulexDTO savedDto = mapper.toDto(saved);
        fillCoordonateurNomPrenom(savedDto, saved);
        return savedDto;
    }


    public ModulexDTO updateModule(Long id, ModulexDTO dto, Filiere filiere) {
    Modulex entity = modulexRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Modulex not found"));

    entity.setNomModule(dto.getNomModule());
    entity.setDescription(dto.getDescription());
    entity.setNombreHeures(dto.getNombreHeures());
    entity.setCoefficient(dto.getCoefficient());
    entity.setDepartementDattache(dto.getDepartementDattache());
    entity.setSemestre(dto.getSemestre());
    entity.setOptionModule(dto.getOptionModule());
    entity.setFiliere(filiere);

    // 🔥 FIXED: Fetch Employe entity before assigning
    Employe coordonateur = employeRepository.findByCin(dto.getCoordonateurCin())
            .orElseThrow(() -> new RuntimeException("Coordonateur not found: " + dto.getCoordonateurCin()));
    entity.setCoordonateur(coordonateur);

    Modulex saved = modulexRepository.save(entity);

    ModulexDTO savedDto = mapper.toDto(saved);
    fillCoordonateurNomPrenom(savedDto, saved);
    return savedDto;
}


    public void deleteModule(Long id) {
        modulexRepository.deleteById(id);
    }

    private void fillCoordonateurNomPrenom(ModulexDTO dto, Modulex modulex) {
    Employe c = modulex.getCoordonateur();
    if (c != null) {
        dto.setCoordonateurNomPrenom(c.getNom() + " " + c.getPrenom());
    } else {
        dto.setCoordonateurNomPrenom("");
    }
}


    public List<EmployeProjection> getEmployesProjection() {
        return employeRepository.findAllEmployes();
    }
}

