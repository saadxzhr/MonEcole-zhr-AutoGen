package com.myschool.backend.Contoller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myschool.backend.Model.Employe;
// import com.myschool.backend.Model.EtatDavancement;
// import com.myschool.backend.Model.Filiere;
import com.myschool.backend.Model.MyAppUser;
import com.myschool.backend.Repository.EmployeRepository;
// import com.myschool.backend.Repository.EtatDavancementRepository;
// import com.myschool.backend.Repository.FiliereRepository;
// import com.myschool.backend.Repository.MatiereRepository;
import com.myschool.backend.Repository.MyAppUserRepository;
// import com.myschool.backend.Service.CalculateProgress;
import com.myschool.backend.Service.EmployeService;
// import com.myschool.backend.Service.EtatDavancementService;
// import com.myschool.backend.Service.FiliereService;
// import com.myschool.backend.Service.MatiereService;
// import com.myschool.backend.Service.excelService;
// import com.myschool.backend.Model.Matiere;
// import com.myschool.backend.Service.excelService;
import com.myschool.backend.Service.MyAppUserService;


@Controller
public class DirectionController {
    
    // @Autowired
    // private excelService excelService;
    
    // @Autowired
    // private EtatDavancementService etatDavancementService;

    // @Autowired
    // private EtatDavancementRepository etatDavancementRepository;

    @Autowired
    private MyAppUserRepository myAppUserRepository;

    @Autowired
    private MyAppUserService myAppUserService;

    // @Autowired
    // private FiliereRepository filiereRepository;

    // @Autowired
    // private FiliereService filiereService;

    // @Autowired
    // private MatiereRepository matiereRepository;

    // @Autowired
    // private MatiereService matiereService;

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private EmployeService employeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // @Autowired
    // private CalculateProgress CalculateProgress;

    @GetMapping("/")
    public String redirectToDirection() {
        return "fragments/direction/accueildirection :: content";
    }

    //charger l'interface direction en affichant le nom de l'utilisateur
    @GetMapping("/req/direction")
    public String getSecretairePage(Model model, Principal principal) {
        String cin = principal.getName(); // This is the current user's CIN

        employeService.getEmployeByCin(cin).ifPresent(employe -> {
            String fullName = employe.getNom() + " " + employe.getPrenom();
            model.addAttribute("employeName", fullName);

        }); 
        return "direction";
    }

    //charger sidebar 'fragment'
    @GetMapping("/req/direction/sidebar")
    public String SecSidebar(Model model) {
        return "fragments/direction/sidebar :: content";
    }




    // //accueil de direction
    // @GetMapping("/req/accueildirection")
    // public String getAccueil(Model model, Principal principal) throws JsonProcessingException {
    //     //Donnee etat d'avacement
    //     List<EtatDavancement> amodifier = etatDavancementService.getByStatut("A modifier");
    //     List<EtatDavancement> enAttente = etatDavancementService.getByStatut("En attente");
    //     model.addAttribute("etatdavancementAmodifier", amodifier);
    //     model.addAttribute("etatdavancementEnAttente", enAttente);

    //     //Donnee listes entites
    //     List<Employe> employes = employeRepository.findAll();
    //     model.addAttribute("employes", employes);
    //     List<Matiere> matieres = matiereRepository.findAll();
    //     model.addAttribute("matieres", matieres);
    //     List<Filiere> filieres = filiereRepository.findAll();
    //     model.addAttribute("filieres", filieres);

    //     //
    //     Map<String, Map<String, Object>> detailedProgressData = CalculateProgress.getProgressDataWithDetails();
    //     System.out.println("Detailed progress data: " + detailedProgressData);
        
    //     try {
    //         ObjectMapper mapper = new ObjectMapper();
    //         String progressDataJson = mapper.writeValueAsString(detailedProgressData);
    //         model.addAttribute("progressData", progressDataJson);
    //     } catch (Exception e) {
    //         System.out.println("Error converting to JSON: " + e.getMessage());
    //         model.addAttribute("progressData", "{}");
    //     }

    //     return "fragments/direction/accueildirection :: content";
    // }
     



    // //////actions sur filiere



