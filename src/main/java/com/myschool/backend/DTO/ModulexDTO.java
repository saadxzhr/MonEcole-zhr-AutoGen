package com.myschool.backend.DTO;


import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModulexDTO {

    private Long id;
    private String codeModule;
    private String nomModule;
    private String description;
    private Integer nombreHeures;
    private Float coefficient;
    private String departementDattache;
    private String coordonateurCin;
    private String coordonateurNomPrenom;

    private String semestre;
    private String optionModule;

    private String codeFiliere; // Mandatory for backend linking
    private String nomFiliere;  // For display in frontend only
 // For display in frontend only
}
