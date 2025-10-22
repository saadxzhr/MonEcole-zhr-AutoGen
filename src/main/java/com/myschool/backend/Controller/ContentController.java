package com.myschool.backend.controller;



import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;



@Controller
public class ContentController {

    
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }


    @RequestMapping("favicon.ico")
    public void favicon() {
        // No-op to stop 404 logs
    }

    // @GetMapping("/test")
    // public String test() {
    //     return "test";
    // }
    
    // @GetMapping("/req/signup")
    // public String signup() {
    //     return "signup";
    // }

    // @GetMapping("/index")
    // public String index() {
    //     return "index";
    // }
    @GetMapping("/formateur")
    public String formateurPage() {
        return "formateur";
    }


    @GetMapping("/fragment/accueil")
    public String accueilFragment() {
        return "fragments/formateur/accueil :: content";
    }

    @GetMapping("/fragment/sidebar")
    public String FormateurSidebar() {
        return "fragments/formateur/sidebar :: content";
    }

    @GetMapping("/fragment/emploi")
    public String emploiFragment() {
        return "fragments/formateur/emploi :: content";
    }

    @GetMapping("/req/changepass")
    public String changepass() {
        return "fragments/changepass :: content";
    }
    


    //Secretariat
    @GetMapping("/secretariat")
    public String secretariat() {
        return "secretariat";
    }

    
    @GetMapping("/secretariat/sidebar")
    public String SecSidebar() {
        return "fragments/secretariat/sidebar :: content";
    }

    @GetMapping("/alletat")
    public String allEtat() {
        return "fragments/alletat :: content";
    }

    @GetMapping("/fragment/emplois")
    public String emploisFragment() {
        return "fragments/emplois :: content";
    }

    @GetMapping("/fragment/accueilsecretariat")
    public String allEtatSlice() {
        return "fragments/accueilsecretariat :: content";
    }

    @GetMapping("/fragment/employes")
    public String employes() {
        return "fragments/employes :: content";
    }

    @GetMapping("/fragment/users")
    public String user() {
        return "fragments/users :: content";
    }

    //Direction
    @GetMapping("/direction")
    public String direction() {
        return "direction";
    }

    
    @GetMapping("/direction/sidebar")
    public String DirSidebar() {
        return "fragments/direction/sidebar :: content";
    }
}
