package br.com.brasil_saas.qualidade.controller;

import br.com.brasil_saas.qualidade.model.*;
import br.com.brasil_saas.qualidade.repository.*;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/qualidade")
@RequiredArgsConstructor
public class QualidadeController {
    private final PlanoInspecaoRepository planos;
    private final InspecaoRepository inspecoes;
    private final NaoConformidadeRepository ncs;

    @GetMapping("/planos")
    @PreAuthorize("hasAuthority('qualidade:leitura')")
    public List<PlanoInspecao> planos(@AuthenticationPrincipal AuthenticatedUser u) {
        return planos.findAllByEmpresaIdAndDeletedAtIsNullOrderByCodigo(u.getEmpresaId());
    }

    @PostMapping("/planos")
    @PreAuthorize("hasAuthority('qualidade:escrita')")
    public ResponseEntity<PlanoInspecao> criarPlano(@AuthenticationPrincipal AuthenticatedUser u,
                                                    @RequestBody PlanoInspecao p) {
        p.setId(null);
        p.setEmpresaId(u.getEmpresaId());
        p.setDeletedAt(null);
        p.setAtivo(true);
        if (p.getCodigo() == null || p.getCodigo().isBlank())
            throw new BusinessException("Codigo do plano e obrigatorio");
        if (p.getDescricao() == null || p.getDescricao().isBlank())
            throw new BusinessException("Descricao do plano e obrigatoria");
        if (p.getTipo() == null || p.getTipo().isBlank()) p.setTipo("RECEBIMENTO");
        return ResponseEntity.ok(planos.save(p));
    }

    @GetMapping("/inspecoes")
    @PreAuthorize("hasAuthority('qualidade:leitura')")
    public List<Inspecao> listarInspecoes(@AuthenticationPrincipal AuthenticatedUser u) {
        return inspecoes.findAllByEmpresaIdAndDeletedAtIsNullOrderByDataInspecaoDesc(u.getEmpresaId());
    }

