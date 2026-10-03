package br.com.brasil_saas.cadastro.service;

import br.com.brasil_saas.cadastro.dto.CategoriaRequest;
import br.com.brasil_saas.cadastro.dto.CategoriaResponse;
import java.util.List;

public interface CategoriaService {
    CategoriaResponse criar(CategoriaRequest request, Long empresaId);
    CategoriaResponse atualizar(Long id, CategoriaRequest request);
    CategoriaResponse buscarPorId(Long id);
    List<CategoriaResponse> listar();
    void excluir(Long id);
}
