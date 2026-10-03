package br.com.brasil_saas.producao.service;

import java.util.List;
import java.util.Map;

public interface MrpService {
    /** Explode a BOM multinivel abatendo o estoque em cada nivel. Nao grava nada. */
    List<Map<String, Object>> simular(Long empresaId, MrpRequest request);

    /**
     * Roda o MRP e materializa as sugestoes: uma Solicitacao de Compra com os
     * itens COMPRAR e uma Ordem de Producao para cada item PRODUZIR.
     */
    Map<String, Object> gerarSugestoes(Long empresaId, Long usuarioId, MrpRequest request);
}
