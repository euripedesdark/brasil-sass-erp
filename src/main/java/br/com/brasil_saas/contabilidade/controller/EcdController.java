package br.com.brasil_saas.contabilidade.controller;
import br.com.brasil_saas.contabilidade.service.EcdService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/contabilidade/ecd") @RequiredArgsConstructor
public class EcdController {
    private final EcdService svc;
    @PostMapping("/gerar") @PreAuthorize("hasAuthority('contabilidade:leitura')")
    public Map<String, Object> gerar(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam int exercicio, @RequestParam String cnpj, @RequestParam String nome, @RequestParam(required = false) String uf, @RequestParam(required = false) String codMun) { return svc.gerar(u.getEmpresaId(), exercicio, cnpj, nome, uf, codMun); }
}
