package br.com.brasil_saas.producao.controller;

import br.com.brasil_saas.producao.service.CapacidadeService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/producao/capacidade")
@RequiredArgsConstructor
public class CapacidadeController {
    private final CapacidadeService service;

    @PostMapping("/simular")
    public Map<String, Object> simular(@AuthenticationPrincipal AuthenticatedUser u,
                                       @RequestBody CapacidadeService.Request request) {
        return service.simular(u.getEmpresaId(), request);
    }

    @PostMapping("/carga")
    public List<Map<String, Object>> carga(@AuthenticationPrincipal AuthenticatedUser u,
                                           @RequestBody List<CapacidadeService.Request> pedidos) {
        return service.cargaPorCentro(u.getEmpresaId(), pedidos);
    }
}
