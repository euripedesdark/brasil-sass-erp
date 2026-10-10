package br.com.brasil_saas.compras.controller;

import br.com.brasil_saas.compras.dto.PedidoCompraRequest;
import br.com.brasil_saas.compras.dto.PedidoCompraResponse;
import br.com.brasil_saas.compras.service.PedidoCompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import br.com.brasil_saas.shared.security.AuthenticatedUser;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/compras/pedidos")
@RequiredArgsConstructor
public class PedidoCompraController {

    private final PedidoCompraService service;

    @PostMapping
    @PreAuthorize("hasAuthority('compras:pedido:escrita')")
    public ResponseEntity<PedidoCompraResponse> criar(@Valid @RequestBody PedidoCompraRequest request, @AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request.comEmpresaDa(u.getEmpresaId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('compras:pedido:leitura')")
    public ResponseEntity<PedidoCompraResponse> buscarPorId(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.ok(service.buscarPorId(id, u.getEmpresaId()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('compras:pedido:leitura')")
    public ResponseEntity<List<PedidoCompraResponse>> listarPorEmpresa(@AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.ok(service.listarPorEmpresa(u.getEmpresaId()));
    }

    @PostMapping("/{id}/receber")
    @PreAuthorize("hasAuthority('compras:pedido:escrita')")
    public ResponseEntity<Void> receber(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        service.receber(id, u.getEmpresaId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/receber-parcial")
    @PreAuthorize("hasAuthority('compras:pedido:escrita')")
    public ResponseEntity<Void> receberParcial(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u, @RequestBody Map<Long, BigDecimal> quantidades) {
        service.receberParcial(id, u.getEmpresaId(), quantidades);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('compras:pedido:escrita')")
    public ResponseEntity<Void> cancelar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        service.cancelar(id, u.getEmpresaId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/aprovar")
    @PreAuthorize("hasAuthority('compras:pedido:aprovar')")
    public ResponseEntity<PedidoCompraResponse> aprovar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.ok(service.aprovar(id, u.getEmpresaId(), u.getId()));
    }

    @PostMapping("/{id}/rejeitar")
    @PreAuthorize("hasAuthority('compras:pedido:aprovar')")
    public ResponseEntity<PedidoCompraResponse> rejeitar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.rejeitar(id, u.getEmpresaId(), u.getId(), body.get("motivo")));
    }
}
