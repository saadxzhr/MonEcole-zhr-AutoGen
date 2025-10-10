package com.myschool.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}	

// Crée la table en SQL dans Flyway (Vx__table_module.sql).
// Crée l’entité correspondante.
// Crée le repository + service.
// Crée le DTO si nécessaire (colonnes provenant d’autres tables).
// Crée les endpoints REST pour CRUD.
// JS/Thymeleaf pour afficher et gérer les formulaires.


// ✅ Entities (JPA classiques)
// ✅ Record DTOs (Java 17+)
// ✅ MapStruct (mapping auto)
// ✅ Services (logique métier)
// ✅ Controllers (REST + Thymeleaf)



//git add .
//git commit -m "Filiere almost Done, when update responsableCin not loaded"
//git push



