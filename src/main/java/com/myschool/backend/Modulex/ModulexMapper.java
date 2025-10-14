package com.myschool.backend.Modulex;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ModulexMapper {

    @Mapping(source = "filiere.codeFiliere", target = "codeFiliere")
    @Mapping(source = "filiere.nomFiliere", target = "nomFiliere")
    @Mapping(source = "coordinateur.cin", target = "coordinateurCin")
    @Mapping(target = "coordinateurNomPrenom",
            expression = "java(modulex.getCoordinateur() != null ? modulex.getCoordinateur().getNom() + \" \" + modulex.getCoordinateur().getPrenom() : null)")
    ModulexDTO toDto(Modulex modulex);

    @Mapping(target = "filiere", ignore = true)
    @Mapping(target = "coordinateur", ignore = true)
    Modulex toEntity(ModulexDTO dto);

    @Mapping(target = "filiere", ignore = true)
    @Mapping(target = "coordinateur", ignore = true)
    void updateEntityFromDto(ModulexDTO dto, @MappingTarget Modulex entity);
}
