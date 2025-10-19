package com.myschool.backend.Modulex;

import com.myschool.backend.Config.DuplicateResourceException;
import com.myschool.backend.Exception.BusinessValidationException;
import com.myschool.backend.Exception.PageResponseDTO;
import com.myschool.backend.Exception.ResourceNotFoundException;
import com.myschool.backend.Model.Employe;
import com.myschool.backend.Model.Filiere;
import com.myschool.backend.Service.EmployeService;
import com.myschool.backend.Service.FiliereService;

import jakarta.persistence.OptimisticLockException;

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
            //Vérifier l’unicité du code
            if (modulexRepository.existsByCodeModule(dto.getCodeModule())) {
                throw new DuplicateResourceException(
                    "Module avec code " + dto.getCodeModule() + " existe déjà!"
                );
            }
            //Mapper DTO → Entity
            Modulex module = modulexMapper.toEntity(dto);
            //Gerrer Filiere et coordinteur
            setModuleRelationships(module, dto);

            //Sauvegarder en base
            Modulex saved = modulexRepository.save(module);
            //Mapper Entity → DTO
            return modulexMapper.toDto(saved);
        }

    // ======================================
    // UPDATE MODULE
    // ======================================
    public ModulexDTO updateModule(Long id, ModulexDTO dto) {
            //1. Récupérer le module existant
            Modulex ent = modulexRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Module non trouvé avec id : " + id));
            //2. Mettre à jour les champs simples via MapStruct
            modulexMapper.updateEntityFromDto(dto, ent);
        
            setModuleRelationships(ent, dto);
            //5. Sauvegarder et renvoyer
            Modulex saved = modulexRepository.save(ent);
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
    // PRIVATE: SET FILIERE & COORDINATEUR
    // ======================================
    private void setModuleRelationships(Modulex module, ModulexDTO dto) {
        // Filiere
        // Charger seulement si différente
            if (module.getFiliere() == null ||
                !dto.getCodeFiliere().equals(module.getFiliere().getCodeFiliere())) {
                Filiere filiere = filiereService.getByCodeFiliere(dto.getCodeFiliere());
                module.setFiliere(filiere);
            }

            // === Gestion du Coordinateur ===
            //verificatin null sur dto
            // Charger seulement si différent
            if (module.getCoordinateur() == null ||
                !dto.getCoordinateurCin().equals(module.getCoordinateur().getCin())) {
                Employe coord = employeService.getEmployeByCin(dto.getCoordinateurCin());
                module.setCoordinateur(coord);
            }
    }
}