    // //charger tout les filieres + charger les nom des formateurs
    // @GetMapping("/req/filieres")
    // public String getFilieres(Model model) {
    //     List<Filiere> filieres = filiereRepository.findAll();
    //     model.addAttribute("filieres", filieres);
    //     model.addAttribute("formateurs", filiereService.getFormateurs());
    //     model.addAttribute("niveaux", filiereRepository.getUniqueNiveau());
    //     return "fragments/direction/filieres"; 
    // }

    // //ajouter une filiere
    // @PostMapping("/req/filieres/save")
    // @ResponseBody
    // public ResponseEntity<?> saveFiliere(@RequestBody Filiere filiere) {
    //     if (filiere.getId() != null) {
    //         // Update mode
    //         Optional<Filiere> existing = filiereRepository.findById(filiere.getId());
    //         if (existing.isPresent()) {
    //             Filiere existingFiliere = existing.get();
    //             // Update all fields including code_filiere if you want to allow changing it
    //             existingFiliere.setCode_filiere(filiere.getCode_filiere());
    //             existingFiliere.setNom_filiere(filiere.getNom_filiere());
    //             existingFiliere.setNiveau(filiere.getNiveau());
    //             existingFiliere.setDuree_heures(filiere.getDuree_heures());
    //             existingFiliere.setDescription(filiere.getDescription());
    //             existingFiliere.setResponsable(filiere.getResponsable());

    //             filiereRepository.save(existingFiliere);
    //         } else {
    //             return ResponseEntity.badRequest().body("Filiere not found for update");
    //         }
    //     } else {
    //         // Create mode
    //         filiereRepository.save(filiere);
    //     }
    //     return ResponseEntity.ok().build();
    // }


    // //supprimer une filiere
    // @DeleteMapping("/req/filieres/delete/{id}")
    // @ResponseBody
    // public ResponseEntity<?> deleteFiliere(@PathVariable("id") Long id) {
    //     filiereRepository.deleteById(id);
    //     return ResponseEntity.ok().build();
    // }



    // //////actions sur matiere



    // //charger tout les filieres + charger les nom des formateurs
    // @GetMapping("/req/matieres")
    // public String getmatieres(Model model) {
    //     List<Matiere> matieres = matiereRepository.findAll();
    //     model.addAttribute("matieres", matieres);
    //     model.addAttribute("filieres", matiereService.getFilieres());
    //     return "fragments/direction/matieres"; 
    // }
 
    // //ajouter une matiere
    // @PostMapping("/req/matieres/save")
    // @ResponseBody
    // public ResponseEntity<?> saveMatiere(@RequestBody Matiere matiere) {
    //     if (matiere.getId()!= null) {
    //         // Update mode
    //         Optional<Matiere> existing = matiereRepository.findById(matiere.getId());
    //         if (existing.isPresent()) {
    //             Matiere existingMatiere = existing.get();
    //             // Update all fields including code_filiere if you want to allow changing it
    //             existingMatiere.setCode_matiere(matiere.getCode_matiere());
    //             existingMatiere.setNom_matiere(matiere.getNom_matiere());
    //             existingMatiere.setDescription(matiere.getDescription());
    //             existingMatiere.setNombre_heures(matiere.getNombre_heures());
    //             existingMatiere.setCoefficient(matiere.getCoefficient());
    //             existingMatiere.setCode_filiere(matiere.getCode_filiere());

    //             matiereRepository.save(existingMatiere);
    //         } else {
    //             return ResponseEntity.badRequest().body("Filiere not found for update");
    //         }
    //     } else {
    //         // Create mode
    //         matiereRepository.save(matiere);
    //     }
    //     return ResponseEntity.ok().build();
    // }


    // //supprimer une filiere
    // @DeleteMapping("/req/matieres/delete/{id}")
    // @ResponseBody
    // public ResponseEntity<?> deleteMatiere(@PathVariable("id") Long id) {
    //     matiereRepository.deleteById(id);
    //     return ResponseEntity.ok().build();
    // }


    //////actions sur matiere
  
    //charger tout les employes
    @GetMapping("/req/employes")
    public String getEmploye(Model model) {
        List<Employe> employes = employeRepository.findAll();
        model.addAttribute("employes", employes);
        model.addAttribute("employe", new Employe());
        model.addAttribute("roles", employeRepository.getUniqueRole());
        return "fragments/direction/employes"; 
    }

