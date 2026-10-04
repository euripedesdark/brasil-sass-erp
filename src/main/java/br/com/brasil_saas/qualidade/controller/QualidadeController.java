package br.com.brasil_saas.qualidade.controller;

import br.com.brasil_saas.qualidade.model.*;
import br.com.brasil_saas.qualidade.repository.*;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.time.LocalDateTime;
import java.util.List;

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
    public ResponseEntity<PlanoInspecao> criarPlano(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody PlanoInspecao p) {
        p.setId(null); p.setEmpresaId(u.getEmpresaId()); p.setDeletedAt(null); p.setAtivo(true);
        if (p.getCodigo() == null || p.getCodigo().isBlank()) throw new IllegalArgumentException("Código do plano é obrigatório");
        if (p.getDescricao() == null || p.getDescricao().isBlank()) throw new IllegalArgumentException("Descrição do plano é obrigatória");
        return ResponseEntity.ok(planos.save(p));
    }

    @GetMapping("/inspecoes")
    @PreAuthorize("hasAuthority('qualidade:leitura')")
    public List<Inspecao> inspecoes(@AuthenticationPrincipal AuthenticatedUser u) {
        return inspecoes.findAllByEmpresaIdAndDeletedAtIsNullOrderByDataInspecaoDesc(u.getEmpresaId());
    }

    @PostMapping("/inspecoes")
    @PreAuthorize("hasAuthority('qualidade:escrita')")
    public ResponseEntity<Inspecao> criarInspecao(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody Inspecao i) {
        i.setId(null); i.setEmpresaId(u.getEmpresaId()); i.setDeletedAt(null);
        if (i.getPlanoInspecaoId() == null) throw new IllegalArgumentException("Plano de inspeção é obrigatório");
        if (planos.findById(i.getPlanoInspecaoId()).filter(x -> u.getEmpresaId().equals(x.getEmpresaId()) && x.getDeletedAt() == null).isEmpty()) throw new IllegalArgumentException("Plano de inspeção inválido para a empresa");
        if (i.getStatus()==null) i.setStatus("ABERTA");
        return ResponseEntity.ok(inspecoes.save(i));
    }

    @GetMapping("/nao-conformidades")
    @PreAuthorize("hasAuthority('qualidade:leitura')")
    public List<NaoConformidade> ncs(@AuthenticationPrincipal AuthenticatedUser u) {
        return ncs.findAllByEmpresaIdAndDeletedAtIsNullOrderByPrazoAsc(u.getEmpresaId());
    }

    @PostMapping("/nao-conformidades")
    @PreAuthorize("hasAuthority('qualidade:escrita')")
    public ResponseEntity<NaoConformidade> criarNc(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody NaoConformidade n) {
        n.setId(null); n.setEmpresaId(u.getEmpresaId()); n.setDeletedAt(null);
        if (n.getDescricao() == null || n.getDescricao().isBlank()) throw new IllegalArgumentException("Descrição da não conformidade é obrigatória");
        return ResponseEntity.ok(ncs.save(n));
    }

    @PostMapping("/nao-conformidades/{id}/encerrar")
    @PreAuthorize("hasAuthority('qualidade:escrita')")
    public ResponseEntity<NaoConformidade> encerrar(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id) {
        NaoConformidade n=ncs.findById(id).orElseThrow();
        if (!u.getEmpresaId().equals(n.getEmpresaId())) return ResponseEntity.notFound().build();
        if ("ENCERRADA".equals(n.getStatus())) throw new IllegalArgumentException("Não conformidade já está encerrada");
        n.setStatus("ENCERRADA"); n.setEncerradaEm(LocalDateTime.now());
        return ResponseEntity.ok(ncs.save(n));
    }
}
