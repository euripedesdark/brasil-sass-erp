package br.com.brasil_saas.cadastro.mapper;

import br.com.brasil_saas.cadastro.model.Servico;
import br.com.brasil_saas.cadastro.service.dto.ServicoDtos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServicoMapper {

    @Mapping(target = "unidadeSigla", source = "unidadeMedida.sigla")
    ServicoDtos.Response toResponse(Servico servico);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "empresaId", ignore = true)
    @Mapping(target = "unidadeMedida", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Servico toEntity(ServicoDtos.Request request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "empresaId", ignore = true)
    @Mapping(target = "unidadeMedida", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void update(ServicoDtos.Request request, @MappingTarget Servico servico);
}
