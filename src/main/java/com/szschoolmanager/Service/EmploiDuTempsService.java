// package com.szschoolmanager.Service;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.szschoolmanager.Model.*;
// import com.szschoolmanager.Repository.EmploiDuTempsRepository;
// import com.szschoolmanager.Repository.EmployeRepository;
// import com.szschoolmanager.Repository.FiliereRepository;
// import com.szschoolmanager.Repository.MatiereRepository;

// import java.time.Duration;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.stream.Collectors;

// @Service
// public class EmploiDuTempsService {

//     private final EmploiDuTempsRepository emploiDuTempsRepository;
//     private final MatiereRepository matiereRepository;
//     private final EmployeRepository employeRepository;
//     private final FiliereRepository filiereRepository;

//     @Autowired
//     public EmploiDuTempsService(
//         EmploiDuTempsRepository emploiDuTempsRepository,
//         MatiereRepository matiereRepository,
//         EmployeRepository employeRepository,
//         FiliereRepository filiereRepository
//     ) {
//         this.emploiDuTempsRepository = emploiDuTempsRepository;
//         this.matiereRepository = matiereRepository;
//         this.employeRepository = employeRepository;
//         this.filiereRepository = filiereRepository;
//     }

//     // -----------------------------
//     // Now all your methods go here
//     // -----------------------------

//     public List<EmploiDuTemps> getAllEmploiDuTemps() {
//         return emploiDuTempsRepository.findAllOrderByDateAndHeureDebut();
//     }

//     public List<EmploiDuTemps> getByCin(String cin) {
//         return emploiDuTempsRepository.findByCin(cin);
//     }

//     public List<EmploiDuTemps> getEmploiDuTempsByCin(String cin) {
//         return emploiDuTempsRepository.findByCinOrderByDateAscHeure_debutAsc(cin);
//     }

//     public List<String> getUniqueFormateurs() {
//         return emploiDuTempsRepository.findAll().stream()
//             .map(EmploiDuTemps::getNomCompletEmploye)
//             .distinct()
//             .collect(Collectors.toList());
//     }

//     public List<String> getUniqueFilieres() {
//         return emploiDuTempsRepository.findAll().stream()
//             .map(e -> e.getMatiere().getFiliere().getNom_filiere())
//             .distinct()
//             .collect(Collectors.toList());
//     }

//     public List<String> getUniqueMatieres() {
//         return emploiDuTempsRepository.findAll().stream()
//             .map(e -> e.getMatiere().getNom_matiere())
//             .distinct()
//             .collect(Collectors.toList());
//     }

//     public List<Filiere> getFilieres() {
//         return filiereRepository.findAll();
//     }

//     public List<Matiere> getMatieres() {
//         return matiereRepository.findAll();
//     }

//     // public List<Employe> getFormateurs() {
//     //     return employeRepository.findAllFormateurs();
//     // }

//     //Dupliquer emploi du temps based on nombre d'heures toalt
//     public void generateRecurringSchedule(String code_matiere) {
//         List<EmploiDuTemps> baseSlots =
// emploiDuTempsRepository.findByCodeMatiereCustom(code_matiere);
//         if (baseSlots.isEmpty()) {
//             throw new RuntimeException("No initial schedule found for matiere: " + code_matiere);
//         }

//         Matiere matiere = matiereRepository.findByCodeMatiereCustom(code_matiere)
//                 .orElseThrow(() -> new RuntimeException("Matiere not found: " + code_matiere));

//         double totalAllowedHours = matiere.getNombre_heures();

//         // ✅ Step 1: Count already scheduled hours for this matiere
//         List<EmploiDuTemps> existingSlots =
// emploiDuTempsRepository.findByCodeMatiereCustom(code_matiere);
//         double alreadyUsedHours = 0;

//         for (EmploiDuTemps slot : existingSlots) {
//             alreadyUsedHours += Duration.between(slot.getHeure_debut(),
// slot.getHeure_fin()).toMinutes() / 60.0;
//         }

//         double remainingHours = totalAllowedHours - alreadyUsedHours;

//         if (remainingHours <= 0) {
//             throw new RuntimeException("No remaining hours for matiere: " + code_matiere);
//         }

//         // ✅ Step 2: Calculate one week's worth of planning from baseSlots
//         double baseWeekHours = 0;
//         for (EmploiDuTemps slot : baseSlots) {
//             baseWeekHours += Duration.between(slot.getHeure_debut(),
// slot.getHeure_fin()).toMinutes() / 60.0;
//         }

//         if (baseWeekHours == 0) {
//             throw new RuntimeException("Base time slots have zero total hours.");
//         }

//         // ✅ Step 3: Generate new slots weekly
//         List<EmploiDuTemps> generated = new ArrayList<>();
//         double accumulatedNewHours = 0;
//         int week = 1;

//         while (accumulatedNewHours + baseWeekHours <= remainingHours) {
//             for (EmploiDuTemps base : baseSlots) {
//                 EmploiDuTemps copy = new EmploiDuTemps();

//                 copy.setJour_semaine(base.getJour_semaine());
//                 copy.setHeure_debut(base.getHeure_debut());
//                 copy.setHeure_fin(base.getHeure_fin());
//                 copy.setCin(base.getCin());
//                 copy.setCode_matiere(base.getCode_matiere());
//                 copy.setSalle(base.getSalle());
//                 copy.setSemestre(base.getSemestre());

//                 copy.setdate(base.getDate().plusWeeks(week));

//                 generated.add(copy);

//                 accumulatedNewHours += Duration.between(
//                         base.getHeure_debut(), base.getHeure_fin()).toMinutes() / 60.0;

//                 if (accumulatedNewHours >= remainingHours) break;
//             }
//             week++;
//         }

//         emploiDuTempsRepository.saveAll(generated);
//     }
// //test Recurring
// //curl -u AB12345:AB12345 -X POST "http://localhost:8089/generate-recurring" -d
// "codeMatiere=TestEmpli2Dub"
// }
