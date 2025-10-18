package com.myschool.backend.Config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI schoolManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("School Manager API")
                        .description("API de gestion des écoles — gestion des enseignants, emplois du temps, filières, etc.")
                        .version("v1.0.0"));
    }
}
