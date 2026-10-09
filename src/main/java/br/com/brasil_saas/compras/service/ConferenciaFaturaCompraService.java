package br.com.brasil_saas.compras.service;

import br.com.brasil_saas.compras.model.ConferenciaFaturaCompra;
import br.com.brasil_saas.compras.model.ConferenciaFaturaCompraItem;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

/**
 * Conferencia 3-way da compra: pedido x recebimento x NF-e de entrada.
 */
public interface ConferenciaFaturaCompraService {

    List<ConferenciaFaturaCompra> listar(Long empresaId);

    /**
     * Confere a fatura contra o pedido, o recebimento e a NF-e de entrada,
     * comparando item a item e gravando as divergencias.
     */
    ConferenciaFaturaCompra conferir(Long empresaId, Request request);

    /**
     * Aprova excepcionalmente uma conferencia divergente, liberando o
     * pagamento. Registra motivo e aprovador na trilha, sem criar tela,
     * status ou contrato novos.
     */
    ConferenciaFaturaCompra aprovarExcepcional(Long empresaId, Long userId, Long conferenciaId, String motivo);

    /** Corpo do POST de aprovacao excepcional (motivo obrigatorio). */
    record AprovacaoExcepcionalRequest(@NotBlank String motivo) {
    }

    /** Itens conferidos da conferencia informada, na ordem do pedido. */

    List<ConferenciaFaturaCompraItem> listarItens(Long empresaId, Long conferenciaId);

    /** Corpo do POST /api/compras/conferencia-faturas (contrato inalterado). */
    record Request(@NotNull @Positive Long pedidoId, @NotNull @Positive BigDecimal valorFatura,
                   @NotNull @Positive Long recebimentoId, @Positive Long tituloId,
                   @NotNull @Positive Long nfeId, @PositiveOrZero BigDecimal tolerancia) {
    }
}
