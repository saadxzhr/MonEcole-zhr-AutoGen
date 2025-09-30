package com.myschool.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.myschool.backend.Model.Employe;

import java.util.List;
import java.util.Optional;

public interface EmployeRepository extends JpaRepository<Employe, Long> {
    Optional<Employe> findByCin(String cin);

    @Query("SELECT DISTINCT e FROM Employe e WHERE e.role LIKE 'Formateur_%'")
    List<Employe> findAllFormateurs();

    @Query("SELECT DISTINCT e.role FROM Employe e")
    List<String> getUniqueRole();
    
}

