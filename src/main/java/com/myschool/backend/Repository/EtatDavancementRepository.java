// package com.myschool.backend.Repository;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

// import com.myschool.backend.Model.EtatDavancement;

// import java.util.List;



// public interface EtatDavancementRepository extends JpaRepository<EtatDavancement, Long> {

    
//     List<EtatDavancement> getByEmploiDuTempsCin(String cin);

//     @Query("SELECT e FROM EtatDavancement e WHERE e.emploiDuTemps.cin = :cin AND e.statut = :statut AND e.emploiDuTemps.date <= CURRENT_DATE ORDER BY e.emploiDuTemps.date ASC, e.emploiDuTemps.heure_debut ASC")
//     List<EtatDavancement> findByCinAndStatut(@Param("cin") String cin, @Param("statut") String statut);

//     @Query("SELECT e FROM EtatDavancement e WHERE e.emploiDuTemps.cin = :cin AND e.emploiDuTemps.date <= CURRENT_DATE ORDER BY e.emploiDuTemps.date ASC, e.emploiDuTemps.heure_debut ASC")
//     List<EtatDavancement> findByCinAndDateBeforeToday(@Param("cin") String cin);

//     @Query("SELECT e FROM EtatDavancement e WHERE e.emploiDuTemps.date <= CURRENT_DATE ORDER BY e.emploiDuTemps.date  DESC, e.emploiDuTemps.heure_debut  DESC")
//     List<EtatDavancement> findAllOrderByDateAndHeureDebut();

//     //charger les etats d'av d'aujourdhui
//     @Query("SELECT e FROM EtatDavancement e WHERE e.emploiDuTemps.date = CURRENT_DATE ORDER BY e.emploiDuTemps.heure_debut ASC")
//     List<EtatDavancement> findAllByTodayDate();

//     //charger les etats d'av a modifier
//     @Query("SELECT e FROM EtatDavancement e WHERE e.statut = :statut AND e.emploiDuTemps.date <= CURRENT_DATE ORDER BY e.emploiDuTemps.date DESC, e.emploiDuTemps.heure_debut DESC")
//     List<EtatDavancement> findByStatut(@Param("statut") String statut);

//     List<EtatDavancement> findByStatutIn(List<String> asList);
// }
