package com.myschool.backend.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.myschool.backend.Model.EmploiDuTemps;
import com.myschool.backend.Model.EtatDavancement;
import com.myschool.backend.Model.Matiere;
import com.myschool.backend.Repository.EtatDavancementRepository;
import com.myschool.backend.Repository.MatiereRepository;

@Service
public class CalculateProgress {

    
    @Autowired
    private EtatDavancementRepository etatDavancementRepository;

    @Autowired
    private MatiereRepository matiereRepository;  // Assuming you have this repository to get the total hours per Matiere


    public Map<String, Map<String, Object>> getProgressDataWithDetails() {
    // Get all EtatDavancement records with statut 'rempli' or 'a modifier'
    List<EtatDavancement> etats = etatDavancementRepository.findByStatutIn(Arrays.asList("Rempli", "A modifier"));
    
    // Create a lookup map for matieres (code -> matiere object)
    List<Matiere> allMatieres = matiereRepository.findAllInOrder();
    Map<String, Matiere> matiereMap = new HashMap<>();
    for (Matiere matiere : allMatieres) {
        matiereMap.put(matiere.getCode_matiere(), matiere);
    }
    
    // Create a map to store the completed hours per matiere
    Map<String, Double> matiereHoursMap = new HashMap<>();
    
    for (EtatDavancement etat : etats) {
        EmploiDuTemps emploi = etat.getEmploiDuTemps();
        if (emploi != null && emploi.getCode_matiere() != null) {
            double hours = calculateHours(emploi.getHeure_debut(), emploi.getHeure_fin());
            matiereHoursMap.merge(emploi.getCode_matiere(), hours, Double::sum);
        }
    }
    
    // Create detailed progress data
    Map<String, Map<String, Object>> detailedProgress = new HashMap<>();
    for (Map.Entry<String, Double> entry : matiereHoursMap.entrySet()) {
        String code_matiere = entry.getKey();
        Matiere matiere = matiereMap.get(code_matiere);
        
        if (matiere != null) {
            double totalHours = matiere.getNombre_heures();
            double completedHours = entry.getValue();
            
            if (totalHours > 0) {
                double progress = (completedHours / totalHours) * 100;
                
                Map<String, Object> details = new HashMap<>();
                details.put("progress", Math.round(progress * 100.0) / 100.0); // Round to 2 decimals
                details.put("totalHours", totalHours);
                details.put("completedHours", Math.round(completedHours * 100.0) / 100.0);
                details.put("filiere", matiere.getFiliere() != null ? matiere.getFiliere().getNom_filiere() : "Non assignée");
                
                detailedProgress.put(matiere.getNom_matiere(), details);
                
                System.out.println("Matiere: " + matiere.getNom_matiere() + 
                                 ", Progress: " + progress + "%" +
                                 ", Completed: " + completedHours + "h" +
                                 ", Total: " + totalHours + "h" +
                                 ", Filiere: " + (matiere.getFiliere() != null ? matiere.getFiliere().getNom_filiere() : "N/A"));
            }
        }
    }
    
    return detailedProgress;
}
    
    private double calculateHours(LocalTime start, LocalTime end) {
        // Calculate the difference in hours between start and end time
        return Duration.between(start, end).toHours();
    }

}

