package br.com.brasil_saas.producao.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CapacidadeService {

    record Request(Long produtoId, BigDecimal quantidade, LocalDate dataInicio) {}

    /** Reserva avulsa (sem OP) ou vinculada a uma OP. */
    record AgendarRequest(Long produtoId, BigDecimal quantidade, LocalDate dataInicio, Long ordemProducaoId) {}

    /**
     * Simula o roteiro com capacidade finita: o que ja esta no calendario de
     * carga reduz as horas livres de cada dia. Nao grava nada.
     */
    Map<String, Object> simular(Long empresaId, Request request);

    /** Carga por centro de varias simulacoes, para detectar gargalo. */
    List<Map<String, Object>> cargaPorCentro(Long empresaId, List<Request> pedidos);

    /**
     * Simula e GRAVA as horas no calendario de carga. Se informar ordemProducaoId,
     * o agendamento anterior dessa OP e substituido (reagendar e idempotente).
     */
    Map<String, Object> agendar(Long empresaId, AgendarRequest request);

    /** Agenda uma OP existente usando produto e quantidade dela. */
    Map<String, Object> agendarOrdem(Long empresaId, Long ordemProducaoId, LocalDate dataInicio);

    /** Libera as horas reservadas de uma OP. Devolve quantas linhas foram liberadas. */
    int liberarOrdem(Long empresaId, Long ordemProducaoId);

    /** Dia a dia de um centro: capacidade, alocado e livre. */
    List<Map<String, Object>> calendario(Long empresaId, Long centroTrabalhoId, LocalDate de, LocalDate ate);
}
