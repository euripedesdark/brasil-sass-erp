package br.com.brasil_saas.cadastro.service;

import br.com.brasil_saas.cadastro.service.dto.TransportadoraDtos;
import br.com.brasil_saas.shared.web.PageResponse;
import org.springframework.data.domain.Pageable;

public interface TransportadoraService {
    PageResponse<TransportadoraDtos.Response> listar(Long empresaId, Pageable pageable);
    TransportadoraDtos.Response buscarPorId(Long empresaId, Long id);
    TransportadoraDtos.Response criar(Long empresaId, TransportadoraDtos.Request request);
    TransportadoraDtos.Response atualizar(Long empresaId, Long id, TransportadoraDtos.Request request);
    void excluir(Long empresaId, Long id);
}
