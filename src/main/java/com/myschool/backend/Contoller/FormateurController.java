

package com.myschool.backend.Contoller;

import com.myschool.backend.Model.EmploiDuTemps;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.myschool.backend.Model.EtatDavancement;
import com.myschool.backend.Service.EmploiDuTempsService;
import com.myschool.backend.Service.EmployeService;
import com.myschool.backend.Service.EtatDavancementService;


@Controller
public class FormateurController {

    @Autowired
    private EtatDavancementService etatDavancementService;

    @Autowired
    private EmploiDuTempsService emploiDuTempsService;

    @Autowired
    private EmployeService employeService;


    //charger l'interface formateurchant le nom de l'formateur
    @GetMapping("/req/formateur")
    public String getFormateurPage(Model model, Principal principal) {
        String cin = principal.getName();

        employeService.getEmployeByCin(cin).ifPresent(employe -> {
            String fullName = employe.getNom() + " " + employe.getPrenom();
            model.addAttribute("employeName", fullName);
        }); 
        return "formateur";
    }


    //charger sidebar 'fragment'
    @GetMapping("/req/formateur/sidebar")
    public String sidebar(Model model) {
        return "fragments/formateur/sidebar :: content";
    }


    //charger accueil > charger etat d'av a remplir ou modifier par cin et par statut
    @GetMapping("/req/accueil")
    public String getAccueil(Model model, Principal principal) {
        String cin = principal.getName();
        List<EtatDavancement> enAttente = etatDavancementService.getByCinAndStatut(cin, "En attente");
        List<EtatDavancement> toupdate = etatDavancementService.getByCinAndStatut(cin, "A modifier");
        model.addAttribute("etatdavancementEnAttente", enAttente);
        model.addAttribute("etatdavancementUpdate", toupdate);
        return "fragments/formateur/accueil :: content";
    }
    

    //charger historique (charge que les enregistrement avant 'today' grace a getByCinAndDateBeforeToday dans repository)
    @GetMapping("/req/etatsdavancement")
    public String getHistorique(Model model, Principal principal) {
        String cin = principal.getName();
        List<EtatDavancement> older = etatDavancementService.getByCinAndDateBeforeToday(cin);
        older.forEach(e -> System.out.println(e.getEmploiDuTemps().getDate()));
        model.addAttribute("etatdavancementHist", older); 
        return "fragments/formateur/etatsdavancement :: content";
    }

    //charger l'emploi du temps dans le fragment seulement du formateur en cours
    @GetMapping("/req/emploi")
    public String getEmploiForCurrentUser(Model model, Principal principal) {
        String cin = principal.getName();
        List<EmploiDuTemps> emploi = emploiDuTempsService.getEmploiDuTempsByCin(cin);
        model.addAttribute("emplois", emploi);
        return "fragments/formateur/emploi :: content";
    }


    // Remplir/modifier etat d'avancement
    
    //Get pour charger l'etat d'av
    @GetMapping("/req/update/{id}")
    public String showUpdateForm(@PathVariable Long id, Model model) {
        EtatDavancement etat = etatDavancementService.getById(id);
        model.addAttribute("etat", etat);
        return "fragments/formateur/update :: content";
    }

    //post pour enregistrer les nvlles valeurs
    @PostMapping("/req/update/{id}")
    public String updateEtat(@PathVariable Long id, @ModelAttribute EtatDavancement updatedEtat) {
        etatDavancementService.updateEtat(id, updatedEtat);
        return "redirect:/req/formateur";
    }

}
