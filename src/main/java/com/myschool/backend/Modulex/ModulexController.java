package com.myschool.backend.Modulex;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.myschool.backend.Projection.EmployeProjection;
import com.myschool.backend.Projection.FiliereProjection;
import com.myschool.backend.Service.EmployeService;
import com.myschool.backend.Service.FiliereService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/req/modulex")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ModulexController {

    private final ModulexService modulexService;
    private final FiliereService filiereService;
    private final EmployeService employeService;


    //Charger page module
    @GetMapping
    public ModelAndView page(Model model) {
        return new ModelAndView("fragments/direction/modulex :: content");
    }


    //charger table modulex
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Page<ModulexDTO>> getModules(
            @RequestParam(required=false) String filiereCode,
            @RequestParam(required=false) String coordinateurCin,
            @RequestParam(required=false) String departement,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) {
        
            log.debug("Fetching modules with filters - filiere: {}, coord: {}, dept: {}", 
                  filiereCode, coordinateurCin, departement);

            Page<ModulexDTO> result = modulexService.getModulesPage(
                filiereCode, coordinateurCin, departement, page, size
            );
     return ResponseEntity.ok(result);
    }


    //Ajouter module
    @PostMapping("/api")
    @ResponseBody
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<ModulexDTO> createModule(@Valid @RequestBody ModulexDTO dto) {
        log.info("Creating new module: {}", dto.getCodeModule());
        ModulexDTO created = modulexService.createModule(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    //Modifer un module
    @PutMapping("/api/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<ModulexDTO> updateModule(
            @PathVariable @NotNull Long id, 
            @Valid @RequestBody ModulexDTO dto) {
        log.info("Updating module with id: {}", id);
        ModulexDTO updated = modulexService.updateModule(id, dto);
        return ResponseEntity.ok(updated);
    }

    //Supprimer un module
    @DeleteMapping("/api/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<Map<String, String>> deleteModule(@PathVariable @NotNull @Valid Long id) {
        log.info("Deleting module with id: {}", id);
        modulexService.deleteModule(id);
        return ResponseEntity.ok(Map.of("message", "Module deleted successfully"));
    }



    //Select pour filtrer

    //Nomes des filieres
    @GetMapping("/api/filieres")
    @ResponseBody
    public ResponseEntity<List<FiliereProjection>> getFilieres() {
        return ResponseEntity.ok(filiereService.getFilieresProjection());
    }

    //Nom et prenom du coordinateur
    @GetMapping("/api/employes")
    @ResponseBody
    public ResponseEntity<List<EmployeProjection>> getEmployes() {
        return ResponseEntity.ok(employeService.getEmployesProjection());
    }

    //nom des depatement
    @GetMapping("/api/departements")
    @ResponseBody
    public ResponseEntity<List<String>> getDepartements() {
        return ResponseEntity.ok(modulexService.getDistinctDepartements());
    }
}