    @PostMapping("/inspecoes")
    @PreAuthorize("hasAuthority('qualidade:escrita')")
    public ResponseEntity<Inspecao> criarInspecao(@AuthenticationPrincipal AuthenticatedUser u,
                                                  @RequestBody Inspecao i) {
        i.setId(null);
        i.setEmpresaId(u.getEmpresaId());
        i.setDeletedAt(null);
        if (i.getPlanoId() == null)
            throw new BusinessException("Plano de inspecao e obrigatorio");
        planos.findById(i.getPlanoId())
                .filter(x -> u.getEmpresaId().equals(x.getEmpresaId()) && x.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException("Plano de inspecao invalido para a empresa"));
        if (i.getReferenciaTipo() == null || i.getReferenciaTipo().isBlank())
            i.setReferenciaTipo("RECEBIMENTO");
        if (i.getNumero() == null || i.getNumero().isBlank())
            i.setNumero("INSP-" + LocalDate.now() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        if (i.getDataInspecao() == null) i.setDataInspecao(LocalDate.now());
        i.setStatus("ABERTA");
        i.setResultado(null);
        return ResponseEntity.ok(inspecoes.save(i));
    }

    @PostMapping("/inspecoes/{id}/concluir")
    @Transactional
    @PreAuthorize("hasAuthority('qualidade:escrita')")
    public ResponseEntity<Inspecao> concluirInspecao(@AuthenticationPrincipal AuthenticatedUser u,
                                                     @PathVariable Long id,
                                                     @Valid @RequestBody ConcluirInspecaoRequest req) {
        Inspecao i = inspecoes.findById(id)
                .filter(x -> u.getEmpresaId().equals(x.getEmpresaId()) && x.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Inspecao nao encontrada"));
        if (!"ABERTA".equals(i.getStatus()))
            throw new BusinessException("Somente inspecao ABERTA pode ser concluida");
        String resultado = req.resultado() == null ? "" : req.resultado().trim().toUpperCase();
        if (!"APROVADO".equals(resultado) && !"REPROVADO".equals(resultado))
            throw new BusinessException("Resultado deve ser APROVADO ou REPROVADO");
        i.setResultado(resultado);
        i.setStatus("CONCLUIDA");
        if (req.observacao() != null && !req.observacao().isBlank())
            i.setObservacao(req.observacao());
        inspecoes.save(i);

        boolean gerarNc = req.gerarNc() == null || Boolean.TRUE.equals(req.gerarNc());
        if ("REPROVADO".equals(resultado) && gerarNc) {
            NaoConformidade n = new NaoConformidade();
            n.setEmpresaId(u.getEmpresaId());
            n.setInspecaoId(i.getId());
            n.setNumero("NC-" + LocalDate.now() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
            n.setSeveridade(req.severidadeNc() != null && !req.severidadeNc().isBlank() ? req.severidadeNc() : "MEDIA");
            n.setStatus("ABERTA");
            n.setDescricao(req.descricaoNc() != null && !req.descricaoNc().isBlank()
                    ? req.descricaoNc()
                    : "NC gerada pela inspecao " + i.getNumero() + " (REPROVADO)");
            ncs.save(n);
        }
        return ResponseEntity.ok(i);
    }

    @GetMapping("/nao-conformidades")
    @PreAuthorize("hasAuthority('qualidade:leitura')")
    public List<NaoConformidade> listarNcs(@AuthenticationPrincipal AuthenticatedUser u) {
        return ncs.findAllByEmpresaIdAndDeletedAtIsNullOrderByPrazoAsc(u.getEmpresaId());
    }

    @PostMapping("/nao-conformidades")
    @PreAuthorize("hasAuthority('qualidade:escrita')")
    public ResponseEntity<NaoConformidade> criarNc(@AuthenticationPrincipal AuthenticatedUser u,
                                                   @RequestBody NaoConformidade n) {
        n.setId(null);
        n.setEmpresaId(u.getEmpresaId());
        n.setDeletedAt(null);
        if (n.getDescricao() == null || n.getDescricao().isBlank())
            throw new BusinessException("Descricao da nao conformidade e obrigatoria");
        if (n.getNumero() == null || n.getNumero().isBlank())
            n.setNumero("NC-" + LocalDate.now() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        if (n.getSeveridade() == null || n.getSeveridade().isBlank()) n.setSeveridade("MEDIA");
        n.setStatus("ABERTA");
        return ResponseEntity.ok(ncs.save(n));
    }

    @PostMapping("/nao-conformidades/{id}/acoes")
    @Transactional
    @PreAuthorize("hasAuthority('qualidade:escrita')")
    public ResponseEntity<NaoConformidade> registrarAcoes(@AuthenticationPrincipal AuthenticatedUser u,
                                                          @PathVariable Long id,
                                                          @RequestBody AcoesNcRequest req) {
        NaoConformidade n = ncs.findById(id)
                .filter(x -> u.getEmpresaId().equals(x.getEmpresaId()) && x.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Nao conformidade nao encontrada"));
        if ("ENCERRADA".equals(n.getStatus()))
            throw new BusinessException("NC ja encerrada");
        if (req.causaRaiz() != null) n.setCausaRaiz(req.causaRaiz());
        if (req.acaoCorretiva() != null) n.setAcaoCorretiva(req.acaoCorretiva());
        if (req.acaoPreventiva() != null) n.setAcaoPreventiva(req.acaoPreventiva());
        if (req.prazo() != null) n.setPrazo(req.prazo());
        if (!"EM_ACAO".equals(n.getStatus())) n.setStatus("EM_ACAO");
        return ResponseEntity.ok(ncs.save(n));
    }

    @PostMapping("/nao-conformidades/{id}/encerrar")
    @Transactional
    @PreAuthorize("hasAuthority('qualidade:escrita')")
    public ResponseEntity<NaoConformidade> encerrar(@AuthenticationPrincipal AuthenticatedUser u,
                                                    @PathVariable Long id) {
        NaoConformidade n = ncs.findById(id)
                .filter(x -> u.getEmpresaId().equals(x.getEmpresaId()) && x.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Nao conformidade nao encontrada"));
        if ("ENCERRADA".equals(n.getStatus()))
            throw new BusinessException("Nao conformidade ja esta encerrada");
        if (n.getAcaoCorretiva() == null || n.getAcaoCorretiva().isBlank())
            throw new BusinessException("Informe a acao corretiva antes de encerrar a NC");
        n.setStatus("ENCERRADA");
        n.setEncerradaEm(LocalDateTime.now());
        return ResponseEntity.ok(ncs.save(n));
    }

    public record ConcluirInspecaoRequest(
            @NotBlank String resultado,
            String observacao,
            Boolean gerarNc,
            String descricaoNc,
            String severidadeNc
    ) {}

    public record AcoesNcRequest(
            String causaRaiz,
            String acaoCorretiva,
            String acaoPreventiva,
            LocalDate prazo
    ) {}
}
