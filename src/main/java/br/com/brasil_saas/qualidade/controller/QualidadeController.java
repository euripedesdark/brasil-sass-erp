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
        return ResponseEntity.ok(ncs.save(n));
    }

    @PostMapping("/nao-conformidades/{id}/encerrar")
    @PreAuthorize("hasAuthority('qualidade:escrita')")
    public ResponseEntity<NaoConformidade> encerrar(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id) {
        NaoConformidade n=ncs.findById(id).orElseThrow();
        if (!u.getEmpresaId().equals(n.getEmpresaId())) return ResponseEntity.notFound().build();
        n.setStatus("ENCERRADA"); n.setEncerradaEm(LocalDateTime.now());
        return ResponseEntity.ok(ncs.save(n));
    }
}
