/* src/main/java/com/sysfluxo/web/controller/EstruturaEmpresarialController.java */
package com.sysfluxo.web.controller;

import com.sysfluxo.web.service.CaixaService;
import com.sysfluxo.web.service.CentroCustoService;
import com.sysfluxo.web.service.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import java.util.Map;

@RestController
@RequestMapping("/api/estrutura")
public class EstruturaEmpresarialController {

    @Autowired
    private CaixaService caixaService;

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private CentroCustoService centroCustoService;

    @GetMapping("/caixas")
    public Flux<Map<String, Object>> getCaixas() {
        return caixaService.listarContasCaixa();
    }

    @GetMapping("/empresas")
    public Flux<Map<String, Object>> getEmpresas() {
        return empresaService.listarEmpresas();
    }

    @GetMapping("/centros-custo")
    public Flux<Map<String, Object>> getCentrosCusto() {
        return centroCustoService.listarCentrosCusto();
    }
}
