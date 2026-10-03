package br.com.brasil_saas.cadastro.service;

import br.com.brasil_saas.cadastro.dto.PessoaRequest;
import br.com.brasil_saas.cadastro.dto.PessoaResponse;
import br.com.brasil_saas.shared.web.PageResponse;
import org.springframework.data.domain.Pageable;

public interface PessoaService {
    PessoaResponse criar(PessoaRequest request, Long empresaId);
    PessoaResponse atualizar(Long empresaId, Long id, PessoaRequest request);
    PessoaResponse buscarPorId(Long empresaId, Long id);
    PageResponse<PessoaResponse> listar(Long empresaId, String nome, String documento, Pageable pageable);
    void excluir(Long empresaId, Long id);
}
