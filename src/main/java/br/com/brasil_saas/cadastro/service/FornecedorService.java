package br.com.brasil_saas.cadastro.service;

import br.com.brasil_saas.cadastro.service.dto.FornecedorDtos;
import br.com.brasil_saas.shared.web.PageResponse;
import org.springframework.data.domain.Pageable;

public interface FornecedorService {
    PageResponse<FornecedorDtos.Response> listar(Long empresaId, Pageable pageable);
    FornecedorDtos.Response buscarPorId(Long empresaId, Long id);
    FornecedorDtos.Response criar(Long empresaId, FornecedorDtos.Request request);
    FornecedorDtos.Response atualizar(Long empresaId, Long id, FornecedorDtos.Request request);
    void excluir(Long empresaId, Long id);
}
