package com.myschool.backend.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.myschool.backend.Model.Employe;
import com.myschool.backend.Projection.EmployeProjection;
import com.myschool.backend.Repository.EmployeRepository;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeService {

    @Autowired
    private EmployeRepository employeRepository;

    public Optional<Employe> getEmployeByCin(String cin) {
        return employeRepository.findByCin(cin);
    }

    public List<EmployeProjection> getEmployes() {
        return employeRepository.findAllEmployes();
    }
    
}
