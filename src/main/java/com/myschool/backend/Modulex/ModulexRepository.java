package com.myschool.backend.Modulex;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;




@Repository
public interface ModulexRepository extends JpaRepository<Modulex, Long> {

    //Charger table modulex utilisant DTO
    @Query("""
        SELECT new com.myschool.backend.Modulex.ModulexDTO(
            m.id,
            m.codeModule,
            m.nomModule,
            m.description,
            m.nombreHeures,
            m.coefficient,
            m.departementDattache,
            m.coordinateur.cin,
            CONCAT(m.coordinateur.nom, ' ', m.coordinateur.prenom),
            m.semestre,
            m.optionModule,
            f.codeFiliere,
            f.nomFiliere
        )
        FROM Modulex m
        LEFT JOIN m.filiere f
        LEFT JOIN m.coordinateur c
        WHERE (:filiereCode IS NULL OR :filiereCode = '' OR f.codeFiliere = :filiereCode)
          AND (:coordinateurCin IS NULL OR :coordinateurCin = '' OR c.cin = :coordinateurCin)
          AND (:departement IS NULL OR :departement = '' OR m.departementDattache = :departement)
        ORDER BY f.nomFiliere, m.nomModule
        """)
    // countQuery = ("SELECT count(m.id) FROM Modulex m LEFT JOIN m.filiere f WHERE (:filiereCode IS NULL OR :filiereCode = '' OR f.codeFiliere = :filiereCode) AND (:departement IS NULL OR :departement = '' OR m.departementDattache = :departement)")
    Page<ModulexDTO> findFiltered(
            @Param("filiereCode") String filiereCode,
            @Param("coordinateurCin") String coordinateurCin,
            @Param("departement") String departement,
            Pageable pageable);

    //Charger juste les noms des departement
    @Query("SELECT DISTINCT m.departementDattache FROM Modulex m WHERE m.departementDattache IS NOT NULL")
    List<String> findDistinctDepartements();

    //Verifier si module existe
    boolean existsByCodeModule(String codeModule);
    
}

