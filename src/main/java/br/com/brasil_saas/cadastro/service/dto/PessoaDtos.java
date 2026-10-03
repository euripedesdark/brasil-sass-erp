package br.com.brasil_saas.cadastro.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class PessoaDtos {

    public record Resumo(Long id, UUID uuid, String tipo, String nome,
                         String documento, String email, String status) {}

    public record FisicaRequest(@Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos") String cpf,
                                String rg, String orgaoExpedidor, LocalDate dataNascimento,
                                String sexo, String estadoCivil) {}

    public record JuridicaRequest(@Pattern(regexp = "\\d{14}", message = "CNPJ deve conter 14 dígitos") String cnpj,
                                  String inscricaoEstadual, String inscricaoMunicipal,
                                  LocalDate dataAbertura, String porte, String naturezaJuridica) {}

    public record EnderecoRequest(@NotBlank String tipo, String logradouro, String numero, String complemento,
                                  String bairro,
                                  @Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 dígitos") String cep,
                                  Long municipioId, String uf, BigDecimal latitude, BigDecimal longitude,
                                  boolean principal) {}

    public record ContatoRequest(@NotBlank String tipo, String nome, @Email String email,
                                 String telefone, String observacao) {}

    public record Request(@NotBlank @Pattern(regexp = "FISICA|JURIDICA") String tipo,
                          @NotBlank @Size(max = 200) String nome,
                          @Size(max = 14) String documento,
                          @Email @Size(max = 150) String email,
                          @Size(max = 20) String telefone,
                          String status,
                          String observacao,
                          @Valid FisicaRequest fisica,
                          @Valid JuridicaRequest juridica,
                          @Valid List<EnderecoRequest> enderecos,
                          @Valid List<ContatoRequest> contatos) {

        /**
         * Copia com a empresa do token.
         *
         * O controller passa a empresa por fora do corpo: aceitar o
         * empresaId do cliente permitiria criar pessoa em nome de outra
         * empresa. O record e imutavel, entao a troca e feita aqui.
         */
        public Request comEmpresaDa(Long empresaId) {
            return new Request(tipo, nome, documento, email, telefone, status,
                    observacao, fisica, juridica, enderecos, contatos);
        }
    }

    public record FisicaResponse(String cpf, String rg, String orgaoExpedidor,
                                 LocalDate dataNascimento, String sexo, String estadoCivil) {}

    public record JuridicaResponse(String cnpj, String inscricaoEstadual, String inscricaoMunicipal,
                                   LocalDate dataAbertura, String porte, String naturezaJuridica) {}

    public record EnderecoResponse(Long id, String tipo, String logradouro, String numero, String complemento,
                                   String bairro, String cep, Long municipioId, String uf,
                                   BigDecimal latitude, BigDecimal longitude, boolean principal) {}

    public record ContatoResponse(Long id, String tipo, String nome, String email,
                                  String telefone, String observacao) {}

    public record Response(Long id, UUID uuid, String tipo, String nome, String documento, String email,
                           String telefone, String status, String observacao,
                           FisicaResponse fisica, JuridicaResponse juridica,
                           List<EnderecoResponse> enderecos, List<ContatoResponse> contatos,
                           LocalDateTime createdAt) {}

    private PessoaDtos() {}
}
