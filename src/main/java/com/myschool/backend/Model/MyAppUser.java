package com.myschool.backend.Model;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

// import lombok.Setter;
// import lombok.Getter;
import jakarta.persistence.*;
// import java.util.Collection;
// import java.util.Collections;

// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
public class MyAppUser implements UserDetails {
    
    // @Id  // Mark 'cin' as the primary key
    // @Column(name = "username", unique = true, nullable = false) // Ensure 'cin' is unique and not nullable
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    
    
    private String username;
    private String password;
    private String role;
    

    
  
   
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        throw new UnsupportedOperationException("Unimplemented method 'getAuthorities'");
    }
   

}
