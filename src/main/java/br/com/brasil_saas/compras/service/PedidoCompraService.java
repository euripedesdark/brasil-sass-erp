package br.com.brasil_saas.compras.service;

import br.com.brasil_saas.compras.dto.PedidoCompraRequest;
import br.com.brasil_saas.compras.dto.PedidoCompraResponse;
import java.util.List;

public interface PedidoCompraService {
    PedidoCompraResponse criar(PedidoCompraRequest request);
    PedidoCompraResponse buscarPorId(Long id);
    List<PedidoCompraResponse> listarPorEmpresa(Long empresaId);
    void receber(Long id);
    void receberParcial(Long id, java.util.Map<Long, java.math.BigDecimal> quantidades);
    void cancelar(Long id);
}
