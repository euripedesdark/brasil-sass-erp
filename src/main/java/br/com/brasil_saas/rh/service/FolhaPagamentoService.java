package br.com.brasil_saas.rh.service;

import br.com.brasil_saas.rh.dto.FolhaPagamentoRequest;
import br.com.brasil_saas.rh.dto.FolhaPagamentoResponse;
import java.util.List;

public interface FolhaPagamentoService {
    FolhaPagamentoResponse criar(Long empresaId, FolhaPagamentoRequest request);
    FolhaPagamentoResponse buscarPorId(Long empresaId, Long id);
    List<FolhaPagamentoResponse> listarPorEmpresa(Long empresaId);
    FolhaPagamentoResponse atualizar(Long empresaId, Long id, FolhaPagamentoRequest request);
    void excluir(Long empresaId, Long id);
    void processar(Long empresaId, Long id);
    void cancelar(Long empresaId, Long id);
    br.com.brasil_saas.rh.dto.FolhaPagamentoResponse importarPonto(Long empresaId, Long id, Long funcionarioId, Integer ano, Integer mes);
}
