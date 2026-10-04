package br.com.brasil_saas.wms.controller;
import br.com.brasil_saas.wms.model.*;
import br.com.brasil_saas.wms.service.WmsService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
@RestController @RequestMapping("/api/wms") @RequiredArgsConstructor
public class WmsController {
    private final WmsService svc;
    public record SepararReq(BigDecimal quantidade, Long enderecoId) {}
    public record FinalizarReq(String codigoRastreio) {}
    @GetMapping("/ondas") @PreAuthorize("hasAuthority('wms:leitura')")
    public List<WmsOnda> ondas(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam(required = false) String status) { return svc.ondas(u.getEmpresaId(), status); }
    @PostMapping("/ondas/gerar-de-reservas") @PreAuthorize("hasAuthority('wms:escrita')")
    public Map<String, Object> gerarDeReservas(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam Long depositoId) { return svc.gerarOndaDeReservas(u.getEmpresaId(), depositoId); }
        @PostMapping("/ondas") @PreAuthorize("hasAuthority('wms:escrita')")
    public ResponseEntity<WmsOnda> criar(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody WmsOnda o) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.criarOnda(u.getEmpresaId(), o)); }
    @PostMapping("/ondas/{id}/itens") @PreAuthorize("hasAuthority('wms:escrita')")
    public ResponseEntity<WmsOndaItem> addItem(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody WmsOndaItem i) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.addItem(u.getEmpresaId(), id, i)); }
    @GetMapping("/ondas/{id}/itens") @PreAuthorize("hasAuthority('wms:leitura')")
    public List<WmsOndaItem> itens(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { return svc.itens(u.getEmpresaId(), id); }
    @PostMapping("/ondas/{id}/liberar") @PreAuthorize("hasAuthority('wms:escrita')")
    public WmsOnda liberar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { return svc.liberar(u.getEmpresaId(), id); }
    @PostMapping("/ondas/{oid}/itens/{iid}/separar") @PreAuthorize("hasAuthority('wms:escrita')")
    public WmsOndaItem separar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long oid, @PathVariable Long iid, @RequestBody SepararReq r) { return svc.separar(u.getEmpresaId(), oid, iid, r.quantidade(), r.enderecoId()); }
    @PostMapping("/ondas/{id}/concluir") @PreAuthorize("hasAuthority('wms:escrita')")
    public WmsOnda concluir(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { return svc.concluir(u.getEmpresaId(), id); }
    @GetMapping("/putaway") @PreAuthorize("hasAuthority('wms:leitura')")
    public Map<String, Object> putaway(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam Long depositoId, @RequestParam Long produtoId) { return svc.putaway(u.getEmpresaId(), depositoId, produtoId); }
    @GetMapping("/volumes") @PreAuthorize("hasAuthority('wms:leitura')")
    public List<WmsVolume> volumes(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam Long expedicaoId) { return svc.volumes(u.getEmpresaId(), expedicaoId); }
    @PostMapping("/volumes") @PreAuthorize("hasAuthority('wms:escrita')")
    public ResponseEntity<WmsVolume> criarVolume(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody WmsVolume v) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.criarVolume(u.getEmpresaId(), v)); }
    @PostMapping("/volumes/{id}/itens") @PreAuthorize("hasAuthority('wms:escrita')")
    public ResponseEntity<WmsVolumeItem> embalar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody WmsVolumeItem i) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.embalar(u.getEmpresaId(), id, i)); }
    @PostMapping("/volumes/{id}/fechar") @PreAuthorize("hasAuthority('wms:escrita')")
    public WmsVolume fechar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { return svc.fecharVolume(u.getEmpresaId(), id); }
    @GetMapping("/expedicoes/{id}/conferencia") @PreAuthorize("hasAuthority('wms:leitura')")
    public Map<String, Object> conferir(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { return svc.conferir(u.getEmpresaId(), id); }
    @PostMapping("/expedicoes/{id}/finalizar") @PreAuthorize("hasAuthority('wms:escrita')")
    public Map<String, Object> finalizar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody(required = false) FinalizarReq r) { return svc.finalizarExpedicao(u.getEmpresaId(), id, r == null ? null : r.codigoRastreio()); }
}
