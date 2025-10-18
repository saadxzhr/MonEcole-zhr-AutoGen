package com.myschool.backend.Modulex;

import com.myschool.backend.Config.DuplicateResourceException;
import com.myschool.backend.DTO.PageResponseDTO;
import com.myschool.backend.Exception.ResourceNotFoundException;

import com.myschool.backend.Service.EmployeService;
import com.myschool.backend.Service.FiliereService;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ModulexService {

    private static final String ENTITY_NAME = "Module";

    private final ModulexRepository modulexRepository;
    private final ModulexMapper modulexMapper;
    private final EmployeService employeService;
    private final FiliereService filiereService;

    // ======================================
    // GET PAGINATED MODULES
    // ======================================
    @Transactional(readOnly = true)
    public PageResponseDTO<ModulexDTO> getModulesPage(
            String filiereCode,
            String coordinateurCin,
            String departement,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("filiere.codeFiliere").ascending()
                        .and(Sort.by("codeModule").ascending())
        );

        Page<ModulexDTO> result = modulexRepository.findFiltered(
                filiereCode, coordinateurCin, departement, pageable
        );

        return new PageResponseDTO<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    // ======================================
    // CREATE MODULE
    // ======================================
    public ModulexDTO createModule(ModulexDTO dto) {
        validateModulex(dto, false);

        Modulex module = modulexMapper.toEntity(dto);
        setModuleRelationships(module, dto);

        Modulex saved = modulexRepository.save(module);
        return modulexMapper.toDto(saved);
    }

    // ======================================
    // UPDATE MODULE
    // ======================================
    public ModulexDTO updateModule(Long id, ModulexDTO dto) {
        Modulex existing = modulexRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ENTITY_NAME + " non trouvé avec id : " + id));

        validateModulex(dto, true);

        modulexMapper.updateEntityFromDto(dto, existing);
        setModuleRelationships(existing, dto);

        Modulex saved = modulexRepository.save(existing);
        return modulexMapper.toDto(saved);
    }

    // ======================================
    // DELETE MODULE
    // ======================================
    public void deleteModule(Long id) {
        if (!modulexRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    ENTITY_NAME + " non trouvé avec id: " + id);
        }
        modulexRepository.deleteById(id);
    }

    // ======================================
    // DISTINCT DEPARTEMENTS
    // ======================================
    @Transactional(readOnly = true)
    public List<String> getDistinctDepartements() {
        return modulexRepository.findDistinctDepartements();
    }

    // ======================================
    // PRIVATE: VALIDATION MÉTIER
    // ======================================
    private void validateModulex(ModulexDTO dto, boolean isUpdate) {
        // Vérifie unicité du code (création seulement)
        if (!isUpdate && modulexRepository.existsByCodeModule(dto.getCodeModule())) {
            throw new DuplicateResourceException(
                    "Module avec code " + dto.getCodeModule() + " existe déjà!"
            );
        }

        // Vérifie existence filière
        if (!filiereService.existsByCodeFiliere(dto.getCodeFiliere())) {
            throw new ResourceNotFoundException(
                    "Filière non trouvée : " + dto.getCodeFiliere()
            );
        }

        // Vérifie existence coordinateur
        if (!employeService.existsByCin(dto.getCoordinateurCin())) {
            throw new ResourceNotFoundException(
                    "Coordinateur non trouvé : " + dto.getCoordinateurCin()
            );
        }
    }

    // ======================================
    // PRIVATE: SET FILIERE & COORDINATEUR
    // ======================================
    private void setModuleRelationships(Modulex module, ModulexDTO dto) {
        // Filiere
        if (module.getFiliere() == null
                || !dto.getCodeFiliere().equals(module.getFiliere().getCodeFiliere())) {
            module.setFiliere(filiereService.getByCodeFiliere(dto.getCodeFiliere()));
        }

        // Coordinateur
        if (module.getCoordinateur() == null
                || !dto.getCoordinateurCin().equals(module.getCoordinateur().getCin())) {
            module.setCoordinateur(employeService.getEmployeByCin(dto.getCoordinateurCin()));
        }
    }
}
