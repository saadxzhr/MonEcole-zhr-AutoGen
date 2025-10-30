package com.szschoolmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @EnableJpaAuditing

@SpringBootApplication
public class BackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(BackendApplication.class, args);
  }

  class StringUtils {}
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

// spring.jpa.properties.hibernate.default_batch_fetch_size=50
// spring.jpa.open-in-view=false

//

// ./mvnw compile exec:java -Dexec.mainClass="com.szschoolmanager.tools.GenerateRSAKeys"
// mvn exec:java "-Dexec.mainClass=com.szschoolmanager.tools.GenerateRSAKeys"

// Lets you instantly check which dependencies are outdated.
// mvn versions:display-dependency-updates
// mvn versions:display-plugin-updates

// OWASP Dependency-Check
// Scans all dependencies for known security vulnerabilities (CVE database).
// mvn clean verify -DskipTests
// → Generates a report under target/dependency-check-report.html.

// mvn spotless:check	Vérifie si ton code est bien formaté
// mvn spotless:apply	Formate automatiquement tous les fichiers
// mvn clean install -Dspotless.apply.skip=false	Compile et applique le formatage


//🧩 3️⃣ DepClean 🧹 (plugin avancé et précis — recommandé)
// mvn depclean:report
// mvn depclean:clean
// mvn se.kth.castor:depclean-maven-plugin:depclean

// redis 6379


// jWT
// refresh tokens exposés en JSON → déplacer vers Secure HttpOnly cookie en prod ;

// git add .
// git commit -m "jwt optimization - reused not commited on db fixed 2"
// git push

// postman
// https://www.getpostman.com/collections/PMAK-68f69f44ad6ba20001901958-840e3aa623d3968709c4a8363b6e947778

