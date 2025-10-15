package com.myschool.backend.Modulex;

import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ModulexDTO {
    private Long id;
    
    
    @Schema(description = "Code unique du module")
    @NotBlank(message = "Le code du module est obligatoire")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Code Module ne doit contenir que lettres, chiffres, - ou _")
    private String codeModule;

    @Schema(description = "Nom du module")
    @NotBlank(message = "Le nom du module est obligatoire")
    private String nomModule;

    private String description;

    @Schema(description = "Nombre heures du module")
    @NotNull(message = "La duree est obligatoire")
    private Float nombreHeures;

    @Schema(description = "Score du module")
    @NotNull(message = "Le 'Score' est obligatoire")
    private Float coefficient;

    private String departementDattache;

    @Schema(description = "Coordinateur du module")
    @NotBlank(message = "Selectionner un Coordinateur!")
    private String coordinateurCin;

    private String coordinateurNomPrenom;

    @Schema(description = "Semestre du module")
    @NotNull(message = "Le Semestre est obligatoire")
    private Integer semestre;

    private String optionModule;

    @Schema(description = "Filiere du module")
    @NotBlank(message = "Selectionner une filière!")
    private String codeFiliere;

    private String nomFiliere;
}
