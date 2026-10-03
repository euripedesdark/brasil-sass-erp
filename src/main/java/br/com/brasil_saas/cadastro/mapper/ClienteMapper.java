package br.com.brasil_saas.cadastro.mapper;

import br.com.brasil_saas.cadastro.model.Cliente;
import br.com.brasil_saas.cadastro.service.dto.ClienteDtos;
import br.com.brasil_saas.cadastro.service.dto.PessoaDtos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = PessoaMapper.class)
public interface ClienteMapper {

    @Mapping(target = "pessoa", source = "pessoa")
    ClienteDtos.Response toResponse(Cliente cliente);

    PessoaDtos.Resumo toPessoaResumo(Cliente cliente);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "empresaId", ignore = true)
    @Mapping(target = "pessoa", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Cliente toEntity(ClienteDtos.Request request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "empresaId", ignore = true)
    @Mapping(target = "pessoa", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void update(ClienteDtos.Request request, @MappingTarget Cliente cliente);
}
