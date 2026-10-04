package br.com.brasil_saas.financeiro.controller;
import br.com.brasil_saas.financeiro.service.OfxService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;
@RestController @RequestMapping("/api/financeiro/extrato") @RequiredArgsConstructor
public class OfxController {
    private final OfxService svc;
    @PostMapping("/importar-ofx") @PreAuthorize("hasAuthority('financeiro:extrato:escrita')")
    public ResponseEntity<Map<String, Object>> importar(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam Long contaBancariaId, @RequestPart("arquivo") MultipartFile arquivo) throws IOException { return ResponseEntity.ok(svc.importar(u.getEmpresaId(), contaBancariaId, arquivo.getBytes())); }
}
