package br.com.brasil_saas.bi.service;

import br.com.brasil_saas.bi.dto.IndicadorRequest;
import br.com.brasil_saas.bi.model.Indicador;

import java.util.List;
import java.util.Map;

public interface IndicadorService {

    Indicador criar(Long empresaId, IndicadorRequest request);

    Indicador atualizar(Long empresaId, Long id, IndicadorRequest request);

    Indicador buscarPorId(Long empresaId, Long id);

    List<Indicador> listarPorEmpresa(Long empresaId);

    List<Indicador> listarPorCategoria(Long empresaId, String categoria);

    List<Indicador> listarVisiveisDashboard(Long empresaId);

    void excluir(Long empresaId, Long id);

    Map<String, Object> calcularIndicadores(Long empresaId, String categoria);

    Map<String, Object> calcularTodosIndicadores(Long empresaId);

    void atualizarValores(Long empresaId);

    void atualizarValor(Long empresaId, Long indicadorId);
}
