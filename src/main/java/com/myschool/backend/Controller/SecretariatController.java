package com.myschool.backend.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
// import com.myschool.backend.Model.EmploiDuTemps;
// import com.myschool.backend.Model.EmployeService;
// import com.myschool.backend.Model.EtatDavancement;
// import com.myschool.backend.Repository.EmploiDuTempsRepository;
// import com.myschool.backend.Service.CalculateProgress;
// import com.myschool.backend.Service.EmploiDuTempsService;
import com.myschool.backend.Service.EmployeService;
// import com.myschool.backend.Service.EtatDavancementService;


@Controller
public class SecretariatController {
    // @Autowired
    // private EtatDavancementService etatDavancementService;

    // @Autowired
    // private EmploiDuTempsRepository emploiDuTempsRepository;

    // @Autowired
    // private EmploiDuTempsService emploiDuTempsService;

    @Autowired
    private EmployeService employeService;

    // @Autowired
    // private CalculateProgress CalculateProgress;

    //charger l'interface secretariat en affichant le nom de l'utilisateur
    @GetMapping("/req/secretariat")
    public String getsecretariatPage(Model model, Principal principal) {
        String cin = principal.getName();

        employeService.getEmployeByCinO(cin).ifPresent(employe -> {
            String fullName = employe.getNom() + " " + employe.getPrenom();
            model.addAttribute("employeName", fullName);
        }); 
        return "secretariat";
    }

    // //charger sidebar 'fragment'
    // @GetMapping("/req/secretariat/sidebar")
    // public String SecSidebar(Model model) {
    //     return "fragments/secretariat/sidebar :: content";
    // }



//     //accueil de secretariat
//     @GetMapping("/req/accueilsecretariat")
//     public String getAccueil(Model model, Principal principal) {
//         List<EtatDavancement> amodifier = etatDavancementService.getByStatut("A modifier");
//         List<EtatDavancement> enAttente = etatDavancementService.getByStatut("En attente");
//         model.addAttribute("etatdavancementAmodifier", amodifier);
//         model.addAttribute("etatdavancementEnAttente", enAttente);
        
//         Map<String, Map<String, Object>> detailedProgressData = CalculateProgress.getProgressDataWithDetails();
//         System.out.println("Detailed progress data: " + detailedProgressData);
        
//         try {
//             ObjectMapper mapper = new ObjectMapper();
//             String progressDataJson = mapper.writeValueAsString(detailedProgressData);
//             model.addAttribute("progressData", progressDataJson);
//         } catch (Exception e) {
//             System.out.println("Error converting to JSON: " + e.getMessage());
//             model.addAttribute("progressData", "{}");
//         }
//         return "fragments/secretariat/accueilsecretariat :: content";
//     }
 

//     //Charger etat d'avancement
//     @GetMapping("/req/alletat")
//     public String getEtatsdavancement(Model model) {
//         List<EtatDavancement> etat = etatDavancementService.getAllEtatDavancement();
//         model.addAttribute("etatdavancement", etat);
//         model.addAttribute("statut", etatDavancementService.getUniqueStatuts());
//         model.addAttribute("formateurs", emploiDuTempsService.getFormateurs());
//         model.addAttribute("filieres", emploiDuTempsService.getFilieres());
//         model.addAttribute("matieres", emploiDuTempsService.getMatieres());
//         return "fragments/alletat"; 
//     }

    


//     //Changer statut (etat d'avancement) a modifier
//     @PostMapping("/req/etat/statut")
//     @ResponseBody
//     public ResponseEntity<?> updateStatut(@RequestBody Map<String, String> data) {
//         try {
//             Long id = Long.parseLong(data.get("id"));
//             String statut = data.get("statut");

//             etatDavancementService.updateStatut(id, statut);
//             return ResponseEntity.ok().build();
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur : " + e.getMessage());
//         }
//     }


//     //annuler la modification (etat d'avancement)
//     @PostMapping("/req/etat/annuler")
//     @ResponseBody
//     public ResponseEntity<?> annulerStatut(@RequestBody Map<String, Object> data) {
//         try {
//             Long id = Long.parseLong(data.get("id").toString());
//             etatDavancementService.annulerStatut(id);
//             return ResponseEntity.ok().build();
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur : " + e.getMessage());
//         }
//     }


//     //charger tout emplois du temps
//     @GetMapping("/req/emplois")
//     public String getEmploiDuTemps(Model model) {
//         List<EmploiDuTemps> emplois = emploiDuTempsService.getAllEmploiDuTemps();
//         model.addAttribute("emplois", emplois);
//         model.addAttribute("filiere", emploiDuTempsService.getFilieres());
//         model.addAttribute("formateur", emploiDuTempsService.getFormateurs());
//         model.addAttribute("matiere", emploiDuTempsService.getMatieres());
//         return "fragments/emplois"; 
//     }
    
//     //Ajouter Emploi
//     @PostMapping("/req/emploi/ajouter")
//         public ResponseEntity<?> ajouterEmploi(@RequestBody EmploiDuTemps emploi) {
//             emploiDuTempsRepository.save(emploi);
//             return ResponseEntity.ok().build();
//         }

//     //Modifier Emploi
//     @PutMapping("/req/emploi/modifier/{id}")
//     public ResponseEntity<?> modifierEmploi(@PathVariable Long id, @RequestBody EmploiDuTemps emploi) {
//         emploi.setId(id);
//         emploiDuTempsRepository.save(emploi);
//         return ResponseEntity.ok().build();
//     }

//     //supprimer Emploi
//     @DeleteMapping("/req/emploi/supprimer/{id}")
//     public ResponseEntity<?> supprimerEmploi(@PathVariable Long id) {
//         emploiDuTempsRepository.deleteById(id);
//         return ResponseEntity.ok().build();
//     }

//     //Dupliquer les emplois du temps selon nombre d'heurs (condition ajouter les emplois d'une seul semaine avant appliquer)
//     @PostMapping("/generate-recurring")
//     @ResponseBody
//     public ResponseEntity<?> generateRecurring(@RequestParam("codeMatiere") String codeMatiere) {
//         try {
//             emploiDuTempsService.generateRecurringSchedule(codeMatiere);
//             return ResponseEntity.ok("✅ Recurring schedule generated for " + codeMatiere);
//         } catch (Exception e) {
//             return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
//         }
//     }
}
