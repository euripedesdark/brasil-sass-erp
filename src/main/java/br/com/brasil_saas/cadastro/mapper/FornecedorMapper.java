package br.com.brasil_saas.cadastro.mapper;

import br.com.brasil_saas.cadastro.model.Fornecedor;
import br.com.brasil_saas.cadastro.service.dto.FornecedorDtos;
import br.com.brasil_saas.cadastro.service.dto.PessoaDtos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = PessoaMapper.class)
public interface FornecedorMapper {

    FornecedorDtos.Response toResponse(Fornecedor fornecedor);

    PessoaDtos.Resumo toPessoaResumo(Fornecedor fornecedor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "empresaId", ignore = true)
    @Mapping(target = "pessoa", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Fornecedor toEntity(FornecedorDtos.Request request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "empresaId", ignore = true)
    @Mapping(target = "pessoa", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void update(FornecedorDtos.Request request, @MappingTarget Fornecedor fornecedor);
}
