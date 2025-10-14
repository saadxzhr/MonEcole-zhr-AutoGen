package com.myschool.backend.Modulex;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.myschool.backend.Model.Employe;


@Mapper(componentModel = "spring")
public interface ModulexMapper {

    // === Mapping Modulex → DTO pour l'affichage ===
    @Mapping(source = "filiere.codeFiliere", target = "codeFiliere")
    @Mapping(source = "filiere.nomFiliere", target = "nomFiliere")
    @Mapping(source = "coordinateur.cin", target = "coordinateurCin")
    @Mapping(expression = "java(formatCoordinateurFullName(modulex.getCoordinateur()))", 
             target = "coordinateurNomPrenom")
    ModulexDTO toDto(Modulex modulex);

    // === Mapping DTO → Modulex pour création ===
    @Mapping(target = "filiere", ignore = true)
    @Mapping(target = "coordinateur", ignore = true)
    Modulex toEntity(ModulexDTO dto);

    // === Mise à jour partielle d'une entité existante ===
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "filiere", ignore = true)
    @Mapping(target = "coordinateur", ignore = true)
    void updateEntityFromDto(ModulexDTO dto, @MappingTarget Modulex entity);

    // === Méthode utilitaire pour afficher nom complet du coordinateur ===
    default String formatCoordinateurFullName(Employe coord) {
        return (coord != null) ? coord.getNom() + " " + coord.getPrenom() : null;
    }
}

