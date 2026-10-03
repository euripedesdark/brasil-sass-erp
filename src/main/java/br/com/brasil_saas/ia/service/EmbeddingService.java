package br.com.brasil_saas.ia.service;

import br.com.brasil_saas.ia.dto.EmbeddingRequest;
import br.com.brasil_saas.ia.model.Embedding;

import java.util.List;
import java.util.Map;

public interface EmbeddingService {

    Embedding criar(Long empresaId, EmbeddingRequest request);

    Embedding atualizar(Long empresaId, Long id, EmbeddingRequest request);

    Embedding buscarPorId(Long empresaId, Long id);

    List<Embedding> listarPorEmpresa(Long empresaId);

    List<Embedding> listarPorEntidadeTipo(Long empresaId, String entidadeTipo);

    List<Embedding> listarPorEntidade(Long empresaId, String entidadeTipo, Long entidadeId);

    void excluir(Long empresaId, Long id);

    void excluirPorEntidade(Long empresaId, String entidadeTipo, Long entidadeId);

    float[] gerarEmbedding(Long empresaId, String texto);

    Map<String, Object> buscarSimilares(Long empresaId, String entidadeTipo, String texto, Integer limite);

    Map<String, Object> buscarMaisSimilar(Long empresaId, String entidadeTipo, String texto);

    void reindexarEmbeddings(Long empresaId, String entidadeTipo);
}
