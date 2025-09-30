package com.myschool.backend.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.myschool.backend.Model.MyAppUser;

@Repository
public interface MyAppUserRepository extends JpaRepository<MyAppUser, Long>{
    Optional<MyAppUser> findByUsername(String username);
    
}
