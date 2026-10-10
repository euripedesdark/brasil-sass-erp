package br.com.brasil_saas.plm.service.impl;

import br.com.brasil_saas.core.service.DocumentoFluxoService;
import br.com.brasil_saas.plm.model.*;
import br.com.brasil_saas.plm.repository.*;
import br.com.brasil_saas.plm.service.PlmService;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service @RequiredArgsConstructor
public class PlmServiceImpl implements PlmService {

    private final PlmMudancaRepository mudancas;
    private final PlmEfeitoRepository efeitos;
    private final PlmAprovacaoRepository aprovacoes;
    private final PlmRevisaoRepository revisoes;
    private final DocumentoFluxoService documentoFluxo;

    @Override @Transactional(readOnly = true)
    public List<PlmMudanca> mudancas(Long empresaId) {
        return mudancas.findByEmpresaIdAndDeletedAtIsNullOrderByIdDesc(empresaId);
    }

    @Override @Transactional
    public PlmMudanca criarMudanca(Long empresaId, Long userId, MudancaRequest request) {
        PlmMudanca m = new PlmMudanca();
        m.setEmpresaId(empresaId);
        m.setNumero(request.numero().trim());
        m.setTipo(request.tipo() == null || request.tipo().isBlank() ? "ENGENHARIA" : request.tipo().trim().toUpperCase());
        m.setTitulo(request.titulo().trim());
        m.setDescricao(request.descricao());
        m.setPrioridade(request.prioridade() == null || request.prioridade().isBlank() ? "NORMAL" : request.prioridade().trim().toUpperCase());
        m.setStatus("ABERTA");
        m.setSolicitanteId(userId);
        m.setCreatedAt(LocalDateTime.now());
        m.setCreatedBy(userId);
        return mudancas.save(m);
    }

