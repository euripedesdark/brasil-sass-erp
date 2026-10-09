package br.com.brasil_saas.fiscal.controller;

import br.com.brasil_saas.fiscal.service.DifalService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/fiscal/difal")
@RequiredArgsConstructor
public class DifalController {

    private final DifalService service;

    public record Req(@NotNull BigDecimal base, @NotNull BigDecimal aliqInterestadual,
                      @NotNull BigDecimal aliqInternaDestino, BigDecimal aliqFcp) {}

    @PostMapping("/calcular")
    @PreAuthorize("hasAuthority('fiscal:leitura') or hasAuthority('fiscal:escrita') or isAuthenticated()")
    public Map<String, Object> calcular(@RequestBody Req r) {
        return service.calcular(r.base(), r.aliqInterestadual(), r.aliqInternaDestino(), r.aliqFcp());
    }
}
