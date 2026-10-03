package br.com.brasil_saas.compras.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.List;
public record PedidoCompraRequest(
    Long empresaId, @NotNull Long fornecedorId, String numero, LocalDate dataEmissao, LocalDate dataPrevisaoEntrega,
    Long condicaoPagamentoId, String observacao, String status,
    BigDecimal valorDesconto, BigDecimal valorFrete,
    @Valid List<ItemPedidoCompraRequest> itens) {}
