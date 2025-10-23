package com.szschoolmanager.Repository;

import com.szschoolmanager.Model.Employe;
import com.szschoolmanager.projection.EmployeProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmployeRepository extends JpaRepository<Employe, Long> {

  Optional<Employe> findByCin(String cin);

  @Query("SELECT e.cin AS cin, e.nom AS nom, e.prenom AS prenom FROM Employe e")
  List<EmployeProjection> findEmployesByP();

  @Query("SELECT DISTINCT e.role FROM Employe e")
  List<String> getUniqueRole();

  boolean existsByCin(String cin);
}
