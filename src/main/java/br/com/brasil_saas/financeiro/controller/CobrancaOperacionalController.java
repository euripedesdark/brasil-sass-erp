package br.com.brasil_saas.financeiro.controller;

import br.com.brasil_saas.financeiro.model.CobrancaAcao;
import br.com.brasil_saas.financeiro.model.PromessaPagamento;
import br.com.brasil_saas.financeiro.service.CobrancaOperacionalService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/financeiro/cobranca-op")
@RequiredArgsConstructor
public class CobrancaOperacionalController {

    private final CobrancaOperacionalService svc;

    public record AcaoReq(Long tituloId, String tipo, Integer nivel, String observacao) {}
    public record PromessaReq(Long tituloId, BigDecimal valor, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataPrometida, String observacao) {}
    public record StatusReq(String status) {}

    @GetMapping("/carteira")
    @PreAuthorize("hasAuthority('financeiro:titulo:leitura')")
    public List<Map<String, Object>> carteira(@AuthenticationPrincipal AuthenticatedUser u) {
        return svc.carteira(u.getEmpresaId());
    }

    @PostMapping("/acoes")
    @PreAuthorize("hasAuthority('financeiro:titulo:escrita')")
    public ResponseEntity<CobrancaAcao> acao(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody AcaoReq body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(svc.registrarAcao(u.getEmpresaId(), body.tituloId(), body.tipo(), body.nivel(), body.observacao()));
    }

    @GetMapping("/acoes")
    @PreAuthorize("hasAuthority('financeiro:titulo:leitura')")
    public List<CobrancaAcao> histAcoes(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam Long tituloId) {
        return svc.historicoAcoes(u.getEmpresaId(), tituloId);
    }

    @PostMapping("/promessas")
    @PreAuthorize("hasAuthority('financeiro:titulo:escrita')")
    public ResponseEntity<PromessaPagamento> promessa(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody PromessaReq body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(svc.criarPromessa(u.getEmpresaId(), body.tituloId(), body.valor(), body.dataPrometida(), body.observacao()));
    }

    @GetMapping("/promessas")
    @PreAuthorize("hasAuthority('financeiro:titulo:leitura')")
    public List<PromessaPagamento> listarPromessas(@AuthenticationPrincipal AuthenticatedUser u,
                                                   @RequestParam(required = false) String status) {
        return svc.listarPromessas(u.getEmpresaId(), status);
    }

    @PostMapping("/promessas/{id}/status")
    @PreAuthorize("hasAuthority('financeiro:titulo:escrita')")
    public PromessaPagamento statusPromessa(@AuthenticationPrincipal AuthenticatedUser u,
                                            @PathVariable Long id, @RequestBody StatusReq body) {
        return svc.atualizarPromessa(u.getEmpresaId(), id, body.status());
    }

    @PostMapping("/promessas/processar-vencidas")
    @PreAuthorize("hasAuthority('financeiro:titulo:escrita')")
    public Map<String, Object> processarVencidas(@AuthenticationPrincipal AuthenticatedUser u) {
        return Map.of("quebradas", svc.processarPromessasVencidas(u.getEmpresaId()));
    }
}
