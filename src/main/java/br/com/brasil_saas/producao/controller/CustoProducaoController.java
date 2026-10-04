package br.com.brasil_saas.producao.controller;

import br.com.brasil_saas.producao.service.CustoProducaoService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/producao")
@RequiredArgsConstructor
public class CustoProducaoController {
    private final CustoProducaoService service;

    @GetMapping("/{id}/custo")
    public CustoProducaoService.Resultado custo(@AuthenticationPrincipal AuthenticatedUser u,
                                                 @PathVariable Long id) {
        return service.calcular(u.getEmpresaId(), id);
    }
}
