package com.myschool.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.myschool.backend.Model.Modulex;
import com.myschool.backend.DTO.ModulexDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;




@Repository
public interface ModulexRepository extends JpaRepository<Modulex, Long> {

    @Query("""
        SELECT new com.myschool.backend.DTO.ModulexDTO(
            m.id,
            m.codeModule,
            m.nomModule,
            m.description,
            m.nombreHeures,
            m.coefficient,
            m.departementDattache,
            m.coordonateur.cin,
            CONCAT(m.coordonateur.nom, ' ', m.coordonateur.prenom),
            m.semestre,
            m.optionModule,
            f.codeFiliere,
            f.nomFiliere
        )
        FROM Modulex m
        LEFT JOIN m.filiere f
        LEFT JOIN m.coordonateur c
        WHERE (:filiereCode IS NULL OR :filiereCode = '' OR f.codeFiliere = :filiereCode)
          AND (:coordonateurCin IS NULL OR :coordonateurCin = '' OR c.cin = :coordonateurCin)
          AND (:departement IS NULL OR :departement = '' OR m.departementDattache = :departement)
        ORDER BY f.nomFiliere, m.nomModule
        """)
    Page<ModulexDTO> findFiltered(
            @Param("filiereCode") String filiereCode,
            @Param("coordonateurCin") String coordonateurCin,
            @Param("departement") String departement,
            Pageable pageable);

    @Query("SELECT DISTINCT m.departementDattache FROM Modulex m WHERE m.departementDattache IS NOT NULL")
    java.util.List<String> findDistinctDepartements();

    

    
    
}

