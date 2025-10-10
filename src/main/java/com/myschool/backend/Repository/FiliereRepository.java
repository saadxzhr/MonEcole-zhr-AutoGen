package com.myschool.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.myschool.backend.DTO.FiliereDTO;
import com.myschool.backend.Model.Employe;
import com.myschool.backend.Model.Filiere;

public interface FiliereRepository extends JpaRepository<Filiere, Long> {

    @Query("SELECT DISTINCT f.niveau FROM Filiere f")
    List<String> getUniqueNiveau();

    Optional<Filiere> findByCodeFiliere(String codeFiliere);

}
