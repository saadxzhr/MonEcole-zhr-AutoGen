package com.szschoolmanager.Repository;

import com.szschoolmanager.Model.Filiere;
import com.szschoolmanager.projection.FiliereProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FiliereRepository extends JpaRepository<Filiere, Long> {

  @Query("SELECT DISTINCT f.niveau FROM Filiere f")
  List<String> findUniqueNiveau();

  @Query(
      "SELECT f.codeFiliere AS codeFiliere, f.nomFiliere AS nomFiliere FROM Filiere f WHERE f.codeFiliere = :codeFiliere")
  Optional<FiliereProjection> findFiliereDTOByCode(@Param("codeFiliere") String codeFiliere);

  Optional<Filiere> findByCodeFiliere(String codeFiliere);

  @Query("SELECT DISTINCT f.codeFiliere AS codeFiliere, f.nomFiliere AS nomFiliere FROM Filiere f")
  List<FiliereProjection> findFilieresProjection();

  boolean existsByCodeFiliere(String codeFiliere);
}
