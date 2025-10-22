package com.myschool.backend.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 🌐 Swagger / OpenAPI Configuration
 * -------------------------------------------------------
 * Adapté au projet MySchoolManager Mothership :
 * - Sécurisé par JWT RSA (RS256)
 * - Compatible avec Swagger UI et Spring Boot 3.5.6
 * - Prêt pour une utilisation React / Microservices
 * -------------------------------------------------------
 * @author Saad
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI mySchoolManagerOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("🎓 MySchoolManager API — Mothership Core")
                        .version("v1.0.0")
                        .description("""
                                Backend centralisé du projet **MySchoolManager Mothership**.
                                
                                🔐 Authentification : JWT RS256 (RSA)
                                📦 Modules : Utilisateur, Employé, Filière, Modulex, Matière, etc.
                                🧩 Extensible : Microservices additionnels (étudiants, paiements...)
                                
                                > Base solide, propre, évolutive et prête pour production.
                                """)
                        .contact(new Contact()
                                .name("Saad - Développeur")
                                .url("https://github.com/saadxzhr")
                                .email("contact@myschoolmanager.ma"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                // Sécurité : Bearer JWT
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components().addSecuritySchemes(securitySchemeName,
                        new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Utiliser le token d’authentification obtenu via `/api/v1/auth/login`.\nNe pas inclure le préfixe 'Bearer ' ici.")
                ));
    }
}
