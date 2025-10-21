package com.myschool.backend.Utilisateur.mapper;


import com.myschool.backend.Utilisateur.dto.*;
import com.myschool.backend.Utilisateur.Model.Utilisateur;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
    public interface UtilisateurMapper {

    // Création : DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "forceChangePassword", ignore = true)
    Utilisateur toEntity(UtilisateurCreateDTO dto);

    // Entity -> DTO de réponse
    UtilisateurResponseDTO toResponseDTO(Utilisateur entity);

    // Mise à jour : DTO -> Entity existant
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromDto(UtilisateurUpdateDTO dto, @MappingTarget Utilisateur entity);
}