package com.myschool.backend.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.myschool.backend.DTO.ModulexDTO;
import com.myschool.backend.Model.Filiere;
import com.myschool.backend.Service.FiliereService;
import com.myschool.backend.Service.ModulexService;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/req/modulex")
@RequiredArgsConstructor
public class ModulexController {

    private final ModulexService modulexService;
    private final FiliereService filiereService;

    @GetMapping
    public String page(Model model) {
        model.addAttribute("filieres", filiereService.getAllFilieres());
        model.addAttribute("employes", modulexService.getEmployesProjection());
        return "fragments/direction/modulex :: content";
    }

    @GetMapping("/api")
    @ResponseBody
    public Page<ModulexDTO> api(
            @RequestParam(required=false) String filiereCode,
            @RequestParam(required=false) String coordonateurCin,
            @RequestParam(required=false) String departement,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="30") int size) {
        return modulexService.getModulesPage(filiereCode, coordonateurCin, departement, page, size);
    }

    @PostMapping("/api")
    @ResponseBody
    public ModulexDTO create(@RequestBody ModulexDTO dto) {
        Filiere f = filiereService.getByCode(dto.getCodeFiliere());
        return modulexService.createModule(dto, f);
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ModulexDTO update(@PathVariable Long id, @RequestBody ModulexDTO dto) {
        Filiere f = filiereService.getByCode(dto.getCodeFiliere());
        return modulexService.updateModule(id, dto, f);
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public Map<String,String> delete(@PathVariable Long id) {
        modulexService.deleteModule(id);
        return Map.of("message","deleted");
    }

    @GetMapping("/api/filieres")
    @ResponseBody
    public List<Map<String,String>> filieres() {
        return filiereService.getAllFilieres().stream()
                .map(f -> Map.of("code", f.getCodeFiliere(), "label", f.getNomFiliere()))
                .toList();
    }

    @GetMapping("/api/employes")
    @ResponseBody
    public List<Map<String,String>> employes() {
        return modulexService.getEmployesProjection().stream()
                .map(e -> Map.of("cin", e.getCin(), "label", e.getNom() + " " + e.getPrenom()))
                .toList();
    }

    @GetMapping("/api/departements")
    @ResponseBody
    public List<String> departements() {
        return modulexService.getDistinctDepartements();
    }
}
