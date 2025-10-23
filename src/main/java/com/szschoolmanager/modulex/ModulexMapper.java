package com.szschoolmanager.modulex;

import com.szschoolmanager.Model.Employe;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ModulexMapper {

  // === Mapping Modulex → DTO pour l'affichage ===
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(source = "filiere.codeFiliere", target = "codeFiliere")
  @Mapping(source = "filiere.nomFiliere", target = "nomFiliere")
  @Mapping(source = "coordinateur.cin", target = "coordinateurCin")
  @Mapping(
      source = "coordinateur",
      target = "coordinateurNomPrenom",
      qualifiedByName = "formatCoordinateurFullName")
  @Named("toDto")
  ModulexDTO toDto(Modulex modulex);

  @Named("formatCoordinateurFullName")
  default String formatCoordinateurFullName(Employe coord) {
    return (coord != null) ? coord.getNom() + " " + coord.getPrenom() : null;
  }

  // méthode utilitaire inverse
  @IterableMapping(qualifiedByName = "toDto")
  List<ModulexDTO> toDtoList(List<Modulex> modules);

  // === Mapping DTO → Modulex pour création ===
  @Mapping(target = "filiere", ignore = true)
  @Mapping(target = "coordinateur", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  Modulex toEntity(ModulexDTO dto);

  // === Mise à jour partielle d'une entité existante ===
  @Mapping(target = "filiere", ignore = true)
  @Mapping(target = "coordinateur", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  void updateEntityFromDto(ModulexDTO dto, @MappingTarget Modulex ent);
}
