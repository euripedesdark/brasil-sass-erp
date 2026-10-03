package br.com.brasil_saas.vendas.service;

import br.com.brasil_saas.vendas.dto.PedidoVendaRequest;
import br.com.brasil_saas.vendas.dto.PedidoVendaResponse;

import java.util.List;

/**
 * Multiempresa: todas as operacoes que agem sobre um pedido existente recebem o
 * {@code empresaId} do token. Antes elas recebiam so o id, e nenhuma conferia a
 * empresa — trocar o id na URL dava acesso a pedido de outra empresa.
 */
public interface PedidoVendaService {

    PedidoVendaResponse criar(PedidoVendaRequest request);

    PedidoVendaResponse buscarPorId(Long id, Long empresaId);

    List<PedidoVendaResponse> listarPorEmpresa(Long empresaId);

    void confirmar(Long id, Long empresaId);

    void faturar(Long id, Long empresaId);

    void cancelar(Long id, Long empresaId);
}
