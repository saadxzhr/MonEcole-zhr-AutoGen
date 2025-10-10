package com.myschool.backend.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.myschool.backend.DTO.ModulexDTO;
import com.myschool.backend.Model.Modulex;



@Mapper(componentModel = "spring")
public interface ModulexMapper {

    ModulexMapper INSTANCE = Mappers.getMapper(ModulexMapper.class);

    @Mapping(source = "filiere.codeFiliere", target = "codeFiliere")
    @Mapping(source = "filiere.nomFiliere", target = "nomFiliere")
    @Mapping(source = "coordonateur.cin", target = "coordonateurCin")
    @Mapping(target = "coordonateurNomPrenom", expression = "java(modulex.getCoordonateur() != null ? modulex.getCoordonateur().getNom() + \" \" + modulex.getCoordonateur().getPrenom() : \"\")")  
    ModulexDTO toDto(Modulex modulex);

    // DTO -> Entity
    @Mapping(target = "filiere", ignore = true)
    @Mapping(target = "coordonateur", ignore = true) // Will set in service
    Modulex toEntity(ModulexDTO dto);

}


