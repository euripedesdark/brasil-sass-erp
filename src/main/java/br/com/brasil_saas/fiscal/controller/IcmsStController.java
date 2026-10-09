package br.com.brasil_saas.fiscal.controller;

import br.com.brasil_saas.fiscal.service.IcmsStService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/fiscal/icms-st")
@RequiredArgsConstructor
public class IcmsStController {

    private final IcmsStService service;

    public record Req(@NotNull BigDecimal baseOperacao, @NotNull BigDecimal aliqInterestadual,
                      @NotNull BigDecimal aliqInterna, @NotNull BigDecimal mva, BigDecimal reducaoBasePct) {}

    @PostMapping("/calcular")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> calcular(@RequestBody Req r) {
        return service.calcular(r.baseOperacao(), r.aliqInterestadual(), r.aliqInterna(), r.mva(), r.reducaoBasePct());
    }
}
