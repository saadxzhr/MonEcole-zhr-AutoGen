package com.myschool.backend.Service;

import java.util.List;
import lombok.RequiredArgsConstructor;
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



@Service
@RequiredArgsConstructor
@Transactional
public class ModulexService {

    private final ModulexRepository modulexRepository;
    private final ModulexMapper mapper;
    private final EmployeRepository employeRepository;

    public List<ModulexDTO> getAllModules(String filiereCode, String coordonateurCin, String departement) {
        return modulexRepository.findAllWithFilters(filiereCode, coordonateurCin, departement);
    }

    public ModulexDTO getModuleById(Long id) {
        Modulex m = modulexRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Modulex not found"));
        return mapper.toDto(m);
    }

    public ModulexDTO createModule(ModulexDTO dto, Filiere filiere) {
        Modulex entity = mapper.toEntity(dto);
        entity.setFiliere(filiere);
        if (dto.getCoordonateurCin() != null) {
            Employe coord = employeRepository.findByCin(dto.getCoordonateurCin())
                    .orElseThrow(() -> new RuntimeException("Coordonateur not found"));
            entity.setCoordonateur(coord);
        }
        Modulex saved = modulexRepository.save(entity);
        return mapper.toDto(saved);
    }

    public ModulexDTO updateModule(Long id, ModulexDTO dto, Filiere filiere) {
        Modulex entity = modulexRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Modulex not found"));
        mapper.updateEntityFromDto(dto, entity);
        entity.setFiliere(filiere);
        if (dto.getCoordonateurCin() != null) {
            Employe coord = employeRepository.findByCin(dto.getCoordonateurCin())
                    .orElseThrow(() -> new RuntimeException("Coordonateur not found"));
            entity.setCoordonateur(coord);
        }
        Modulex saved = modulexRepository.save(entity);
        return mapper.toDto(saved);
    }

    public void deleteModule(Long id) {
        modulexRepository.deleteById(id);
    }

    public List<EmployeProjection> getEmployesProjection() {
        return employeRepository.findAllEmployes();
    }
}
