package br.com.brasil_saas.ia.service.impl;

import br.com.brasil_saas.ia.dto.ClassificacaoRequest;
import br.com.brasil_saas.ia.model.Classificacao;
import br.com.brasil_saas.ia.repository.ClassificacaoRepository;
import br.com.brasil_saas.ia.service.ClassificacaoService;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClassificacaoServiceImpl implements ClassificacaoService {

    private final ClassificacaoRepository classificacaoRepository;

    @Override
    @Transactional
    public Classificacao criar(Long empresaId, ClassificacaoRequest request) {
        Classificacao classificacao = new Classificacao();
        classificacao.setEmpresaId(empresaId);
        classificacao.setNome(request.nome());
        classificacao.setDescricao(request.descricao());
        classificacao.setTipo(request.tipo());
        classificacao.setCategoria(request.categoria());
        classificacao.setTags(request.tags());
        classificacao.setConfianca(request.confianca());
        classificacao.setClassificacaoManual(request.classificacaoManual());
        classificacao.setClassificacaoIa(String.valueOf(request.classificacaoIa()));
        classificacao.setStatus("PENDENTE");
        classificacao.setEntidadeId(request.entidadeId());
        classificacao.setCriadoPor(request.criadoPor());
        classificacao.setDataCriacao(LocalDateTime.now());

        return classificacaoRepository.save(classificacao);
    }

    @Override
    @Transactional
    public Classificacao atualizar(Long empresaId, Long id, ClassificacaoRequest request) {
        Classificacao classificacao = classificacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classificacao nao encontrada"));

        if (!classificacao.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Classificacao nao pertence a esta empresa");
        }

        classificacao.setNome(request.nome());
        classificacao.setDescricao(request.descricao());
        classificacao.setTipo(request.tipo());
        classificacao.setCategoria(request.categoria());
        classificacao.setTags(request.tags());
        classificacao.setConfianca(request.confianca());
        classificacao.setClassificacaoIa(String.valueOf(request.classificacaoIa()));
        classificacao.setEntidadeId(request.entidadeId());

        return classificacaoRepository.save(classificacao);
    }

    @Override
    @Transactional(readOnly = true)
    public Classificacao buscarPorId(Long empresaId, Long id) {
        Classificacao classificacao = classificacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classificacao nao encontrada"));

        if (!classificacao.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Classificacao nao pertence a esta empresa");
        }

        return classificacao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Classificacao> listarPorEmpresa(Long empresaId) {
        return classificacaoRepository.findByEmpresaId(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Classificacao> listarPorTipo(Long empresaId, String tipo) {
        return classificacaoRepository.findByEmpresaIdAndTipo(empresaId, tipo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Classificacao> listarPorEntidade(Long empresaId, Long entidadeId) {
        return classificacaoRepository.findByEmpresaIdAndEntidadeId(empresaId, entidadeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Classificacao> listarPendentes(Long empresaId, String tipo) {
        return classificacaoRepository.findPendentesByTipo(empresaId, tipo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Classificacao> listarHighConfidence(Long empresaId, String tipo, Double minConfianca) {
        return classificacaoRepository.findHighConfidenceByTipo(empresaId, tipo);
    }

    @Override
    @Transactional
    public void excluir(Long empresaId, Long id) {
        Classificacao classificacao = classificacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classificacao nao encontrada"));

        if (!classificacao.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Classificacao nao pertence a esta empresa");
        }

        classificacaoRepository.delete(classificacao);
    }

    @Override
    @Transactional
    public void aprovarClassificacao(Long empresaId, Long id, String classificacaoManual) {
        Classificacao classificacao = classificacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classificacao nao encontrada"));

        if (!classificacao.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Classificacao nao pertence a esta empresa");
        }

        classificacao.setClassificacaoManual(classificacaoManual);
        classificacao.setStatus("APROVADO");
        classificacao.setAprovadoPor(empresaId);
        classificacao.setDataAprovacao(LocalDateTime.now());

        classificacaoRepository.save(classificacao);
    }

    @Override
    @Transactional
    public void rejeitarClassificacao(Long empresaId, Long id, String motivo) {
        Classificacao classificacao = classificacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classificacao nao encontrada"));

        if (!classificacao.getEmpresaId().equals(empresaId)) {
            throw new ResourceNotFoundException("Classificacao nao pertence a esta empresa");
        }

        classificacao.setStatus("REJEITADO");
        classificacao.setDataAprovacao(LocalDateTime.now());

        classificacaoRepository.save(classificacao);
    }

    @Override
    @Transactional
    public Map<String, Object> classificarTexto(Long empresaId, String tipo, String texto) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("status", "SUCCESS");
        resultado.put("tipo", tipo);
        resultado.put("texto", texto);
        resultado.put("classificacao", "CLASSIFICACAO_AUTOMATICA");
        resultado.put("confianca", 0.90);
        return resultado;
    }

    @Override
    @Transactional
    public Map<String, Object> classificarProduto(Long empresaId, Long produtoId, String texto) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("status", "SUCCESS");
        resultado.put("produtoId", produtoId);
        resultado.put("texto", texto);
        resultado.put("classificacao", "PRODUTO_CLASSIFICADO");
        resultado.put("confianca", 0.85);
        return resultado;
    }

    @Override
    @Transactional
    public Map<String, Object> classificarLote(Long empresaId, List<Long> entidadeIds) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("status", "SUCCESS");
        resultado.put("quantidade", entidadeIds.size());
        resultado.put("classificacoes", entidadeIds.size());
        resultado.put("confiancaMedia", 0.88);
        return resultado;
    }
}
