package br.com.brasil_saas.cadastro.dto;

import br.com.brasil_saas.cadastro.model.Pessoa;
import java.time.LocalDateTime;
import java.util.List;

public record PessoaResponse(
    Long id,
    String uuid,
    String tipo,
    String nome,
    String documento,
    String email,
    String telefone,
    String status,
    String observacao,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    PessoaFisicaResponse fisica,
    PessoaJuridicaResponse juridica,
    List<EnderecoResponse> enderecos,
    List<ContatoResponse> contatos
) {
    public static PessoaResponse from(Pessoa p) {
        return new PessoaResponse(
            p.getId(),
            p.getUuid() != null ? p.getUuid().toString() : null,
            p.getTipo(), p.getNome(), p.getDocumento(), p.getEmail(), p.getTelefone(),
            p.getStatus(), p.getObservacao(), p.getCreatedAt(), p.getUpdatedAt(),
            p.getFisica() != null ? PessoaFisicaResponse.from(p.getFisica()) : null,
            p.getJuridica() != null ? PessoaJuridicaResponse.from(p.getJuridica()) : null,
            p.getEnderecos() != null ? p.getEnderecos().stream().map(EnderecoResponse::from).toList() : List.of(),
            p.getContatos() != null ? p.getContatos().stream().map(ContatoResponse::from).toList() : List.of()
        );
    }
}
