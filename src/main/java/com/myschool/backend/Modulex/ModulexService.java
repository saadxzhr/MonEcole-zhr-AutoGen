package com.myschool.backend.Modulex;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.myschool.backend.Config.DuplicateResourceException;
import com.myschool.backend.DTO.PageResponseDTO;
import com.myschool.backend.Exception.BusinessValidationException;
import com.myschool.backend.Exception.ResourceNotFoundException;
import com.myschool.backend.Model.Employe;
import com.myschool.backend.Model.Filiere;
import com.myschool.backend.Service.EmployeService;
import com.myschool.backend.Service.FiliereService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;



@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ModulexService {

    private static final String ENTITY_NAME = "Module";

    private final ModulexRepository modulexRepository;
    private final ModulexMapper modulexMapper;
    private final EmployeService employeService;
    private final FiliereService filiereService;
    

        // private static final String ENTITY_NAME = "Module";
        // log.info("Creating new {}: {}", ENTITY_NAME, dto.getCodeModule());
    

        //@CacheEvict(value = "modulesPage", allEntries = true)
        // @Cacheable(
        //     value = "modulesPage",
        //     key = "T(java.util.Objects).hash(#filiereCode, #coordinateurCin, #departement, #page, #size)"
        // )
        @Transactional(readOnly = true)
        public PageResponseDTO<ModulexDTO> getModulesPage(
            String filiereCode, 
            String coordinateurCin, 
            String departement, 
            int page, 
            int size) {

            Pageable pageable = PageRequest.of(page, size, Sort.by("filiere.codeFiliere").ascending().and(Sort.by("codeModule")));
            Page<ModulexDTO> result = modulexRepository.findFiltered(filiereCode, coordinateurCin, departement, pageable);

            return new PageResponseDTO<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
            );
        }

        

        //Ajouter un module
        @Transactional
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
            log.info("Creation nouvelle {}: {}", ENTITY_NAME, dto.getCodeModule());
            //Mapper Entity → DTO
            return modulexMapper.toDto(saved);
        }


        //Modifier un module
        @Transactional
        public ModulexDTO updateModule(Long id, ModulexDTO dto) {
            //1. Récupérer le module existant
            Modulex ent = modulexRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Module non trouvé avec id : " + id));
            //2. Mettre à jour les champs simples via MapStruct
            modulexMapper.updateEntityFromDto(dto, ent);

            //Gerrer Filiere et coordinteur
            setModuleRelationships(ent, dto);

            //5. Sauvegarder et renvoyer
            Modulex saved = modulexRepository.save(ent);

            log.info("Modification de {}: {}", ENTITY_NAME, id);
            return modulexMapper.toDto(saved);
        }

        //gestion des filieres et coordinateur
        private void setModuleRelationships(Modulex module, ModulexDTO dto) {
            // === Gestion de la Filiere ===
            //verificatin null sur dto
            // if (dto.getCodeFiliere() == null || dto.getCodeFiliere().isBlank()) {
            //     throw new BusinessValidationException("Selectionner une filière!");
            // }
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


        //charger pour select/filtre
        public List<String> getDistinctDepartements() {
            return modulexRepository.findDistinctDepartements();
        }

         //supprimer un module
        @Transactional
        public void deleteModule(Long id) {
            // Vérifier si le module existe
            if (!modulexRepository.existsById(id)) {
                throw new ResourceNotFoundException("Module not found with id: " + id);
            }

            // Supprimer rapidement
            modulexRepository.deleteById(id);

            log.info("Supprimer {} avec id: {}", ENTITY_NAME, id);
        }

        

}
        // private void checkModuleDependencies(Modulex module) {
        //     boolean isUsed = scheduleRepository.existsByModule(module); // exemple : emploi du temps
        //     if (isUsed) {
        //         throw new IllegalStateException("Impossible de supprimer le module, il est utilisé dans un emploi du temps");
        //     }
        // }


        // private void checkModuleDependencies(Modulex module) {
        //     // Check if module is referenced in other tables
        //     // This is a placeholder - implement based on your business rules
        //     // Example: check if module is assigned to any schedules
            
        //     // if (scheduleRepository.existsByModule(module)) {
        //     //     throw new DataIntegrityViolationException("Impossible de supprimer le module, il est utilisé dans un emploi du temps");
        //     // }
        // }


        // public void deleteModule(Long id) {
        //     modulexRepository.deleteById(id);
        // }

        

        
 

