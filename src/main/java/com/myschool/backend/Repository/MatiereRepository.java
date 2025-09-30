package com.myschool.backend.Repository;





import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.myschool.backend.Model.Matiere;


public interface MatiereRepository extends JpaRepository<Matiere, Long> {

    @Query("SELECT m FROM Matiere m WHERE m.code_matiere = :code")
    Matiere findByCodematiere(@Param("code") String code_matiere);

    @Query("SELECT m FROM Matiere m ORDER BY m.code_filiere ASC")
    List<Matiere> findAllInOrder();

 
 
    @Query("SELECT m FROM Matiere m WHERE m.code_matiere = :code")
    Optional<Matiere> findByCodeMatiereCustom(@Param("code") String code);
    

}
