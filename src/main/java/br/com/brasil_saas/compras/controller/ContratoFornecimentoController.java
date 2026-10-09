package br.com.brasil_saas.compras.controller;

import br.com.brasil_saas.compras.dto.*;
import br.com.brasil_saas.compras.service.ContratoFornecimentoService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras/contratos")
@RequiredArgsConstructor
public class ContratoFornecimentoController {

    private final ContratoFornecimentoService service;

    @PostMapping
    @PreAuthorize("hasAuthority('compras:pedido:escrita')")
    public ResponseEntity<ContratoFornecimentoResponse> criar(@Valid @RequestBody ContratoFornecimentoRequest r, @AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(r.comEmpresaDa(u.getEmpresaId())));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('compras:pedido:leitura')")
    public List<ContratoFornecimentoResponse> listar(@AuthenticationPrincipal AuthenticatedUser u) {
        return service.listar(u.getEmpresaId());
    }

    @GetMapping("/a-vencer")
    @PreAuthorize("hasAuthority('compras:pedido:leitura')")
    public List<ContratoFornecimentoResponse> aVencer(@RequestParam(defaultValue = "30") int dias, @AuthenticationPrincipal AuthenticatedUser u) {
        return service.aVencer(u.getEmpresaId(), dias);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('compras:pedido:leitura')")
    public ContratoFornecimentoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        return service.buscar(id, u.getEmpresaId());
    }

    @PostMapping("/{id}/ativar")
    @PreAuthorize("hasAuthority('compras:pedido:escrita')")
    public ContratoFornecimentoResponse ativar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        return service.ativar(id, u.getEmpresaId());
    }

    @PostMapping("/{id}/encerrar")
    @PreAuthorize("hasAuthority('compras:pedido:escrita')")
    public ContratoFornecimentoResponse encerrar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        return service.encerrar(id, u.getEmpresaId());
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('compras:pedido:escrita')")
    public ContratoFornecimentoResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        return service.cancelar(id, u.getEmpresaId());
    }

    @PostMapping("/{id}/liberar")
    @PreAuthorize("hasAuthority('compras:pedido:escrita')")
    public ResponseEntity<PedidoCompraResponse> liberar(@PathVariable Long id, @Valid @RequestBody ContratoLiberacaoRequest r, @AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.liberar(id, u.getEmpresaId(), u.getId(), r));
    }
}
