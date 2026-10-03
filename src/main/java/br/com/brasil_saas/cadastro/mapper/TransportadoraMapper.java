package br.com.brasil_saas.cadastro.mapper;

import br.com.brasil_saas.cadastro.model.Transportadora;
import br.com.brasil_saas.cadastro.service.dto.PessoaDtos;
import br.com.brasil_saas.cadastro.service.dto.TransportadoraDtos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = PessoaMapper.class)
public interface TransportadoraMapper {

    TransportadoraDtos.Response toResponse(Transportadora transportadora);

    PessoaDtos.Resumo toPessoaResumo(Transportadora transportadora);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "empresaId", ignore = true)
    @Mapping(target = "pessoa", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Transportadora toEntity(TransportadoraDtos.Request request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "empresaId", ignore = true)
    @Mapping(target = "pessoa", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void update(TransportadoraDtos.Request request, @MappingTarget Transportadora transportadora);
}
