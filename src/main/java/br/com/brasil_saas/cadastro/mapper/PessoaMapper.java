package br.com.brasil_saas.cadastro.mapper;

import br.com.brasil_saas.cadastro.model.Contato;
import br.com.brasil_saas.cadastro.model.Endereco;
import br.com.brasil_saas.cadastro.model.Pessoa;
import br.com.brasil_saas.cadastro.service.dto.PessoaDtos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PessoaMapper {

    PessoaDtos.Resumo toResumo(Pessoa pessoa);

    PessoaDtos.FisicaResponse toFisicaResponse(Pessoa pessoa);
    PessoaDtos.JuridicaResponse toJuridicaResponse(Pessoa pessoa);
    @org.mapstruct.Mapping(target = "fisica", expression = "java(pessoa.getFisica() != null ? toFisicaResponse(pessoa) : null)")
    @org.mapstruct.Mapping(target = "juridica", expression = "java(pessoa.getJuridica() != null ? toJuridicaResponse(pessoa) : null)")
    PessoaDtos.Response toResponse(Pessoa pessoa);
    PessoaDtos.EnderecoResponse toEnderecoResponse(Endereco endereco);
    PessoaDtos.ContatoResponse toContatoResponse(Contato contato);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "empresaId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "fisica.pessoa", ignore = true)
    @Mapping(target = "juridica.pessoa", ignore = true)
    @Mapping(target = "enderecos", ignore = true)
    @Mapping(target = "contatos", ignore = true)
    Pessoa toEntity(PessoaDtos.Request request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "empresaId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "fisica", ignore = true)
    @Mapping(target = "juridica", ignore = true)
    @Mapping(target = "enderecos", ignore = true)
    @Mapping(target = "contatos", ignore = true)
    void update(PessoaDtos.Request request, @MappingTarget Pessoa pessoa);

    Endereco toEndereco(PessoaDtos.EnderecoRequest request);
    Contato toContato(PessoaDtos.ContatoRequest request);
}