    @Override @Transactional
    public PlmMudanca enviarAprovacao(Long empresaId, Long userId, Long mudancaId, Long aprovadorId) {
        PlmMudanca m = mudancas.findByIdForUpdate(mudancaId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Mudanca inexistente"));
        if (!"ABERTA".equals(m.getStatus())) throw new BusinessException("Somente mudanca ABERTA vai para aprovacao");
        if (efeitos.findByEmpresaIdAndMudancaIdOrderByOrdemExecucaoAscIdAsc(empresaId, mudancaId).isEmpty()) {
            throw new BusinessException("Mudanca sem efeitos nao vai para aprovacao");
        }
        PlmAprovacao etapa = new PlmAprovacao();
        etapa.setEmpresaId(empresaId);
        etapa.setMudancaId(mudancaId);
        etapa.setEtapa(1);
        etapa.setAprovadorId(aprovadorId);
        etapa.setDecisao("PENDENTE");
        aprovacoes.save(etapa);
        m.setStatus("EM_APROVACAO");
        m.setAprovadorId(aprovadorId);
        mudancas.save(m);
        documentoFluxo.ligar(empresaId, userId, "MUDANCA_PLM", m.getId(), m.getNumero(),
                "MUDANCA_PLM", m.getId(), m.getNumero(), "AGUARDA_APROVACAO");
        return m;
    }

    @Override @Transactional
    public PlmMudanca decidirEtapa(Long empresaId, Long userId, Long mudancaId, boolean aprovar, String observacao) {
        PlmMudanca m = mudancas.findByIdForUpdate(mudancaId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Mudanca inexistente"));
        if (!"EM_APROVACAO".equals(m.getStatus())) throw new BusinessException("Somente mudanca em aprovacao pode ser decidida");
        PlmAprovacao etapa = aprovacoes.findByEmpresaIdAndMudancaIdAndEtapa(empresaId, mudancaId, 1)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa de aprovacao inexistente"));
        if (!"PENDENTE".equals(etapa.getDecisao())) throw new BusinessException("Etapa ja decidida");
        etapa.setDecisao(aprovar ? "APROVADA" : "REJEITADA");
        etapa.setObservacao(observacao);
        etapa.setDecididoEm(java.time.LocalDateTime.now());
        if (etapa.getAprovadorId() == null) etapa.setAprovadorId(userId);
        aprovacoes.save(etapa);
        m.setStatus(aprovar ? "APROVADA" : "REJEITADA");
        if (aprovar) m.setAprovadoEm(java.time.LocalDateTime.now());
        mudancas.save(m);
        documentoFluxo.ligar(empresaId, userId, "MUDANCA_PLM", m.getId(), m.getNumero(),
                "MUDANCA_PLM", m.getId(), m.getNumero(), aprovar ? "APROVADA" : "REJEITADA");
        return m;
    }
    
    @Override @Transactional
    public PlmMudanca implementar(Long empresaId, Long userId, Long mudancaId) {
        PlmMudanca m = mudancas.findByIdForUpdate(mudancaId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Mudanca inexistente"));
        if ("IMPLEMENTADA".equals(m.getStatus())) return m;
        if (!"APROVADA".equals(m.getStatus())) throw new BusinessException("Somente mudanca aprovada pode ser implementada");
        var lista = efeitos.findByEmpresaIdAndMudancaIdOrderByOrdemExecucaoAscIdAsc(empresaId, mudancaId);
        if (lista.isEmpty()) throw new BusinessException("Mudanca sem efeitos nao pode ser implementada");
        for (var efeito : lista) {
            if ("APLICADA".equals(efeito.getStatus())) continue;
            aplicarEfeito(empresaId, userId, efeito);
        }
        m.setStatus("IMPLEMENTADA");
        m.setImplementadoEm(java.time.LocalDateTime.now());
        mudancas.save(m);
        documentoFluxo.ligar(empresaId, userId, "MUDANCA_PLM", m.getId(), m.getNumero(),
                "MUDANCA_PLM", m.getId(), m.getNumero(), "IMPLEMENTADA");
        return m;
    }

    private void aplicarEfeito(Long empresaId, Long userId, PlmEfeito efeito) {
        try {
            if ("VIGORAR_REVISAO".equals(efeito.getAcao())) {
                PlmRevisao revisao = revisoes.findByIdForUpdate(efeito.getEntidadeId(), empresaId)
                        .orElseThrow(() -> new ResourceNotFoundException("Revisao inexistente"));
                vigorarRevisaoInterna(empresaId, userId, revisao);
                efeito.setRevisaoNova(revisao.getRevisao());
            } else if (!"REGISTRAR".equals(efeito.getAcao())) {
                throw new BusinessException("Acao de efeito desconhecida: " + efeito.getAcao());
            }
            efeito.setStatus("APLICADA");
            efeito.setAplicadoEm(java.time.LocalDateTime.now());
            efeito.setAplicadoPor(userId);
            efeito.setErroImplementacao(null);
        } catch (RuntimeException ex) {
            efeito.setStatus("ERRO");
            String detalhe = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            efeito.setErroImplementacao(detalhe.length() > 2000 ? detalhe.substring(0, 2000) : detalhe);
            efeitos.save(efeito);
            throw ex;
        }
        efeitos.save(efeito);
    }

    @Override @Transactional(readOnly = true)
    public List<PlmEfeito> efeitos(Long empresaId, Long mudancaId) {
        mudancas.findByIdAndEmpresaIdAndDeletedAtIsNull(mudancaId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Mudanca inexistente"));
        return efeitos.findByEmpresaIdAndMudancaIdOrderByOrdemExecucaoAscIdAsc(empresaId, mudancaId);
    }

    @Override @Transactional
    public PlmEfeito adicionarEfeito(Long empresaId, EfeitoRequest request) {
        PlmMudanca m = mudancas.findByIdAndEmpresaIdAndDeletedAtIsNull(request.mudancaId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Mudanca inexistente"));
        if (!"ABERTA".equals(m.getStatus())) throw new BusinessException("Somente mudanca ABERTA aceita efeitos");
        String acao = request.acao() == null ? "" : request.acao().trim().toUpperCase();
        if (!"VIGORAR_REVISAO".equals(acao) && !"REGISTRAR".equals(acao)) {
            throw new BusinessException("Acao deve ser VIGORAR_REVISAO ou REGISTRAR");
        }
        if ("VIGORAR_REVISAO".equals(acao)) {
            revisoes.findByIdAndEmpresaId(request.entidadeId(), empresaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Revisao inexistente"));
        }
        PlmEfeito efeito = new PlmEfeito();
        efeito.setEmpresaId(empresaId);
        efeito.setMudancaId(request.mudancaId());
        efeito.setEntidadeTipo(request.entidadeTipo().trim().toUpperCase());
        efeito.setEntidadeId(request.entidadeId());
        efeito.setAcao(acao);
        efeito.setRevisaoAnterior(request.revisaoAnterior());
        efeito.setRevisaoNova(request.revisaoNova());
        efeito.setEfetivaEm(request.efetivaEm());
        efeito.setOrdemExecucao(request.ordemExecucao() == null ? 1 : request.ordemExecucao());
        efeito.setObrigatorio(true);
        efeito.setStatus("PENDENTE");
        efeito.setObservacao(request.observacao());
        return efeitos.save(efeito);
    }

    @Override @Transactional(readOnly = true)
    public List<PlmRevisao> revisoes(Long empresaId, Long produtoId) {
        return revisoes.findByEmpresaIdAndProdutoIdOrderByIdDesc(empresaId, produtoId);
    }

    @Override @Transactional
    public PlmRevisao criarRevisao(Long empresaId, Long userId, RevisaoRequest request) {
        PlmRevisao revisao = new PlmRevisao();
        revisao.setEmpresaId(empresaId);
        revisao.setProdutoId(request.produtoId());
        revisao.setRevisao(request.revisao().trim());
        revisao.setDescricao(request.descricao());
        revisao.setStatus("EM_DESENVOLVIMENTO");
        revisao.setMotivo(request.motivo());
        revisao.setCriadoPor(userId);
        return revisoes.save(revisao);
    }

    @Override @Transactional
    public PlmRevisao vigorarRevisao(Long empresaId, Long userId, Long revisaoId) {
        PlmRevisao revisao = revisoes.findByIdForUpdate(revisaoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Revisao inexistente"));
        return vigorarRevisaoInterna(empresaId, userId, revisao);
    }

    private PlmRevisao vigorarRevisaoInterna(Long empresaId, Long userId, PlmRevisao revisao) {
        if ("VIGENTE".equals(revisao.getStatus())) return revisao;
        for (PlmRevisao anterior : revisoes.findByEmpresaIdAndProdutoIdOrderByIdDesc(empresaId, revisao.getProdutoId())) {
            if (anterior.getId().equals(revisao.getId())) continue;
            if ("VIGENTE".equals(anterior.getStatus())) {
                anterior.setStatus("SUBSTITUIDA");
                anterior.setVigenteAte(LocalDate.now());
                revisoes.save(anterior);
            }
        }
        revisao.setStatus("VIGENTE");
        revisao.setVigenteDesde(LocalDate.now());
        revisao.setAprovadoPor(userId);
        revisao.setAprovadoEm(java.time.LocalDateTime.now());
        return revisoes.save(revisao);
    }
}
