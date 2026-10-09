package br.com.brasil_saas.fiscal.controller;

import br.com.brasil_saas.fiscal.service.TributacaoSimuladorService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fiscal/tributacao")
@RequiredArgsConstructor
public class TributacaoController {

    private final TributacaoSimuladorService service;

    public record SimReq(@NotNull BigDecimal base, String ncm, String cfop,
                         String ufOrigem, String ufDestino,
                         Boolean consumidorFinal, Boolean contribuinte) {}

    public record SimLoteReq(String ufOrigem, String ufDestino, Boolean consumidorFinal, Boolean contribuinte,
                             List<Map<String, Object>> itens) {}

    @PostMapping("/simular")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> simular(@RequestBody SimReq r, @AuthenticationPrincipal AuthenticatedUser u) {
        return service.simular(u.getEmpresaId(), r.base(), r.ncm(), r.cfop(),
                r.ufOrigem(), r.ufDestino(), r.consumidorFinal(), r.contribuinte());
    }

    @PostMapping("/simular-itens")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> simularItens(@RequestBody SimLoteReq r, @AuthenticationPrincipal AuthenticatedUser u) {
        return service.simularItens(u.getEmpresaId(), r.ufOrigem(), r.ufDestino(),
                r.consumidorFinal(), r.contribuinte(), r.itens());
    }
}
