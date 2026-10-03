package br.com.brasil_saas.producao.service;

import java.math.BigDecimal;
import java.util.List;

public record ProducaoRequest(
    String numero,
    String tipoProducao,
    Long produtoFinalId,
    BigDecimal quantidadePlanejada,
    String unidadeMedida,
    BigDecimal densidade,
    List<ItemRequest> itens
) {}
