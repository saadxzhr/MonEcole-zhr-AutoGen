package com.szschoolmanager.modulex;
// package com.szschoolmanager.Projection;

// public interface ModulexProjection {

//     // Infos du module
//     Long getId();
//     String getCodeModule();
//     String getNomModule();
//     String getDescription();
//     Integer getNombreHeures();
//     Float getCoefficient();
//     String getOptionModule();
//     String getDepartementDattache();
//     Integer getSemestre();

//     // ⭐ Infos du coordonateur (depuis la relation)
//     String getCoordonateurCin();
//     String getCoordonateurNom();
//     String getCoordonateurPrenom();

//     // ⭐ Méthode calculée pour le nom complet
//     default String getCoordonateurNomComplet() {
//         if (getCoordonateurNom() == null || getCoordonateurPrenom() == null) {
//             return "Non assigné";
//         }
//         return getCoordonateurNom() + " " + getCoordonateurPrenom();
//     }

//     // ⭐ Infos de la filière (depuis la relation)
//     String getCodeFiliere();
//     String getNomFiliere();
// }
