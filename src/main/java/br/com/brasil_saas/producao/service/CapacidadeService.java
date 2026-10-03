package br.com.brasil_saas.producao.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CapacidadeService {

    /** Pedido de simulacao: produto, quantidade e data de inicio desejada (hoje se nula). */
    record Request(Long produtoId, BigDecimal quantidade, LocalDate dataInicio) {}

    /**
     * Calcula, a partir do roteiro vigente do produto, as horas exigidas por
     * operacao e por centro de trabalho, os dias necessarios dado a capacidade
     * diaria, e a data de termino estimada. Nao grava nada.
     */
    Map<String, Object> simular(Long empresaId, Request request);

    /** Carga por centro de trabalho de varias simulacoes, para detectar gargalo. */
    List<Map<String, Object>> cargaPorCentro(Long empresaId, List<Request> pedidos);
}
