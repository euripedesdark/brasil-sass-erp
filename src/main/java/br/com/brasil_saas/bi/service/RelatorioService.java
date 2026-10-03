package br.com.brasil_saas.bi.service;

import br.com.brasil_saas.bi.dto.RelatorioRequest;
import br.com.brasil_saas.bi.model.Relatorio;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

public interface RelatorioService {

    Relatorio criar(Long empresaId, RelatorioRequest request);

    Relatorio atualizar(Long empresaId, Long id, RelatorioRequest request);

    Relatorio buscarPorId(Long empresaId, Long id);

    List<Relatorio> listarPorEmpresa(Long empresaId);

    List<Relatorio> listarPorCategoria(Long empresaId, String categoria);

    List<Relatorio> listarAgendados(Long empresaId);

    void excluir(Long empresaId, Long id);

    ByteArrayOutputStream gerarPdf(Long empresaId, Long relatorioId, Map<String, Object> parametros);

    ByteArrayOutputStream gerarExcel(Long empresaId, Long relatorioId, Map<String, Object> parametros);

    ByteArrayOutputStream gerarCsv(Long empresaId, Long relatorioId, Map<String, Object> parametros);

    Map<String, Object> executarRelatorio(Long empresaId, Long relatorioId, Map<String, Object> parametros);
}
