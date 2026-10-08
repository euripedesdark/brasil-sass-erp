package br.com.brasil_saas.financeiro.controller;

import br.com.brasil_saas.financeiro.dto.CaixaDtos.CaixaRequest;
import br.com.brasil_saas.financeiro.dto.CaixaDtos.CaixaResponse;
import br.com.brasil_saas.financeiro.dto.CaixaDtos.MovimentoRequest;
import br.com.brasil_saas.financeiro.dto.CaixaDtos.MovimentoResponse;
import br.com.brasil_saas.financeiro.model.Caixa;
import br.com.brasil_saas.financeiro.model.MovimentoCaixa;
import br.com.brasil_saas.financeiro.service.CaixaService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/financeiro/caixas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GESTOR','GERENTE','DIRETORIA','ADMIN','SUPERADMIN','SUPERUSER')")
public class CaixaController {

    private final CaixaService service;

    @GetMapping
    public ResponseEntity<List<CaixaResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String busca,
            @AuthenticationPrincipal AuthenticatedUser user) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 200), Sort.by("nome"));
        Page<Caixa> resultado = service.listar(empresa(user), pageable, busca);
        return ResponseEntity.ok(resultado.getContent().stream().map(CaixaController::para).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaixaResponse> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(para(service.buscarPorId(id, empresa(user))));
    }

    @PostMapping
    public ResponseEntity<CaixaResponse> criar(
            @Valid @RequestBody CaixaRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(para(service.criar(request, empresa(user))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CaixaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CaixaRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(para(service.atualizar(id, request, empresa(user))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        service.desativar(id, empresa(user));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/movimentos")
    public ResponseEntity<MovimentoResponse> movimentar(
            @PathVariable Long id,
            @Valid @RequestBody MovimentoRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paraMov(service.movimentar(empresa(user), id, request)));
    }

    @GetMapping("/{id}/movimentos")
    public ResponseEntity<List<MovimentoResponse>> listarMovimentos(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(service.listarMovimentos(empresa(user), id).stream()
                .map(CaixaController::paraMov).toList());
    }

    private static Long empresa(AuthenticatedUser user) {
        if (user == null || user.getEmpresaId() == null) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Usuario sem empresa definida no token");
        }
        return user.getEmpresaId();
    }

    private static CaixaResponse para(Caixa c) {
        return new CaixaResponse(c.getId(), c.getNome(), c.getSaldo(), c.getStatus(), c.isAtivo());
    }

    private static MovimentoResponse paraMov(MovimentoCaixa m) {
        return new MovimentoResponse(
                m.getId(), m.getCaixaId(), m.getTipo(), m.getValor(),
                m.getSaldoAnterior(), m.getSaldoPosterior(), m.getObservacao(), m.getCreatedAt());
    }
}
