package br.com.brasil_saas.cadastro.mapper;

import br.com.brasil_saas.cadastro.model.Produto;
import br.com.brasil_saas.cadastro.model.ProdutoVariacao;
import br.com.brasil_saas.cadastro.service.dto.ProdutoDtos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProdutoMapper {

    ProdutoDtos.Resumo toResumo(Produto produto);

    @Mapping(target = "categoriaNome", source = "categoria.nome")
    @Mapping(target = "marcaNome", source = "marca.nome")
    @Mapping(target = "unidadeSigla", source = "unidadeMedida.sigla")
    ProdutoDtos.Response toResponse(Produto produto);

    ProdutoDtos.VariacaoResponse toVariacaoResponse(ProdutoVariacao variacao);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "empresaId", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "marca", ignore = true)
    @Mapping(target = "unidadeMedida", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Produto toEntity(ProdutoDtos.Request request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "empresaId", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "marca", ignore = true)
    @Mapping(target = "unidadeMedida", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void update(ProdutoDtos.Request request, @MappingTarget Produto produto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "empresaId", ignore = true)
    @Mapping(target = "produto", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    ProdutoVariacao toVariacao(ProdutoDtos.VariacaoRequest request);
}
