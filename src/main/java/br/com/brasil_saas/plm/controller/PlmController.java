package br.com.brasil_saas.plm.controller;

import br.com.brasil_saas.plm.model.*;
import br.com.brasil_saas.plm.service.PlmService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/plm") @RequiredArgsConstructor
public class PlmController {

    private final PlmService service;

    @GetMapping("/mudancas") @PreAuthorize("hasAuthority('plm:leitura')")
    public List<PlmMudanca> mudancas(@AuthenticationPrincipal AuthenticatedUser u) {
        return service.mudancas(u.getEmpresaId());
    }

    @PostMapping("/mudancas") @PreAuthorize("hasAuthority('plm:escrita')")
    public ResponseEntity<PlmMudanca> criar(
            @AuthenticationPrincipal AuthenticatedUser u,
            @Valid @RequestBody PlmService.MudancaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.criarMudanca(u.getEmpresaId(), u.getId(), request));
    }

    public record EnviarRequest(Long aprovadorId) {}

    @PostMapping("/mudancas/{id}/enviar-aprovacao") @PreAuthorize("hasAuthority('plm:escrita')")
    public PlmMudanca enviar(@AuthenticationPrincipal AuthenticatedUser u,
                             @PathVariable Long id,
                             @RequestBody(required = false) EnviarRequest request) {
        return service.enviarAprovacao(u.getEmpresaId(), u.getId(), id,
                request == null ? null : request.aprovadorId());
    }

    public record DecidirRequest(boolean aprovar, String observacao) {}

    @PostMapping("/mudancas/{id}/decidir") @PreAuthorize("hasAuthority('plm:aprovar')")
    public PlmMudanca decidir(@AuthenticationPrincipal AuthenticatedUser u,
                              @PathVariable Long id,
                              @Valid @RequestBody DecidirRequest request) {
        return service.decidirEtapa(u.getEmpresaId(), u.getId(), id,
                request.aprovar(), request.observacao());
    }

    @PostMapping("/mudancas/{id}/implementar") @PreAuthorize("hasAuthority('plm:escrita')")
    public PlmMudanca implementar(@AuthenticationPrincipal AuthenticatedUser u,
                                  @PathVariable Long id) {
        return service.implementar(u.getEmpresaId(), u.getId(), id);
    }

    @GetMapping("/mudancas/{id}/efeitos") @PreAuthorize("hasAuthority('plm:leitura')")
    public List<PlmEfeito> efeitos(@AuthenticationPrincipal AuthenticatedUser u,
                                   @PathVariable Long id) {
        return service.efeitos(u.getEmpresaId(), id);
    }

    @PostMapping("/efeitos") @PreAuthorize("hasAuthority('plm:escrita')")
    public ResponseEntity<PlmEfeito> adicionarEfeito(
            @AuthenticationPrincipal AuthenticatedUser u,
            @Valid @RequestBody PlmService.EfeitoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.adicionarEfeito(u.getEmpresaId(), request));
    }

    @GetMapping("/revisoes") @PreAuthorize("hasAuthority('plm:leitura')")
    public List<PlmRevisao> revisoes(@AuthenticationPrincipal AuthenticatedUser u,
                                     @RequestParam Long produtoId) {
        return service.revisoes(u.getEmpresaId(), produtoId);
    }

    @PostMapping("/revisoes") @PreAuthorize("hasAuthority('plm:escrita')")
    public ResponseEntity<PlmRevisao> criarRevisao(
            @AuthenticationPrincipal AuthenticatedUser u,
            @Valid @RequestBody PlmService.RevisaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.criarRevisao(u.getEmpresaId(), u.getId(), request));
    }

    @PostMapping("/revisoes/{id}/vigorar") @PreAuthorize("hasAuthority('plm:escrita')")
    public PlmRevisao vigorar(@AuthenticationPrincipal AuthenticatedUser u,
                              @PathVariable Long id) {
        return service.vigorarRevisao(u.getEmpresaId(), u.getId(), id);
    }
}
