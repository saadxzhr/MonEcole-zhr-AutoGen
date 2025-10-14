package com.myschool.backend.Modulex;

import jakarta.validation.constraints.NotBlank;
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
    private String codeModule;
    private String nomModule;
    private String description;
    private Float nombreHeures;
    private Float coefficient;
    private String departementDattache;
    @NotBlank private String coordinateurCin;
    private String coordinateurNomPrenom;
    private Integer semestre;
    private String optionModule;
    @NotBlank private String codeFiliere;
    private String nomFiliere;
}
