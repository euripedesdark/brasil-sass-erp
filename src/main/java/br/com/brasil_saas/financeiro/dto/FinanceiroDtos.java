package br.com.brasil_saas.financeiro.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class FinanceiroDtos {
    public record TituloResponse(Long id, String tipo, String numeroDocumento, String descricao,
        Long pessoaId, BigDecimal valorOriginal, BigDecimal valorSaldo, LocalDate dataEmissao,
        LocalDate dataVencimento, String status, Long centroCustoId, Long planoContasId) {}
    public record ParcelaResponse(Long id, Integer numeroParcela, BigDecimal valorParcela,
        BigDecimal valorSaldo, LocalDate dataVencimento, String status) {}
    public record BaixaRequest(Long parcelaId, Long contaBancariaId, Long tipoPagamentoId,
        LocalDate dataBaixa, @NotNull BigDecimal valorBaixa, BigDecimal valorDesconto,
        BigDecimal valorJuro, BigDecimal valorMulta, String observacao) {}
    public record BaixaResponse(Long id, Long tituloId, Long parcelaId, BigDecimal valorBaixa,
        BigDecimal valorDesconto, BigDecimal valorJuro, BigDecimal valorMulta,
        LocalDate dataBaixa, BigDecimal saldoRestante, String statusTitulo) {}
    public record CondicaoRequest(@NotBlank String descricao, String dias, Boolean ativo) {}
    public record CondicaoResponse(Long id, String descricao, String dias, Boolean ativo) {}
    public record ContaRequest(@NotBlank String banco, @NotBlank String agencia, @NotBlank String conta,
        String digito, @NotBlank String tipo, BigDecimal saldoInicial, Boolean ativa) {}
    public record ContaResponse(Long id, String banco, String agencia, String conta, String digito,
        String tipo, BigDecimal saldoInicial, Boolean ativa) {}
    public record TipoPagamentoRequest(@NotBlank String descricao, String codigo, Boolean ativo) {}
    public record TipoPagamentoResponse(Long id, String descricao, String codigo, Boolean ativo) {}
    public record PlanoContasRequest(@NotBlank String codigo, @NotBlank String descricao,
        @NotBlank String tipo, @NotBlank String natureza, Long contaPaiId, Integer nivel, Boolean ativa) {}
    public record PlanoContasResponse(Long id, String codigo, String descricao, String tipo,
        String natureza, Long contaPaiId, Integer nivel, Boolean ativa) {}
    public record CentroCustoRequest(@NotBlank String codigo, @NotBlank String descricao,
        Long centroCustoPaiId, Boolean ativo) {}
    public record CentroCustoResponse(Long id, String codigo, String descricao, Long centroCustoPaiId, Boolean ativo) {}
    public record ExtratoRequest(@NotNull Long contaBancariaId, @NotNull LocalDate dataMovimento,
        @NotBlank String descricao, @NotNull BigDecimal valor, @NotBlank String tipo) {}
    public record ExtratoResponse(Long id, Long contaBancariaId, LocalDate dataMovimento, String descricao,
        BigDecimal valor, String tipo, BigDecimal saldoAnterior, BigDecimal saldoAtual, Boolean conciliado) {}
    public record PartidaRequest(@NotNull Long planoContasId, Long centroCustoId,
        @NotBlank String tipo, @NotNull BigDecimal valor) {}

    // ===== Workflow de aprovação de títulos =====
    public record SolicitacaoAprovacaoRequest(Long fluxoAprovacaoId, Integer niveis,
        Long usuarioAprovadorId, String observacao) {}
    public record DecisaoAprovacaoRequest(String acao, String observacao) {} // APROVAR | REJEITAR
    public record AprovacaoResponse(Long id, Long tituloId, Long fluxoAprovacaoId, Integer nivel,
        String status, Long usuarioSolicitanteId, Long usuarioAprovadorId,
        java.time.LocalDateTime dataSolicitacao, java.time.LocalDateTime dataAprovacao,
        java.time.LocalDateTime dataRejeicao, String observacao,
        String numeroDocumento, String tipoTitulo, BigDecimal valorSaldo,
        java.time.LocalDate dataVencimento,
        String solicitanteNome, String aprovadorNome) {}
    public record LancamentoRequest(@NotNull LocalDate dataLancamento, @NotBlank String descricaoHistorico,
        String origem, Long idOrigem, @NotNull List<PartidaRequest> partidas) {}
    public record LancamentoResponse(Long id, LocalDate dataLancamento, String descricaoHistorico,
        BigDecimal valorTotal, String origem, Long idOrigem, List<PartidaResponse> partidas) {}
    public record PartidaResponse(Long id, Long planoContasId, Long centroCustoId, String tipo, BigDecimal valor) {}
    private FinanceiroDtos() {}
}
