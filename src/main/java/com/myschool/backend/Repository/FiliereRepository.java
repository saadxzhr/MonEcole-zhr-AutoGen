package com.myschool.backend.Repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.myschool.backend.Model.Filiere;


public interface FiliereRepository extends JpaRepository<Filiere, Long> {
    @Query("SELECT DISTINCT f.niveau FROM Filiere f")
    List<String> getUniqueNiveau();
}
