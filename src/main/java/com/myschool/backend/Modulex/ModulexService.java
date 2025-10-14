package com.myschool.backend.Modulex;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.myschool.backend.Config.DuplicateResourceException;
import com.myschool.backend.Model.Employe;
import com.myschool.backend.Model.Filiere;
import com.myschool.backend.Service.EmployeService;
import com.myschool.backend.Service.FiliereService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;



@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ModulexService {

    private final ModulexRepository modulexRepository;
    private final ModulexMapper modulexMapper;
    private final EmployeService employeService;
    private final FiliereService filiereService;
    


        //@CacheEvict(value = "modulesPage", allEntries = true)
        // @Cacheable(
        //     value = "modulesPage",
        //     key = "T(java.util.Objects).hash(#filiereCode, #coordinateurCin, #departement, #page, #size)"
        // )
        @Transactional(readOnly = true)
        public Page<ModulexDTO> getModulesPage(
                String filiereCode, 
                String coordinateurCin, 
                String departement, 
                int page, 
                int size) {

            //Validation basique
            if (page < 0 || size < 1 || size > 100) {
                throw new IllegalArgumentException("Invalid pagination parameters");
            }
            //Pas besoin de Sort ici : le tri est déjà fait dans le JPQL(query Repo)
            Pageable pageable = PageRequest.of(page, size);

            //Appel direct à la requête optimisée
            return modulexRepository.findFiltered(filiereCode, coordinateurCin, departement, pageable);
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

            //Mapper Entity → DTO
            return modulexMapper.toDto(saved);
        }


        //Modifier un module
        @Transactional
        public ModulexDTO updateModule(Long id, ModulexDTO dto) {
            //1. Récupérer le module existant
            Modulex ent = modulexRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Module non trouvé avec id : " + id));
            //2. Mettre à jour les champs simples via MapStruct
            modulexMapper.updateEntityFromDto(dto, ent);

            //Gerrer Filiere et coordinteur
            setModuleRelationships(ent, dto);

            //5. Sauvegarder et renvoyer
            Modulex saved = modulexRepository.save(ent);
            return modulexMapper.toDto(saved);
        }

        //gestion des filieres et coordinateur
        private void setModuleRelationships(Modulex module, ModulexDTO dto) {
            // === Gestion de la Filiere ===
            if (dto.getCodeFiliere() == null || dto.getCodeFiliere().isBlank()) {
                throw new ConstraintViolationException("Ajouter une filière!", null);
            }
            // Charger seulement si différente
            if (module.getFiliere() == null ||
                !dto.getCodeFiliere().equals(module.getFiliere().getCodeFiliere())) {
                Filiere filiere = filiereService.getByCodeFiliere(dto.getCodeFiliere());
                module.setFiliere(filiere);
            }

            // === Gestion du Coordinateur ===
            if (dto.getCoordinateurCin() == null || dto.getCoordinateurCin().isBlank()) {
                throw new ConstraintViolationException("Ajouter un coordinateur!", null);
            }
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
                throw new EntityNotFoundException("Module not found with id: " + id);
            }

            // Supprimer rapidement
            modulexRepository.deleteById(id);

            log.info("Module deleted successfully with id: {}", id);
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

        

        
 

