package br.com.brasil_saas.ativos.controller;

import br.com.brasil_saas.ativos.model.*;
import br.com.brasil_saas.ativos.service.ManutencaoService;
import br.com.brasil_saas.ativos.service.ManutencaoService.ConclusaoReq;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Manutencao (PM): ordens, notas, planos preventivos e medicoes. */
@RestController
@RequestMapping("/api/ativos")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ManutencaoController {
    private final ManutencaoService svc;

    public record MotivoReq(String motivo) {}

    // ----------------------------------------------------------------- ordens

    @GetMapping("/manutencoes")
    public List<Manutencao> ordens(@AuthenticationPrincipal AuthenticatedUser u) {
        return svc.ordens(u.getEmpresaId());
    }

    @PostMapping("/manutencoes")
    public ResponseEntity<Manutencao> criar(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody Manutencao m) {
        return ResponseEntity.ok(svc.criarOrdem(u.getEmpresaId(), m));
    }

    @GetMapping("/manutencoes/{id}")
    public Manutencao ordem(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        return svc.ordem(u.getEmpresaId(), id);
    }

    @PostMapping("/manutencoes/{id}/liberar")
    public Manutencao liberar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        return svc.liberar(u.getEmpresaId(), id);
    }

    @PostMapping("/manutencoes/{id}/iniciar")
    public Manutencao iniciar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        return svc.iniciar(u.getEmpresaId(), id);
    }

    @PostMapping("/manutencoes/{id}/concluir")
    public ResponseEntity<Manutencao> concluir(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id,
                                               @RequestBody(required = false) ConclusaoReq r) {
        return ResponseEntity.ok(svc.concluir(u.getEmpresaId(), id, r));
    }

    @PostMapping("/manutencoes/{id}/cancelar")
    public Manutencao cancelar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id,
                               @RequestBody(required = false) MotivoReq r) {
        return svc.cancelar(u.getEmpresaId(), id, r == null ? null : r.motivo());
    }

    @GetMapping("/manutencoes/{id}/materiais")
    public List<ManutencaoMaterial> materiais(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        return svc.materiais(u.getEmpresaId(), id);
    }

    @PostMapping("/manutencoes/{id}/materiais")
    public ManutencaoMaterial adicionarMaterial(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody ManutencaoMaterial m) {
        return svc.adicionarMaterial(u.getEmpresaId(), id, m);
    }

    @DeleteMapping("/manutencoes/{id}/materiais/{materialId}")
    public ResponseEntity<Void> removerMaterial(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @PathVariable Long materialId) {
        svc.removerMaterial(u.getEmpresaId(), id, materialId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/manutencoes/{id}/apontamentos")
    public List<ManutencaoApontamento> apontamentos(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        return svc.apontamentos(u.getEmpresaId(), id);
    }

    @PostMapping("/manutencoes/{id}/apontamentos")
    public ManutencaoApontamento apontar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody ManutencaoApontamento a) {
        return svc.apontar(u.getEmpresaId(), id, a);
    }

    @DeleteMapping("/manutencoes/{id}/apontamentos/{apontamentoId}")
    public ResponseEntity<Void> removerApontamento(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @PathVariable Long apontamentoId) {
        svc.removerApontamento(u.getEmpresaId(), id, apontamentoId);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------ notas

    @GetMapping("/notas")
    public List<NotaManutencao> notas(@AuthenticationPrincipal AuthenticatedUser u) {
        return svc.notas(u.getEmpresaId());
    }

    @PostMapping("/notas")
    public NotaManutencao criarNota(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody NotaManutencao n) {
        return svc.criarNota(u.getEmpresaId(), u.getId(), n);
    }

    @PostMapping("/notas/{id}/gerar-ordem")
    public Manutencao gerarOrdem(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        return svc.gerarOrdemDaNota(u.getEmpresaId(), id);
    }

    @PostMapping("/notas/{id}/encerrar")
    public NotaManutencao encerrarNota(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id,
                                       @RequestBody(required = false) MotivoReq r) {
        return svc.encerrarNota(u.getEmpresaId(), id, r == null ? null : r.motivo());
    }

    // ----------------------------------------------------------------- planos

    @GetMapping("/planos")
    public List<PlanoManutencao> planos(@AuthenticationPrincipal AuthenticatedUser u) {
        return svc.planos(u.getEmpresaId());
    }

    @PostMapping("/planos")
    public PlanoManutencao criarPlano(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody PlanoManutencao p) {
        return svc.salvarPlano(u.getEmpresaId(), null, p);
    }

    @PutMapping("/planos/{id}")
    public PlanoManutencao atualizarPlano(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody PlanoManutencao p) {
        return svc.salvarPlano(u.getEmpresaId(), id, p);
    }

    @DeleteMapping("/planos/{id}")
    public ResponseEntity<Void> excluirPlano(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        svc.excluirPlano(u.getEmpresaId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/planos/vencidos")
    public List<PlanoManutencao> vencidos(@AuthenticationPrincipal AuthenticatedUser u,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return svc.planosVencidos(u.getEmpresaId(), data == null ? LocalDate.now() : data);
    }

    @PostMapping("/planos/gerar-ordens")
    public List<Manutencao> gerarOrdens(@AuthenticationPrincipal AuthenticatedUser u,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return svc.gerarOrdensDosPlanos(u.getEmpresaId(), data == null ? LocalDate.now() : data);
    }

    // --------------------------------------------------- medicoes e historico

    @GetMapping("/medicoes")
    public List<MedicaoAtivo> medicoes(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam(required = false) Long ativoId) {
        return svc.medicoes(u.getEmpresaId(), ativoId);
    }

    @PostMapping("/medicoes")
    public MedicaoAtivo registrarMedicao(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody MedicaoAtivo m) {
        return svc.registrarMedicao(u.getEmpresaId(), m);
    }

    @GetMapping("/{id}/historico-manutencao")
    public Map<String, Object> historico(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        return svc.historico(u.getEmpresaId(), id);
    }
}
