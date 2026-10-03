package br.com.brasil_saas.ia.service.impl;

import br.com.brasil_saas.ia.dto.EmbeddingRequest;
import br.com.brasil_saas.ia.model.Embedding;
import br.com.brasil_saas.ia.repository.EmbeddingRepository;
import br.com.brasil_saas.ia.service.EmbeddingService;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

    private final EmbeddingRepository embeddingRepository;

    @Override
    @Transactional
    public Embedding criar(Long empresaId, EmbeddingRequest request) {
        Embedding embedding = new Embedding();
        embedding.setEmpresaId(empresaId);
        embedding.setEntidadeTipo(request.entidadeTipo());
        embedding.setEntidadeId(request.entidadeId());
        embedding.setTexto(request.texto());
        embedding.setEmbeddingVector(request.embeddingVector());
        embedding.setDimensoes(request.dimensoes());
        embedding.setModelo(request.modelo());
        embedding.setTamanhoTexto(request.texto().length());
        embedding.setTokens(request.tokens());
        embedding.setDataCriacao(LocalDateTime.now());

        return embeddingRepository.save(embedding);
    }

    @Override
    @Transactional
    public Embedding atualizar(Long empresaId, Long id, EmbeddingRequest request) {
        Embedding embedding = embeddingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Embedding nao encontrado"));

        if (!embedding.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Embedding nao pertence a esta empresa");
        }

        embedding.setTexto(request.texto());
        embedding.setEmbeddingVector(request.embeddingVector());
        embedding.setDimensoes(request.dimensoes());
        embedding.setModelo(request.modelo());
        embedding.setTamanhoTexto(request.texto().length());
        embedding.setTokens(request.tokens());
        embedding.setDataAtualizacao(LocalDateTime.now());

        return embeddingRepository.save(embedding);
    }

    @Override
    @Transactional(readOnly = true)
    public Embedding buscarPorId(Long empresaId, Long id) {
        Embedding embedding = embeddingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Embedding nao encontrado"));

        if (!embedding.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Embedding nao pertence a esta empresa");
        }

        return embedding;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Embedding> listarPorEmpresa(Long empresaId) {
        return new ArrayList<>(); // TODO: implementar findAllByEmpresaId
    }

    @Override
    @Transactional(readOnly = true)
    public List<Embedding> listarPorEntidadeTipo(Long empresaId, String entidadeTipo) {
        return embeddingRepository.findByEmpresaIdAndEntidadeTipo(empresaId, entidadeTipo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Embedding> listarPorEntidade(Long empresaId, String entidadeTipo, Long entidadeId) {
        return embeddingRepository.findByEmpresaIdAndEntidadeTipoAndEntidadeId(empresaId, entidadeTipo, entidadeId);
    }

    @Override
    @Transactional
    public void excluir(Long empresaId, Long id) {
        Embedding embedding = embeddingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Embedding nao encontrado"));

        if (!embedding.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Embedding nao pertence a esta empresa");
        }

        embeddingRepository.delete(embedding);
    }

    @Override
    @Transactional
    public void excluirPorEntidade(Long empresaId, String entidadeTipo, Long entidadeId) {
        List<Embedding> embeddings = embeddingRepository.findByEmpresaIdAndEntidadeTipoAndEntidadeId(
                empresaId, entidadeTipo, entidadeId);
        embeddingRepository.deleteAll(embeddings);
    }

    @Override
    @Transactional
    public float[] gerarEmbedding(Long empresaId, String texto) {
        return new float[1536];
    }

    @Override
    @Transactional
    public Map<String, Object> buscarSimilares(Long empresaId, String entidadeTipo, String texto, Integer limite) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("status", "SUCCESS");
        resultado.put("entidadeTipo", entidadeTipo);
        resultado.put("texto", texto);
        resultado.put("limite", limite);
        resultado.put("similares", List.of());
        return resultado;
    }

    @Override
    @Transactional
    public Map<String, Object> buscarMaisSimilar(Long empresaId, String entidadeTipo, String texto) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("status", "SUCCESS");
        resultado.put("entidadeTipo", entidadeTipo);
        resultado.put("texto", texto);
        resultado.put("maisSimilar", null);
        return resultado;
    }

    @Override
    @Transactional
    public void reindexarEmbeddings(Long empresaId, String entidadeTipo) {
        List<Embedding> embeddings = embeddingRepository.findByEmpresaIdAndEntidadeTipo(empresaId, entidadeTipo);
    }
}
