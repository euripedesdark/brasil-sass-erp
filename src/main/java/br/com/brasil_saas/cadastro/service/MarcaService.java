package br.com.brasil_saas.cadastro.service;

import br.com.brasil_saas.cadastro.dto.MarcaRequest;
import br.com.brasil_saas.cadastro.dto.MarcaResponse;
import java.util.List;

public interface MarcaService {
    MarcaResponse criar(MarcaRequest request, Long empresaId);
    MarcaResponse atualizar(Long id, MarcaRequest request);
    MarcaResponse buscarPorId(Long id);
    List<MarcaResponse> listar();
    void excluir(Long id);
}
