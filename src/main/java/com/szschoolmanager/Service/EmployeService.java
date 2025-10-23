package com.szschoolmanager.Service;

import com.szschoolmanager.Model.Employe;
import com.szschoolmanager.Repository.EmployeRepository;
import com.szschoolmanager.projection.EmployeProjection;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeService {

  @Autowired private EmployeRepository employeRepository;

  public Employe getEmployeByCinEx(String cin) {
    return employeRepository
        .findByCin(cin)
        .orElseThrow(() -> new EntityNotFoundException("Filière non trouvée: " + cin));
  }

  public Optional<Employe> getEmployeByCinO(String cin) {
    return employeRepository.findByCin(cin);
  }

  public List<EmployeProjection> getEmployesProjection() {
    return employeRepository.findEmployesByP();
  }

  public Employe getEmployeByCin(String cin) {
    return employeRepository
        .findByCin(cin)
        .orElseThrow(() -> new EntityNotFoundException("Employe non trouvée: " + cin));
  }

  public boolean existsByCin(String cin) {
    return employeRepository.existsByCin(cin);
  }
}
