package br.com.brasil_saas.ativos.controller;

import br.com.brasil_saas.ativos.model.*;
import br.com.brasil_saas.ativos.service.AtivoContabilService;
import br.com.brasil_saas.ativos.service.AtivoContabilService.*;
import br.com.brasil_saas.ativos.repository.AtivoImobilizadoRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Contabilidade de ativos (FI-AA). */
@RestController
@RequestMapping("/api/ativos")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AtivosController {
    private final AtivoImobilizadoRepository ativos;
    private final AtivoContabilService svc;

    @GetMapping
    public List<AtivoImobilizado> listar(@AuthenticationPrincipal AuthenticatedUser u) {
        return ativos.findAllByEmpresaIdAndDeletedAtIsNullOrderByCodigo(u.getEmpresaId());
    }

    @PostMapping
    public ResponseEntity<AtivoImobilizado> criar(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody AtivoImobilizado a) {
        return ResponseEntity.ok(svc.criar(u.getEmpresaId(), u.getId(), a));
    }

    @GetMapping("/{id}")
    public AtivoImobilizado obter(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        return svc.ativo(u.getEmpresaId(), id);
    }

    @PutMapping("/{id}")
    public AtivoImobilizado atualizar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody AtivoImobilizado a) {
        return svc.atualizar(u.getEmpresaId(), id, a);
    }

    /** Sem corpo = baixa total sem venda (comportamento anterior). */
    @PostMapping("/{id}/baixar")
    public ResponseEntity<AtivoImobilizado> baixar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id,
                                                   @RequestBody(required = false) BaixaReq r) {
        svc.baixar(u.getEmpresaId(), u.getId(), id, r);
        return ResponseEntity.ok(svc.ativo(u.getEmpresaId(), id));
    }

    @PostMapping("/{id}/adicao")
    public AtivoMovimento adicao(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody AdicaoReq r) {
        return svc.adicao(u.getEmpresaId(), u.getId(), id, r);
    }

    @PostMapping("/{id}/transferir")
    public AtivoMovimento transferir(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody TransferenciaReq r) {
        return svc.transferir(u.getEmpresaId(), u.getId(), id, r);
    }

    @PostMapping("/{id}/reavaliar")
    public AtivoMovimento reavaliar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody ValorReq r) {
        return svc.reavaliar(u.getEmpresaId(), u.getId(), id, r);
    }

    @PostMapping("/{id}/impairment")
    public AtivoMovimento impairment(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody ValorReq r) {
        return svc.impairment(u.getEmpresaId(), u.getId(), id, r);
    }

    @GetMapping("/{id}/movimentos")
    public List<AtivoMovimento> movimentos(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        return svc.movimentos(u.getEmpresaId(), id);
    }

    @GetMapping("/{id}/depreciacao")
    public Map<String, Object> depreciacao(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        AtivoImobilizado a = svc.ativo(u.getEmpresaId(), id);
        LocalDate inicio = a.getDataInicioDepreciacao() != null ? a.getDataInicioDepreciacao() : a.getDataAquisicao();
        if (inicio == null) throw new BusinessException("Ativo sem data de aquisição");
        if (a.getVidaUtilMeses() == null || a.getVidaUtilMeses() <= 0) throw new BusinessException("Vida útil do ativo inválida");
        long decorridos = Math.max(0, Math.min(a.getVidaUtilMeses(),
                ChronoUnit.MONTHS.between(YearMonth.from(inicio), YearMonth.now()) + 1));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ativoId", a.getId());
        out.put("codigo", a.getCodigo());
        out.put("metodo", a.getMetodoDepreciacao() == null ? "LINEAR" : a.getMetodoDepreciacao());
        out.put("mesesDepreciaveis", a.getVidaUtilMeses());
        out.put("mesesDecorridos", decorridos);
        out.put("baseDepreciavel", AtivoContabilService.baseDepreciavel(a));
        out.put("depreciacaoMensal", AtivoContabilService.cotaDoPeriodo(a, YearMonth.now()));
        out.put("depreciacaoAcumuladaCalculada", a.getValorDepreciado());
        out.put("valorContabilCalculado", AtivoContabilService.valorContabil(a));
        out.put("ultimoPeriodoDepreciado", a.getUltimoPeriodoDepreciado());
        return out;
    }

    /** Deprecia o ativo ate o mes corrente. */
    @PostMapping("/{id}/depreciar")
    public AtivoImobilizado depreciar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        return svc.depreciarAteHoje(u.getEmpresaId(), u.getId(), id);
    }

    // ---------------------------------------------------------------- classes

    @GetMapping("/classes")
    public List<ClasseAtivo> classes(@AuthenticationPrincipal AuthenticatedUser u) {
        return svc.classes(u.getEmpresaId());
    }

    @PostMapping("/classes")
    public ClasseAtivo criarClasse(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody ClasseAtivo c) {
        return svc.salvarClasse(u.getEmpresaId(), null, c);
    }

    @PutMapping("/classes/{id}")
    public ClasseAtivo atualizarClasse(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody ClasseAtivo c) {
        return svc.salvarClasse(u.getEmpresaId(), id, c);
    }

    @DeleteMapping("/classes/{id}")
    public ResponseEntity<Void> excluirClasse(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        svc.excluirClasse(u.getEmpresaId(), id);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------- depreciacao mensal

    @GetMapping("/depreciacao/simular")
    public List<Map<String, Object>> simular(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam String periodo) {
        return svc.simular(u.getEmpresaId(), periodo(periodo));
    }

    @PostMapping("/depreciacao/executar")
    public DepreciacaoExecucao executar(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam String periodo) {
        return svc.executar(u.getEmpresaId(), u.getId(), periodo(periodo));
    }

    @GetMapping("/depreciacao/execucoes")
    public List<DepreciacaoExecucao> execucoes(@AuthenticationPrincipal AuthenticatedUser u) {
        return svc.execucoes(u.getEmpresaId());
    }

    @PostMapping("/depreciacao/execucoes/{id}/estornar")
    public DepreciacaoExecucao estornar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        return svc.estornar(u.getEmpresaId(), id);
    }

    // ------------------------------------------------------------- relatorios

    @GetMapping("/relatorios/posicao")
    public Map<String, Object> posicao(@AuthenticationPrincipal AuthenticatedUser u) {
        return svc.posicao(u.getEmpresaId());
    }

    @GetMapping("/relatorios/projecao")
    public List<Map<String, Object>> projecao(@AuthenticationPrincipal AuthenticatedUser u,
                                              @RequestParam(required = false) String inicio,
                                              @RequestParam(defaultValue = "12") int meses) {
        return svc.projecao(u.getEmpresaId(), inicio == null ? YearMonth.now() : periodo(inicio), meses);
    }

    private static YearMonth periodo(String p) {
        try {
            return YearMonth.parse(p);
        } catch (Exception e) {
            throw new BusinessException("Período inválido, use AAAA-MM");
        }
    }
}
