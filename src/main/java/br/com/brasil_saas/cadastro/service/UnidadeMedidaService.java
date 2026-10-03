package br.com.brasil_saas.cadastro.service;

import br.com.brasil_saas.cadastro.dto.UnidadeMedidaRequest;
import br.com.brasil_saas.cadastro.dto.UnidadeMedidaResponse;
import java.util.List;

public interface UnidadeMedidaService {
    UnidadeMedidaResponse criar(UnidadeMedidaRequest request, Long empresaId);
    UnidadeMedidaResponse atualizar(Long id, UnidadeMedidaRequest request);
    UnidadeMedidaResponse buscarPorId(Long id);
    List<UnidadeMedidaResponse> listar();
    void excluir(Long id);
}
