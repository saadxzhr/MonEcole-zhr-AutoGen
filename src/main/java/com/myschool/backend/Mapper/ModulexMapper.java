package com.myschool.backend.Mapper;

import org.mapstruct.*;
import com.myschool.backend.DTO.ModulexDTO;
import com.myschool.backend.Model.Modulex;


@Mapper(componentModel = "spring")
public interface ModulexMapper {

    @Mapping(source = "filiere.codeFiliere", target = "codeFiliere")
    @Mapping(source = "filiere.nomFiliere", target = "nomFiliere")
    @Mapping(source = "coordonateur.cin", target = "coordonateurCin")
    @Mapping(target = "coordonateurNomPrenom",
            expression = "java(modulex.getCoordonateur() != null ? modulex.getCoordonateur().getNom() + \" \" + modulex.getCoordonateur().getPrenom() : null)")
    ModulexDTO toDto(Modulex modulex);

    @Mapping(target = "filiere", ignore = true)
    @Mapping(target = "coordonateur", ignore = true)
    Modulex toEntity(ModulexDTO dto);

    @Mapping(target = "filiere", ignore = true)
    @Mapping(target = "coordonateur", ignore = true)
    void updateEntityFromDto(ModulexDTO dto, @MappingTarget Modulex entity);
}
