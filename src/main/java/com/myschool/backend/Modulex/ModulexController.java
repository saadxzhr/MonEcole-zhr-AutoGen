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

import com.myschool.backend.Exception.PageResponseDTO;
import com.myschool.backend.Exception.ResponseDTO;
import com.myschool.backend.Projection.EmployeProjection;
import com.myschool.backend.Projection.FiliereProjection;
import com.myschool.backend.Service.EmployeService;
import com.myschool.backend.Service.FiliereService;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;




@CrossOrigin(origins = "*") 
@RestController
@RequestMapping("/api/v1/modulex") // ✅ Versioning + ressource
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Modules", description = "Gestion des modules académiques")
public class ModulexController {

    private final ModulexService modulexService;
    private final FiliereService filiereService;
    private final EmployeService employeService;

    // ===============================
    // PAGE VIEW
    // ===============================
    @GetMapping
    public ModelAndView modulex(Model model) {
        return new ModelAndView("fragments/direction/modulex :: content");
    }

    // ===============================
    // GET MODULES PAGINATED
    // ===============================
    @GetMapping("/list")
    @ResponseBody
    @Operation(summary = "Lister les modules", description = "Retourne une page de modules filtrée")
    public ResponseEntity<ResponseDTO<Page<ModulexDTO>>> getModules(
            @RequestParam(required = false) String filiereCode,
            @RequestParam(required = false) String coordinateurCin,
            @RequestParam(required = false) String departement,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ModulexDTO> result = modulexService.getModulesPage(
                filiereCode, coordinateurCin, departement, page, size
        );
        return ResponseEntity.ok(ResponseDTO.success("Page de modules chargée avec succès", result));
    }

    // ===============================
    // CREATE MODULE
    // ===============================
    @PostMapping
    @ResponseBody
    @PreAuthorize("hasRole('Direction')")
    @Operation(summary = "Créer un module", description = "Ajoute un nouveau module avec validation complète")
    public ResponseEntity<ResponseDTO<ModulexDTO>> createModule(
            @Valid @RequestBody ModulexDTO dto
    ) {
        log.info("Creating new module: {}", dto.getCodeModule());
        ModulexDTO created = modulexService.createModule(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success("Module créé avec succès", created));
    }

    // ===============================
    // UPDATE MODULE
    // ===============================
    @PutMapping("/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('Direction')")
    @Operation(summary = "Modifier un module", description = "Met à jour un module existant")
    public ResponseEntity<ResponseDTO<ModulexDTO>> updateModule(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody ModulexDTO dto
    ) {
        log.info("Updating module with id: {}", id);
        ModulexDTO updated = modulexService.updateModule(id, dto);
        return ResponseEntity.ok(ResponseDTO.success("Module mis à jour avec succès", updated));
    }

    // ===============================
    // DELETE MODULE
    // ===============================
    @DeleteMapping("/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('Direction')")
    @Operation(summary = "Supprimer un module", description = "Supprime un module existant")
    public ResponseEntity<ResponseDTO<Void>> deleteModule(
            @PathVariable @Valid  @NotNull Long id
    ) {
        log.info("Deleting module with id: {}", id);
        modulexService.deleteModule(id);
        return ResponseEntity.ok(ResponseDTO.success("Module supprimé avec succès", null));
    }

    // ===============================
    // SELECT OPTIONS FOR FILTERING
    // ===============================
    @GetMapping("/filieres")
    @ResponseBody
    @Operation(summary = "Liste des filières")
    public ResponseEntity<List<FiliereProjection>> getFilieres() {
        return ResponseEntity.ok(filiereService.getFilieresProjection());
    }

    @GetMapping("/employes")
    @ResponseBody
    @Operation(summary = "Liste des coordinateurs")
    public ResponseEntity<List<EmployeProjection>> getEmployes() {
        return ResponseEntity.ok(employeService.getEmployesProjection());
    }

    @GetMapping("/departements")
    @ResponseBody
    @Operation(summary = "Liste des départements")
    public ResponseEntity<List<String>> getDepartements() {
        return ResponseEntity.ok(modulexService.getDistinctDepartements());
    }
}
