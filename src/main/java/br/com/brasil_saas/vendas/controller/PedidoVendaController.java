package br.com.brasil_saas.vendas.controller;

import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.vendas.dto.PedidoVendaRequest;
import br.com.brasil_saas.vendas.dto.PedidoVendaResponse;
import br.com.brasil_saas.vendas.service.AtpService;
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
    private final AtpService atpService;
    private final br.com.brasil_saas.vendas.service.PedidoTributacaoService tributacaoService;

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
        PedidoVendaRequest seguro = request.comEmpresaDa(empresaDoToken(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(seguro));
    }

    @GetMapping("/clientes/{clienteId}/credito")
    @PreAuthorize("hasAuthority('vendas:pedido:leitura')")
    public Map<String, Object> credito(@PathVariable Long clienteId, @AuthenticationPrincipal AuthenticatedUser user) {
        return service.credito(empresaDoToken(user), clienteId);
    }

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

    @GetMapping("/{id}/atp")
    @PreAuthorize("hasAuthority('vendas:pedido:leitura') or hasAuthority('vendas:pedido:escrita')")
    public Map<String, Object> atp(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return atpService.verificar(empresaDoToken(user), id);
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
    public ResponseEntity<Map<String, Object>> posvenda(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody PosVendaReq r) {
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

    /** Prévia de impostos (NCM/CFOP do produto + DIFAL/ST se aplicável). Não grava documento. */
    @PostMapping("/tributacao/prever")
    @PreAuthorize("hasAuthority('vendas:pedido:leitura') or hasAuthority('vendas:pedido:escrita')")
    public Map<String, Object> preverTributacao(
            @RequestBody PedidoVendaRequest request,
            @RequestParam(required = false) String ufDestino,
            @RequestParam(required = false, defaultValue = "true") Boolean consumidorFinal,
            @RequestParam(required = false, defaultValue = "false") Boolean contribuinte,
            @AuthenticationPrincipal AuthenticatedUser user) {
        Long empresaId = empresaDoToken(user);
        return tributacaoService.prever(empresaId, request.comEmpresaDa(empresaId),
                ufDestino, consumidorFinal, contribuinte);
    }

}
