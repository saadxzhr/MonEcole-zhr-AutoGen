package com.myschool.backend.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.myschool.backend.Service.ModulexService;
import com.myschool.backend.Service.FiliereService;
import com.myschool.backend.DTO.ModulexDTO;
import com.myschool.backend.Model.Filiere;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/req/modulex")
public class ModulexController {

    private final ModulexService modulexService;
    private final FiliereService filiereService;

    // Thymeleaf page
    @GetMapping
    public String showModulexPage(Model model) {
        model.addAttribute("filieres", filiereService.getAllFilieres());
        model.addAttribute("employes", modulexService.getEmployesProjection());
        return "fragments/direction/modulex :: content";
    }

    // JSON endpoints
    @GetMapping("/api")
    @ResponseBody
    public List<ModulexDTO> getAllModulesJson(
            @RequestParam(required = false) String filiereCode,
            @RequestParam(required = false) String coordonateurCin,
            @RequestParam(required = false) String departement) {
        return modulexService.getAllModules(filiereCode, coordonateurCin, departement);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ModulexDTO getModuleByIdJson(@PathVariable Long id) {
        return modulexService.getModuleById(id);
    }

    @PostMapping("/api")
    @ResponseBody
    public ModulexDTO createModuleJson(@RequestBody ModulexDTO dto) {
        Filiere filiere = filiereService.getByCode(dto.getCodeFiliere());
        return modulexService.createModule(dto, filiere);
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ModulexDTO updateModuleJson(@PathVariable Long id, @RequestBody ModulexDTO dto) {
        Filiere filiere = filiereService.getByCode(dto.getCodeFiliere());
        return modulexService.updateModule(id, dto, filiere);
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public Map<String, String> deleteModuleJson(@PathVariable Long id) {
        modulexService.deleteModule(id);
        return Map.of("message", "Module deleted successfully");
    }
}
