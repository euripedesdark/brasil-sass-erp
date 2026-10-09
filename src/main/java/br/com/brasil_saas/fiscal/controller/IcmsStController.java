package br.com.brasil_saas.fiscal.controller;

import br.com.brasil_saas.fiscal.service.IcmsStService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
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

    public record Req(@NotNull @DecimalMin("0") BigDecimal baseOperacao, @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal aliqInterestadual,
                      @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal aliqInterna, @NotNull @DecimalMin("0") BigDecimal mva, @DecimalMin("0") @DecimalMax("100") BigDecimal reducaoBasePct) {}

    @PostMapping("/calcular")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> calcular(@Valid @RequestBody Req r) {
        return service.calcular(r.baseOperacao(), r.aliqInterestadual(), r.aliqInterna(), r.mva(), r.reducaoBasePct());
    }
}
