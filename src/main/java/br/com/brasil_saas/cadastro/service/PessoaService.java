package br.com.brasil_saas.cadastro.service;

import br.com.brasil_saas.cadastro.dto.PessoaRequest;
import br.com.brasil_saas.cadastro.dto.PessoaResponse;
import br.com.brasil_saas.shared.web.PageResponse;
import org.springframework.data.domain.Pageable;

public interface PessoaService {
    PessoaResponse criar(PessoaRequest request, Long empresaId);
    PessoaResponse atualizar(Long id, PessoaRequest request);
    PessoaResponse buscarPorId(Long id);
    PageResponse<PessoaResponse> listar(String nome, String documento, Pageable pageable);
    void excluir(Long id);
}
