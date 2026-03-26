package com.szschoolmanager.modulex;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ModulexRepository extends JpaRepository<Modulex, Long> {

    /// KYESET Pagination
    @Query("""
    SELECT new com.szschoolmanager.modulex.ModulexDTO(
        m.id, m.codeModule, m.nomModule, m.description,
        m.nombreHeures, m.coefficient, m.departementDattache,
        c.cin, CONCAT(c.nom, ' ', c.prenom),
        m.semestre, m.optionModule, f.codeFiliere, f.nomFiliere
    )
    FROM Modulex m
    JOIN m.filiere f
    JOIN m.coordinateur c
    WHERE (:filiereCode IS NULL OR :filiereCode = '' OR f.codeFiliere = :filiereCode)
    AND (:coordinateurCin IS NULL OR :coordinateurCin = '' OR c.cin = :coordinateurCin)
    AND (:departement IS NULL OR :departement = '' OR m.departementDattache = :departement)
    AND (
      ((:filiereCode IS NOT NULL AND :filiereCode != '')
        AND m.codeModule > :lastCodeModule)
      OR
      ((:filiereCode IS NULL OR :filiereCode = '') AND (
        f.codeFiliere > :lastCodeFiliere
        OR (f.codeFiliere = :lastCodeFiliere AND m.codeModule > :lastCodeModule)
      ))
    )
    ORDER BY f.codeFiliere ASC, m.codeModule ASC
    """)
    List<ModulexDTO> findKeyset(
            @Param("filiereCode") String filiereCode,
            @Param("coordinateurCin") String coordinateurCin,
            @Param("departement") String departement,
            @Param("lastCodeFiliere") String lastCodeFiliere,
            @Param("lastCodeModule") String lastCodeModule,
            Pageable pageable);


    /// OFFSET Pagination
    @Query(
      value =
          """
                SELECT new com.szschoolmanager.modulex.ModulexDTO(
                    m.id,
                    m.codeModule,
                    m.nomModule,
                    m.description,
                    m.nombreHeures,
                    m.coefficient,
                    m.departementDattache,
                    c.cin,
                    CONCAT(c.nom, ' ', c.prenom),
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
            """,
      countQuery =
          """
                SELECT count(m.id)
                FROM Modulex m
                LEFT JOIN m.filiere f
                LEFT JOIN m.coordinateur c
                WHERE (:filiereCode IS NULL OR :filiereCode = '' OR f.codeFiliere = :filiereCode)
                AND (:coordinateurCin IS NULL OR :coordinateurCin = '' OR c.cin = :coordinateurCin)
                AND (:departement IS NULL OR :departement = '' OR m.departementDattache = :departement)
            """)
  Page<ModulexDTO> findFiltered(
      @Param("filiereCode") String filiereCode,
      @Param("coordinateurCin") String coordinateurCin,
      @Param("departement") String departement,
      Pageable pageable);

  // Charger juste les noms des departement
  @Query(
      "SELECT DISTINCT m.departementDattache FROM Modulex m WHERE m.departementDattache IS NOT NULL")
  List<String> findDistinctDepartements();

  // Verifier si module existe
  boolean existsByCodeModule(String codeModule);
}
