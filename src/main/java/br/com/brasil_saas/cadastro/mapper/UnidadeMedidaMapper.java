package br.com.brasil_saas.cadastro.mapper;

import br.com.brasil_saas.cadastro.model.UnidadeMedida;
import br.com.brasil_saas.cadastro.service.dto.UnidadeMedidaDtos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UnidadeMedidaMapper {

    UnidadeMedidaDtos.Response toResponse(UnidadeMedida unidade);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "empresaId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    UnidadeMedida toEntity(UnidadeMedidaDtos.Request request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "empresaId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void update(UnidadeMedidaDtos.Request request, @MappingTarget UnidadeMedida unidade);
}
