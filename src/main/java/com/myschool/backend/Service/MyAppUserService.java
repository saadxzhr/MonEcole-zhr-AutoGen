package com.myschool.backend.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.myschool.backend.Model.MyAppUser;
import com.myschool.backend.Repository.MyAppUserRepository;

import lombok.AllArgsConstructor;

@Service
public class MyAppUserService implements UserDetailsService {

    @Autowired
    public MyAppUserRepository repository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));

        // Ici on crée un PasswordEncoder qui vérifie les deux cas
        String password = user.getPassword();
        if (!password.startsWith("$2a$")) {
            // pas encore encodé
            password = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(password);
            user.setPassword(password);
            repository.save(user); // on met à jour une fois
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities);
    }

    public MyAppUser createUser(MyAppUser user) {   
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    public MyAppUser updateUser(Long id, MyAppUser user) {
        return repository.findById(id).map(existing -> {
            existing.setUsername(user.getUsername());
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                existing.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            existing.setRole(user.getRole());
            existing.setCin(user.getCin());
            return repository.save(existing);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

}
