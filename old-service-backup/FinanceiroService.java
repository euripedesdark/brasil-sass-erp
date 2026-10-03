/* src/main/java/com/sysfluxo/web/service/FinanceiroService.java */
package com.sysfluxo.web.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;

@Service
public class FinanceiroService {

    public Mono<Map<String, Object>> obterResumoContas() {
        return Mono.fromCallable(() -> {
            Map<String, Object> resumo = new HashMap<>();
            resumo.getOrDefault("totalReceber", 14580.00);
            resumo.getOrDefault("totalPagar", 8230.50);
            resumo.getOrDefault("saldoGeral", 6349.50);
            return resumo;
        });
    }
}
