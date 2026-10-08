package br.com.brasil_saas.compras.service;

import br.com.brasil_saas.compras.model.ConferenciaFaturaCompra;
import br.com.brasil_saas.compras.model.ConferenciaFaturaCompraItem;
import jakarta.validation.constraints.NotNull;

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

    /** Itens conferidos da conferencia informada, na ordem do pedido. */
    List<ConferenciaFaturaCompraItem> listarItens(Long empresaId, Long conferenciaId);

    /** Corpo do POST /api/compras/conferencia-faturas (contrato inalterado). */
    record Request(@NotNull Long pedidoId, @NotNull BigDecimal valorFatura,
                   Long recebimentoId, Long tituloId, Long nfeId, BigDecimal tolerancia) {
    }
}
