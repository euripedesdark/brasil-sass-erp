package br.com.brasil_saas.vendas.controller;

import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.vendas.dto.PedidoVendaRequest;
import br.com.brasil_saas.vendas.dto.PedidoVendaResponse;
import br.com.brasil_saas.vendas.service.PedidoVendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendas/pedidos")
@RequiredArgsConstructor
public class PedidoVendaController {

    private final PedidoVendaService service;

    /**
     * O tenant vem do token, nunca do cliente.
     *
     * Antes a listagem aceitava {@code ?empresaId=} e as demais rotas nem
     * olhavam a empresa: bastava trocar o id na URL para ler, confirmar,
     * faturar ou cancelar pedido de outra empresa. Numainstallacao com mais de
     * uma empresa isso e vazamento de dados entre elas — e o service tambem nao
     * conferia, porque recebia so o id.
     *
     * O mesmo padrao ja valia no RelatorioController.
     */
    private Long empresaDoToken(AuthenticatedUser user) {
        if (user == null || user.getEmpresaId() == null) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Usuario sem empresa definida no token");
        }
        return user.getEmpresaId();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('vendas:pedido:escrita')")
    public ResponseEntity<PedidoVendaResponse> criar(
            @Valid @RequestBody PedidoVendaRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        // A empresa do corpo e sobrescrita pela do token: aceitar a do cliente
        // permitiria criar pedido em nome de outra empresa.
        PedidoVendaRequest seguro = request.comEmpresaDa(empresaDoToken(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(seguro));
    }

    @GetMapping("/clientes/{clienteId}/credito")
    @PreAuthorize("hasAuthority('vendas:pedido:leitura')")
    public Map<String, Object> credito(@PathVariable Long clienteId, @AuthenticationPrincipal AuthenticatedUser user) { return service.credito(empresaDoToken(user), clienteId); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('vendas:pedido:leitura')")
    public ResponseEntity<PedidoVendaResponse> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(service.buscarPorId(id, empresaDoToken(user)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('vendas:pedido:leitura')")
    public ResponseEntity<List<PedidoVendaResponse>> listarPorEmpresa(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(service.listarPorEmpresa(empresaDoToken(user)));
    }

    @PostMapping("/{id}/confirmar")
    @PreAuthorize("hasAuthority('vendas:pedido:escrita')")
    public ResponseEntity<Void> confirmar(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        service.confirmar(id, empresaDoToken(user));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/faturar")
    @PreAuthorize("hasAuthority('vendas:pedido:escrita')")
    public ResponseEntity<Void> faturar(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false, defaultValue = "false") boolean forcar) {
        service.faturar(id, empresaDoToken(user), forcar);
        return ResponseEntity.ok().build();
    }

        public record PosVendaReq(String motivo, String equipamento) {}
    @PostMapping("/{id}/posvenda")
    @PreAuthorize("hasAuthority('vendas:pedido:escrita')")
    public ResponseEntity<Map<String, Object>> posvenda(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user, @RequestBody PosVendaReq r) {
        return ResponseEntity.ok(service.abrirPosVenda(id, empresaDoToken(user), user.getId(), r.motivo(), r.equipamento()));
    }
@PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('vendas:pedido:escrita')")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        service.cancelar(id, empresaDoToken(user));
        return ResponseEntity.ok().build();
    }
}
