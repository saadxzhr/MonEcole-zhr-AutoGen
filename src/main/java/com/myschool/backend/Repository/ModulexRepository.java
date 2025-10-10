package com.myschool.backend.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.myschool.backend.Model.Modulex;
// import com.myschool.backend.Projection.ModulexProjection;

// import java.util.List;
import java.util.Optional;

@Repository
public interface ModulexRepository extends JpaRepository<Modulex, Long> {

    Optional<Modulex> findByCodeModule(String codeModule);

    // @Query(value = """
    //      SELECT 
    //         m.id AS id,
    //         m.codeModule AS codeModule,
    //         m.nomModule AS nomModule,
    //         m.description AS description,
    //         m.nombreHeures AS nombreHeures,
    //         m.coefficient AS coefficient,
    //         m.departementDattache AS departementDattache,
    //         m.optionModule AS optionModule,
    //         m.semestre AS semestre,
    //         m.coordonateur AS coordonateurCin,
    //         f.codeFiliere AS codeFiliere,
    //         f.nomFiliere AS nomFiliere
    //     FROM Modulex m
    //     LEFT JOIN Filiere f ON f.codeFiliere = m.codeFiliere
    // """, nativeQuery = true)
    // List<ModulexProjection> findAllModulexWithFiliereAndCoordonateur();
}

