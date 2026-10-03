package br.com.brasil_saas.ia.service;

import br.com.brasil_saas.ia.dto.PromptRequest;
import br.com.brasil_saas.ia.model.Prompt;

import java.util.List;

public interface PromptService {

    Prompt criar(Long empresaId, PromptRequest request);

    Prompt atualizar(Long empresaId, Long id, PromptRequest request);

    Prompt buscarPorId(Long empresaId, Long id);

    List<Prompt> listarPorEmpresa(Long empresaId);

    List<Prompt> listarPublicos(Long empresaId);

    List<Prompt> listarPorCategoria(Long empresaId, String categoria);

    List<Prompt> listarFavoritos(Long empresaId);

    List<Prompt> listarMaisUsados(Long empresaId);

    List<Prompt> buscarPorTermo(Long empresaId, String termo);

    void excluir(Long empresaId, Long id);

    void alternarFavorito(Long empresaId, Long id);

    void incrementarContadorUso(Long empresaId, Long id);
}