    //ajouter une employe
    @PostMapping("/req/employes/save")
    @ResponseBody
    public ResponseEntity<?> saveEmploye(@RequestBody Employe employe) {
        if (employe.getId()!= null) {
            // Update mode
            Optional<Employe> existing = employeRepository.findById(employe.getId());
            if (existing.isPresent()) {
                Employe existingEmploye = existing.get();
                // Update all fields including code_filiere if you want to allow changing it
                existingEmploye.setCin(employe.getCin());
                existingEmploye.setNom(employe.getNom());
                existingEmploye.setPrenom(employe.getPrenom());
                existingEmploye.setAdresse(employe.getAdresse());
                existingEmploye.setTelephone(employe.getTelephone());
                existingEmploye.setEmail(employe.getEmail());
                existingEmploye.setDateEmbauche(employe.getDateEmbauche());
                existingEmploye.setRole(employe.getRole());
                existingEmploye.setSpecialite(employe.getSpecialite());
                existingEmploye.setNiveauEtude(employe.getNiveauEtude());
                existingEmploye.setSalaire(employe.getSalaire());
//cin, nom, prenom, adresse, telephone, email, dateEmbauche, role, specialite, 
//niveauEtude, salaire, maxHeuresSemaine, disponibleWeekend, seulementWeekend, actif
                existingEmploye.setMaxHeuresSemaine(employe.getMaxHeuresSemaine());
                existingEmploye.setDisponibleWeekend(employe.getDisponibleWeekend());
                existingEmploye.setSeulementWeekend(employe.getSeulementWeekend());
                existingEmploye.setActif(employe.getActif());

                employeRepository.save(existingEmploye);
            } else {
                return ResponseEntity.badRequest().body("Employe not found for update");
            }
        } else {
            // Create mode
            employeRepository.save(employe);
        }
        return ResponseEntity.ok().build();
    }

    //supprimer un employe
    @DeleteMapping("/req/employes/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteEmploye(@PathVariable("id") Long id) {
        employeRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

 


    //////actions sur users
  
    //charger tout les utilisateurs
    @GetMapping("/req/users")
    public String getUsers(Model model) {
        List<MyAppUser> users = myAppUserRepository.findAll();
        model.addAttribute("utilisateurs", users);
        model.addAttribute("roles", employeRepository.getUniqueRole());
        return "fragments/direction/users"; 
    }

    //ajouter une employe
    @PostMapping("/req/users/save")
    @ResponseBody
    public ResponseEntity<?> saveUser(@RequestBody MyAppUser user) {
        if (user.getId() != null) {
            myAppUserService.updateUser(user.getId(), user);
        } else {
            myAppUserService.createUser(user);
        }
        return ResponseEntity.ok().build();
    }


    //supprimer un utilisateur
    @DeleteMapping("/req/users/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        myAppUserRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }





    
    

    // // ✅ Let Spring inject it via constructor
    // public DirectionController(excelService excelService) {
    //     this.excelService = excelService;
    // }

    // @PostMapping("/req/upload-excel")
    // @ResponseBody
    // public ResponseEntity<?> uploadExcelFile(@RequestParam("file") MultipartFile file) {
    //     try {
    //         excelService.importExcelData(file);
    //         return ResponseEntity.ok("✅ File uploaded and data imported successfully!");
    //     } catch (Exception e) {
    //         return ResponseEntity
    //                 .badRequest()
    //                 .body("❌ Error uploading file: " + e.getMessage());
    //     }
    // }

    // ////
    // @PostMapping("/req/etat/toggle")
    // @ResponseBody
    // public ResponseEntity<String> toggleEtat(@RequestBody Map<String, Object> payload) {
    //     try {
    //         Long id = Long.valueOf(payload.get("id").toString());
    //         // 1. Find the Etat by ID
    //         EtatDavancement etat = etatDavancementRepository.findById(id)
    //                 .orElseThrow(() -> new RuntimeException("Etat not found"));

    //         // 2. Toggle the statut
    //         if ("Rempli".equals(etat.getStatut())) {
    //             etat.setStatut("A modifier");
    //         } else if ("A modifier".equals(etat.getStatut())) {
    //             etat.setStatut("Rempli");
    //         }

    //         // 3. Save changes
    //         etatDavancementRepository.save(etat);

    //         // 4. Return the new statut as response
    //         return ResponseEntity.ok(etat.getStatut());
    //     } catch (Exception e) {
    //         return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
    //     }
    // }

    
}


    


