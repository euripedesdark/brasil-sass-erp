package br.com.brasil_saas.producao.controller;

import br.com.brasil_saas.producao.service.*;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/producao/mrp")
@RequiredArgsConstructor
public class MrpController {
    private final MrpService service;

    @PostMapping("/simular")
    @PreAuthorize("hasAuthority('producao:mrp:leitura')")
    public List<Map<String, Object>> simular(@AuthenticationPrincipal AuthenticatedUser u,
                                             @RequestBody MrpRequest request) {
        return service.simular(u.getEmpresaId(), request);
    }

    /** Materializa as sugestoes: solicitacao de compra + ordens de producao. */
    @PostMapping("/gerar-sugestoes")
    @PreAuthorize("hasAuthority('producao:mrp:escrita')")
    public Map<String, Object> gerar(@AuthenticationPrincipal AuthenticatedUser u,
                                     @RequestBody MrpRequest request) {
        return service.gerarSugestoes(u.getEmpresaId(), u.getId(), request);
    }
}
