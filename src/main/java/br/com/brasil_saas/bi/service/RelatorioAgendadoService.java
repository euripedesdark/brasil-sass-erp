package br.com.brasil_saas.bi.service;

import br.com.brasil_saas.bi.dto.RelatorioAgendadoRequest;
import br.com.brasil_saas.bi.model.RelatorioAgendado;

import java.time.LocalDateTime;
import java.util.List;

public interface RelatorioAgendadoService {

    RelatorioAgendado criar(Long empresaId, RelatorioAgendadoRequest request);

    RelatorioAgendado atualizar(Long empresaId, Long id, RelatorioAgendadoRequest request);

    RelatorioAgendado buscarPorId(Long empresaId, Long id);

    List<RelatorioAgendado> listarPorEmpresa(Long empresaId);

    List<RelatorioAgendado> listarPendentes(Long empresaId);

    List<RelatorioAgendado> listarPorFrequencia(Long empresaId, String frequencia);

    void excluir(Long empresaId, Long id);

    void executarPendentes();

    void executarAgendado(Long empresaId, Long id);

    void agendarProximaExecucao(Long empresaId, Long id);

    void definirProximaExecucao(Long empresaId, Long id, LocalDateTime proximaExecucao);
}
