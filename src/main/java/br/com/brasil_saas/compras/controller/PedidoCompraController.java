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
    public ResponseEntity<PedidoCompraResponse> criar(@Valid @RequestBody PedidoCompraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('compras:pedido:leitura')")
    public ResponseEntity<PedidoCompraResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('compras:pedido:leitura')")
    public ResponseEntity<List<PedidoCompraResponse>> listarPorEmpresa(@AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.ok(service.listarPorEmpresa(u.getEmpresaId()));
    }

    @PostMapping("/{id}/receber")
    @PreAuthorize("hasAuthority('compras:pedido:escrita')")
    public ResponseEntity<Void> receber(@PathVariable Long id) {
        service.receber(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/receber-parcial")
    @PreAuthorize("hasAuthority('compras:pedido:escrita')")
    public ResponseEntity<Void> receberParcial(@PathVariable Long id, @RequestBody Map<Long, BigDecimal> quantidades) {
        service.receberParcial(id, quantidades);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('compras:pedido:escrita')")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        service.cancelar(id);
        return ResponseEntity.ok().build();
    }
}
