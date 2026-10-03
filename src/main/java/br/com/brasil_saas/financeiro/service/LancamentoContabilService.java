package br.com.brasil_saas.financeiro.service;
import br.com.brasil_saas.financeiro.dto.FinanceiroDtos.*;
import java.util.List;
public interface LancamentoContabilService {
    LancamentoResponse criar(Long empresaId, LancamentoRequest request);
    LancamentoResponse buscar(Long empresaId, Long id);
    List<LancamentoResponse> listar(Long empresaId);
    List<PartidaResponse> listarPartidas(Long empresaId, Long id);
    LancamentoResponse atualizar(Long empresaId, Long id, LancamentoRequest request);
    void excluir(Long empresaId, Long id);
}
