package br.com.brasil_saas.producao.controller;
import br.com.brasil_saas.producao.service.OeeService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
@RestController @RequestMapping("/api/producao/oee") @RequiredArgsConstructor
public class OeeController {
    private final OeeService svc;
    @GetMapping @PreAuthorize("hasAuthority('producao:oee:leitura')")
    public List<Map<String, Object>> eficiencia(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) { return svc.eficiencia(u.getEmpresaId(), de, ate); }
}
