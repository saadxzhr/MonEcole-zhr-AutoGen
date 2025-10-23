// package com.szschoolmanager.Repository;

// import java.util.List;

// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

// import com.szschoolmanager.Model.EmploiDuTemps;

// public interface EmploiDuTempsRepository extends JpaRepository<EmploiDuTemps, Long> {
//     // Optional: add custom queries here
//         List<EmploiDuTemps> getByCin(String cin);
//         List<EmploiDuTemps> findByCin(String cin);

//         //charger l'emploi start from tday
//         @Query("SELECT e FROM EmploiDuTemps e WHERE e.cin = :cin AND e.date >= CURRENT_DATE ORDER
// BY e.date ASC, e.heure_debut ASC")
//         List<EmploiDuTemps> findByCinOrderByDateAscHeure_debutAsc(@Param("cin") String cin);

//         @Query("SELECT e FROM EmploiDuTemps e WHERE e.date >= CURRENT_DATE ORDER BY e.date,
// e.heure_debut")
//         List<EmploiDuTemps> findAllOrderByDateAndHeureDebut();

//         @Query("SELECT e FROM EmploiDuTemps e WHERE e.code_matiere = :code")
//         List<EmploiDuTemps> findByCodeMatiereCustom(@Param("code") String code);

// }
