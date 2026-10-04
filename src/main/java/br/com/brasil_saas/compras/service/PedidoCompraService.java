package br.com.brasil_saas.compras.service;

import br.com.brasil_saas.compras.dto.PedidoCompraRequest;
import br.com.brasil_saas.compras.dto.PedidoCompraResponse;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

public interface PedidoCompraService {
    PedidoCompraResponse criar(PedidoCompraRequest request);
    PedidoCompraResponse buscarPorId(Long id, Long empresaId);
    List<PedidoCompraResponse> listarPorEmpresa(Long empresaId);
    void receber(Long id, Long empresaId);
    void receberParcial(Long id, Long empresaId, Map<Long, BigDecimal> quantidades);
    void cancelar(Long id, Long empresaId);
}
