// package com.szschoolmanager.Service;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.szschoolmanager.Model.EtatDavancement;
// import com.szschoolmanager.Repository.EtatDavancementRepository;

// import java.util.List;

// import java.util.Optional;
// import java.util.stream.Collectors;

// @Service
// public class EtatDavancementService {

//     @Autowired
//     private EtatDavancementRepository etatDavancementRepository;

//     public List<EtatDavancement> getAllEtatDavancement() {
//     return etatDavancementRepository.findAllOrderByDateAndHeureDebut();
//     }

//     public List<EtatDavancement> getByCin(String cin) {
//     return etatDavancementRepository.getByEmploiDuTempsCin(cin);
//     }

//     public List<EtatDavancement> getByCinAndStatut(String cin, String statut) {
//          return etatDavancementRepository.findByCinAndStatut(cin, statut);
//     }

//     public List<EtatDavancement> getByCinAndDateBeforeToday(String cin) {
//         return etatDavancementRepository.findByCinAndDateBeforeToday(cin);
//     }

//     //trouver enregistrement pour Remplir et modifier etat d'avancement
//     public EtatDavancement getById(Long id) {
//         return etatDavancementRepository.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Not found"));
//     }

//     //Remplir ou modifier etat d'av puis statut = 'Rempli'
//     public void updateEtat(Long id, EtatDavancement newValues) {
//         EtatDavancement existing = getById(id);

//         // Update all fields
//         existing.setType_activite(newValues.getType_activite());
//         existing.setObjectif(newValues.getObjectif());
//         existing.setDescriptif(newValues.getDescriptif());
//         existing.setStatut("Rempli");
//         existing.setObservations(newValues.getObservations());
//         existing.setSuivant(newValues.getSuivant());

//         etatDavancementRepository.save(existing);
//     }

//     //Modifier statut
//     public void updateStatut(Long id, String statut) {
//         Optional<EtatDavancement> optional = etatDavancementRepository.findById(id);
//         if (optional.isPresent()) {
//             EtatDavancement etat = optional.get();
//             etat.setStatut(statut);
//             etatDavancementRepository.save(etat);
//         }
//     }

//     //charger liste des statuts
//     public List<String> getUniqueStatuts() {
//     return etatDavancementRepository.findAll().stream()
//         .map(EtatDavancement::getStatut)
//         .distinct()
//         .collect(Collectors.toList());
//     }

//     //Trouver les etats d'av d'aujourdhui
//     public List<EtatDavancement> getAllByTodayDate() {
//         return etatDavancementRepository.findAllByTodayDate();
//     }

//     //Trouver les etats d'av a modifier
//     public List<EtatDavancement> getByStatut(String statut) {
//         return etatDavancementRepository.findByStatut(statut);
//     }

//     //Annuler modifier etat
//     public void annulerStatut(Long id) {
//         Optional<EtatDavancement> optional = etatDavancementRepository.findById(id);
//         if (optional.isPresent()) {
//             EtatDavancement etat = optional.get();
//             etat.setStatut("Rempli");
//             etatDavancementRepository.save(etat);
//         }
//     }
// }
