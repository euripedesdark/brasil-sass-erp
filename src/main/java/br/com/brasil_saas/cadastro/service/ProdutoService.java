package br.com.brasil_saas.cadastro.service;

import br.com.brasil_saas.cadastro.dto.ProdutoRequest;
import br.com.brasil_saas.cadastro.dto.ProdutoResponse;
import br.com.brasil_saas.shared.web.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ProdutoService {
    ProdutoResponse criar(ProdutoRequest request, Long empresaId);
    ProdutoResponse atualizar(Long id, ProdutoRequest request);
    ProdutoResponse buscarPorId(Long id);
    PageResponse<ProdutoResponse> listar(String nome, String codigo, Long categoriaId, Long marcaId, Boolean ativo, Pageable pageable);
    void excluir(Long id);
}
