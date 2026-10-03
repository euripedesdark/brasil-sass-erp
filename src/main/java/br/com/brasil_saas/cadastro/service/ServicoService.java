package br.com.brasil_saas.cadastro.service;

import br.com.brasil_saas.cadastro.dto.ServicoRequest;
import br.com.brasil_saas.cadastro.dto.ServicoResponse;
import br.com.brasil_saas.shared.web.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ServicoService {
    ServicoResponse criar(ServicoRequest request, Long empresaId);
    ServicoResponse atualizar(Long id, ServicoRequest request);
    ServicoResponse buscarPorId(Long id);
    PageResponse<ServicoResponse> listar(String nome, String codigo, Boolean ativo, Pageable pageable);
    void excluir(Long id);
}
