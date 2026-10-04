package br.com.brasil_saas.financeiro.controller;
import br.com.brasil_saas.financeiro.model.Renegociacao;
import br.com.brasil_saas.financeiro.service.RenegociacaoService;
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
@RestController @RequestMapping("/api/financeiro/renegociacoes") @RequiredArgsConstructor
public class RenegociacaoController {
    private final RenegociacaoService svc;
    public record RenegociarReq(Long tituloId, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate novoVencimento, BigDecimal acrescimo, String observacao) {}
    @GetMapping @PreAuthorize("hasAuthority('financeiro:titulo:leitura')")
    public List<Renegociacao> listar(@AuthenticationPrincipal AuthenticatedUser u) { return svc.listar(u.getEmpresaId()); }
    @PostMapping("/renegociar") @PreAuthorize("hasAuthority('financeiro:titulo:escrita')")
    public ResponseEntity<Renegociacao> renegociar(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody RenegociarReq r) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.renegociar(u.getEmpresaId(), r.tituloId(), r.novoVencimento(), r.acrescimo(), r.observacao())); }
}
