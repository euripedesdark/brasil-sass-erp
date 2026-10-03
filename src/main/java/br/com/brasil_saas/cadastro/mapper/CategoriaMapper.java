package br.com.brasil_saas.cadastro.mapper;

import br.com.brasil_saas.cadastro.model.Categoria;
import br.com.brasil_saas.cadastro.service.dto.CategoriaDtos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoriaMapper {

    @Mapping(target = "categoriaPaiId", source = "categoriaPai.id")
    CategoriaDtos.Response toResponse(Categoria categoria);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "empresaId", ignore = true)
    @Mapping(target = "categoriaPai", ignore = true)
    @Mapping(target = "filhos", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Categoria toEntity(CategoriaDtos.Request request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "empresaId", ignore = true)
    @Mapping(target = "categoriaPai", ignore = true)
    @Mapping(target = "filhos", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void update(CategoriaDtos.Request request, @MappingTarget Categoria categoria);
}
