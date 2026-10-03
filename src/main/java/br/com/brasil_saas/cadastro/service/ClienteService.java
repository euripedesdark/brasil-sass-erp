package br.com.brasil_saas.cadastro.service;

import br.com.brasil_saas.cadastro.service.dto.ClienteDtos;
import br.com.brasil_saas.shared.web.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ClienteService {
    PageResponse<ClienteDtos.Response> listar(Long empresaId, Pageable pageable);
    ClienteDtos.Response buscarPorId(Long empresaId, Long id);
    ClienteDtos.Response criar(Long empresaId, ClienteDtos.Request request);
    ClienteDtos.Response atualizar(Long empresaId, Long id, ClienteDtos.Request request);
    void excluir(Long empresaId, Long id);
}
