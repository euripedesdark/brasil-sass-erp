package br.com.brasil_saas.ativos.controller;
import br.com.brasil_saas.ativos.service.IndicadoresService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController @RequestMapping("/api/ativos/indicadores") @RequiredArgsConstructor
public class IndicadoresController {
    private final IndicadoresService svc;
    @GetMapping
    public List<Map<String, Object>> indicadores(@AuthenticationPrincipal AuthenticatedUser u) { return svc.indicadores(u.getEmpresaId()); }
}
