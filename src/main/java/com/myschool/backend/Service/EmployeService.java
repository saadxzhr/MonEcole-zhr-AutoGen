package com.myschool.backend.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.myschool.backend.Model.Employe;
import com.myschool.backend.Model.Filiere;
import com.myschool.backend.Projection.EmployeProjection;
import com.myschool.backend.Repository.EmployeRepository;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeService {

    @Autowired
    private EmployeRepository employeRepository;

    public Employe getEmployeByCinEx(String cin) {
        return employeRepository.findByCin(cin)
            .orElseThrow(() -> new EntityNotFoundException("Filière non trouvée: " + cin));
    }

    public Optional<Employe> getEmployeByCinO(String cin) {
        return employeRepository.findByCin(cin);
    }

    public List<EmployeProjection> getEmployesProjection() {
        return employeRepository.findEmployesByP();
    }

    public Employe getEmployeByCin(String cin) {
        return employeRepository.findByCin(cin)
                .orElseThrow(() -> new EntityNotFoundException("Employe non trouvée: " + cin));
    }
}
