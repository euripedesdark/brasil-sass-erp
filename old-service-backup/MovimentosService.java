/* src/main/java/com/sysfluxo/web/service/MovimentosService.java */
package com.sysfluxo.web.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.Map;

@Service
public class MovimentosService {

    public Flux<Map<String, Object>> listarOrdensServico() {
        return Flux.just(
            Map.of("os", 1001, "cliente", "Indústria Alfa", "data", "2026-09-03", "status", "Finalizada", "valor", 1500.00),
            Map.of("os", 1002, "cliente", "Distribuidora Beta", "data", "2026-09-04", "status", "Aberta", "valor", 850.00)
        );
    }
}
