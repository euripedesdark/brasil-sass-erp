package br.com.brasil_saas.ia.service;

import br.com.brasil_saas.ia.dto.ClassificacaoRequest;
import br.com.brasil_saas.ia.model.Classificacao;

import java.util.List;
import java.util.Map;

public interface ClassificacaoService {

    Classificacao criar(Long empresaId, ClassificacaoRequest request);

    Classificacao atualizar(Long empresaId, Long id, ClassificacaoRequest request);

    Classificacao buscarPorId(Long empresaId, Long id);

    List<Classificacao> listarPorEmpresa(Long empresaId);

    List<Classificacao> listarPorTipo(Long empresaId, String tipo);

    List<Classificacao> listarPorEntidade(Long empresaId, Long entidadeId);

    List<Classificacao> listarPendentes(Long empresaId, String tipo);

    List<Classificacao> listarHighConfidence(Long empresaId, String tipo, Double minConfianca);

    void excluir(Long empresaId, Long id);

    void aprovarClassificacao(Long empresaId, Long id, String classificacaoManual);

    void rejeitarClassificacao(Long empresaId, Long id, String motivo);

    Map<String, Object> classificarTexto(Long empresaId, String tipo, String texto);

    Map<String, Object> classificarProduto(Long empresaId, Long produtoId, String texto);

    Map<String, Object> classificarLote(Long empresaId, List<Long> entidadeIds);
}
