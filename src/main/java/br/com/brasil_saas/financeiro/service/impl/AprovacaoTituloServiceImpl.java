package br.com.brasil_saas.financeiro.service.impl;

import br.com.brasil_saas.core.model.Usuario;
import br.com.brasil_saas.core.repository.UsuarioRepository;
import br.com.brasil_saas.financeiro.dto.FinanceiroDtos.*;
import br.com.brasil_saas.financeiro.model.Aprovacao;
import br.com.brasil_saas.financeiro.model.FluxoAprovacao;
import br.com.brasil_saas.financeiro.model.Titulo;
import br.com.brasil_saas.financeiro.repository.AprovacaoRepository;
import br.com.brasil_saas.financeiro.repository.FluxoAprovacaoRepository;
import br.com.brasil_saas.financeiro.repository.TituloRepository;
import br.com.brasil_saas.financeiro.service.AprovacaoTituloService;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AprovacaoTituloServiceImpl implements AprovacaoTituloService {

    private static final String PENDENTE = "PENDENTE";
    private static final String APROVADO = "APROVADO";
    private static final String REJEITADO = "REJEITADO";
    private static final String CANCELADO = "CANCELADO";

    private final AprovacaoRepository aprovacaoRepository;
    private final FluxoAprovacaoRepository fluxoRepository;
    private final TituloRepository tituloRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public List<AprovacaoResponse> solicitar(Long empresaId, Long usuarioId, Long tituloId,
                                             SolicitacaoAprovacaoRequest request) {
        Titulo titulo = tituloRepository.findForUpdate(tituloId, empresaId)
            .orElseThrow(() -> new ResourceNotFoundException("Título não encontrado"));

        if (!"ABERTO".equals(titulo.getStatus())) {
            throw new BusinessException("Somente títulos ABERTO podem entrar em aprovação (status atual: "
                + titulo.getStatus() + ")");
        }
        if (!aprovacaoRepository.findByTituloIdAndStatusAndNivelAndDeletedAtIsNull(tituloId, PENDENTE, 1).isEmpty()) {
            throw new BusinessException("Título já possui aprovação pendente");
        }

        if (request.fluxoAprovacaoId() != null) {
            FluxoAprovacao fluxo = fluxoRepository
                .findByIdAndEmpresaIdAndDeletedAtIsNull(request.fluxoAprovacaoId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Fluxo de aprovação não encontrado"));
            if (!Boolean.TRUE.equals(fluxo.getAtivo())) {
                throw new BusinessException("Fluxo de aprovação inativo");
            }
        }

        int niveis = request.niveis() == null ? 1 : request.niveis();
        if (niveis < 1 || niveis > 10) {
            throw new BusinessException("Número de níveis de aprovação deve estar entre 1 e 10");
        }

        LocalDateTime agora = LocalDateTime.now();
        List<Aprovacao> criadas = new ArrayList<>();
        for (int nivel = 1; nivel <= niveis; nivel++) {
            Aprovacao a = new Aprovacao();
            a.setEmpresaId(empresaId);
            a.setTituloId(tituloId);
            a.setDocumentoId(tituloId);
            a.setTipoDocumento("TITULO");
            a.setNumeroDocumento(titulo.getNumeroDocumento());
            a.setFluxoAprovacaoId(request.fluxoAprovacaoId());
            a.setNivel(nivel);
            a.setUsuarioSolicitanteId(usuarioId);
            a.setDataSolicitacao(agora);
            a.setUsuarioAprovadorId(nivel == 1 ? request.usuarioAprovadorId() : null);
            a.setStatus(nivel == 1 ? PENDENTE : "AGUARDANDO_NIVEL");
            a.setObservacao(nivel == 1 ? request.observacao() : null);
            criadas.add(a);
        }
        aprovacaoRepository.saveAll(criadas);

        titulo.setStatus("PENDENTE_APROVACAO");
        tituloRepository.save(titulo);

        return toResponses(criadas);
    }

    @Override
    @Transactional
    public AprovacaoResponse aprovar(Long empresaId, Long usuarioId, Long aprovacaoId,
                                     DecisaoAprovacaoRequest request) {
        Aprovacao atual = obterPendenteParaUpdate(empresaId, usuarioId, aprovacaoId);

        atual.setStatus(APROVADO);
        atual.setUsuarioAprovadorId(usuarioId);
        atual.setDataAprovacao(LocalDateTime.now());
        atual.setObservacao(combinarObservacao(atual.getObservacao(), request == null ? null : request.observacao()));
        aprovacaoRepository.save(atual);

        Titulo titulo = tituloRepository.findForUpdate(atual.getTituloId(), empresaId)
            .orElseThrow(() -> new ResourceNotFoundException("Título não encontrado"));

        // Promove o próximo nível, se existir; caso contrário libera o título.
        List<Aprovacao> trilha = aprovacaoRepository.findByTituloIdAndDeletedAtIsNullOrderByNivelAsc(atual.getTituloId());
        Aprovacao proximo = trilha.stream()
            .filter(a -> "AGUARDANDO_NIVEL".equals(a.getStatus()) && a.getNivel() > atual.getNivel())
            .findFirst().orElse(null);

        if (proximo != null) {
            proximo.setStatus(PENDENTE);
            proximo.setUsuarioAprovadorId(proximo.getUsuarioAprovadorId() != null
                ? proximo.getUsuarioAprovadorId() : atual.getUsuarioAprovadorId());
            proximo.setDataSolicitacao(LocalDateTime.now());
            aprovacaoRepository.save(proximo);
        } else {
            if ("PENDENTE_APROVACAO".equals(titulo.getStatus())) {
                titulo.setStatus("ABERTO");
                tituloRepository.save(titulo);
            }
        }
        return toResponse(atual);
    }

    @Override
    @Transactional
    public AprovacaoResponse rejeitar(Long empresaId, Long usuarioId, Long aprovacaoId,
                                      DecisaoAprovacaoRequest request) {
        Aprovacao atual = obterPendenteParaUpdate(empresaId, usuarioId, aprovacaoId);

        atual.setStatus(REJEITADO);
        atual.setUsuarioAprovadorId(usuarioId);
        atual.setDataRejeicao(LocalDateTime.now());
        atual.setObservacao(combinarObservacao(atual.getObservacao(), request == null ? null : request.observacao()));
        aprovacaoRepository.save(atual);

        // Cancela os demais níveis pendentes/aguardando do mesmo título.
        List<Aprovacao> trilha = aprovacaoRepository.findByTituloIdAndDeletedAtIsNullOrderByNivelAsc(atual.getTituloId());
        for (Aprovacao a : trilha) {
            if (!a.getId().equals(atual.getId())
                && (PENDENTE.equals(a.getStatus()) || "AGUARDANDO_NIVEL".equals(a.getStatus()))) {
                a.setStatus(CANCELADO);
                a.setDataAprovacao(LocalDateTime.now());
                aprovacaoRepository.save(a);
            }
        }

        Titulo titulo = tituloRepository.findForUpdate(atual.getTituloId(), empresaId)
            .orElseThrow(() -> new ResourceNotFoundException("Título não encontrado"));
        if ("PENDENTE_APROVACAO".equals(titulo.getStatus())) {
            titulo.setStatus("ABERTO");
            tituloRepository.save(titulo);
        }
        return toResponse(atual);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AprovacaoResponse> pendentes(Long empresaId, Long usuarioId) {
        return toResponses(aprovacaoRepository.findPendentesParaUsuario(empresaId, usuarioId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AprovacaoResponse> porTitulo(Long empresaId, Long tituloId) {
        tituloRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(tituloId, empresaId)
            .orElseThrow(() -> new ResourceNotFoundException("Título não encontrado"));
        return toResponses(aprovacaoRepository.findByTituloIdAndDeletedAtIsNullOrderByNivelAsc(tituloId));
    }

    private Aprovacao obterPendenteParaUpdate(Long empresaId, Long usuarioId, Long aprovacaoId) {
        Aprovacao atual = aprovacaoRepository.findForUpdate(aprovacaoId, empresaId)
            .orElseThrow(() -> new ResourceNotFoundException("Aprovação não encontrada"));
        if (!PENDENTE.equals(atual.getStatus())) {
            throw new BusinessException("Aprovação já decidida (status: " + atual.getStatus() + ")");
        }
        if (atual.getUsuarioSolicitanteId() != null && atual.getUsuarioSolicitanteId().equals(usuarioId)) {
            throw new BusinessException("Segregação de funções: o solicitante não pode aprovar o próprio pedido");
        }
        if (atual.getUsuarioAprovadorId() != null && !atual.getUsuarioAprovadorId().equals(usuarioId)) {
            throw new BusinessException("Esta aprovação está designada a outro usuário");
        }
        return atual;
    }

    private String combinarObservacao(String original, String nova) {
        if (nova == null || nova.isBlank()) return original;
        return (original == null || original.isBlank()) ? nova : original + " | " + nova;
    }

    private AprovacaoResponse toResponse(Aprovacao a) {
        return toResponses(List.of(a)).get(0);
    }

    /**
     * Converte a lista enriquecendo cada aprovação com dados do título (número, tipo,
     * saldo, vencimento) e nomes de solicitante/aprovador, carregados em lote para
     * evitar N+1 (V87__financeiro_aprovacao_enriquecer_trilha.sql).
     */
    private List<AprovacaoResponse> toResponses(List<Aprovacao> aprovacoes) {
        Set<Long> tituloIds = new HashSet<>();
        Set<Long> usuarioIds = new HashSet<>();
        for (Aprovacao a : aprovacoes) {
            if (a.getTituloId() != null) tituloIds.add(a.getTituloId());
            if (a.getUsuarioSolicitanteId() != null) usuarioIds.add(a.getUsuarioSolicitanteId());
            if (a.getUsuarioAprovadorId() != null) usuarioIds.add(a.getUsuarioAprovadorId());
        }
        Map<Long, Titulo> titulos = new HashMap<>();
        if (!tituloIds.isEmpty()) {
            for (Titulo t : tituloRepository.findAllById(tituloIds)) {
                titulos.put(t.getId(), t);
            }
        }
        Map<Long, String> nomes = new HashMap<>();
        if (!usuarioIds.isEmpty()) {
            for (Usuario u : usuarioRepository.findAllById(usuarioIds)) {
                nomes.put(u.getId(), u.getNome());
            }
        }
        List<AprovacaoResponse> respostas = new ArrayList<>(aprovacoes.size());
        for (Aprovacao a : aprovacoes) {
            Titulo t = a.getTituloId() == null ? null : titulos.get(a.getTituloId());
            respostas.add(new AprovacaoResponse(a.getId(), a.getTituloId(), a.getFluxoAprovacaoId(), a.getNivel(),
                a.getStatus(), a.getUsuarioSolicitanteId(), a.getUsuarioAprovadorId(),
                a.getDataSolicitacao(), a.getDataAprovacao(), a.getDataRejeicao(), a.getObservacao(),
                a.getNumeroDocumento() != null ? a.getNumeroDocumento()
                    : (t == null ? null : t.getNumeroDocumento()),
                t == null ? null : t.getTipo(),
                t == null ? null : t.getValorSaldo(),
                t == null ? null : t.getDataVencimento(),
                a.getUsuarioSolicitanteId() == null ? null : nomes.get(a.getUsuarioSolicitanteId()),
                a.getUsuarioAprovadorId() == null ? null : nomes.get(a.getUsuarioAprovadorId())));
        }
        return respostas;
    }
}
