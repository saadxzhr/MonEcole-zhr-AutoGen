package com.szschoolmanager.employe;

import com.szschoolmanager.employe.Employe;

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
