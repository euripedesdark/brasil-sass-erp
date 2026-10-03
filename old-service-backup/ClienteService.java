/* src/main/java/com/sysfluxo/web/service/ClienteService.java */
package com.sysfluxo.web.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Map;

@Service
public class ClienteService {

    public Flux<Map<String, Object>> listarClientesFornecedores() {
        return Flux.just(
            Map.of("id", 1, "nome", "Indústria Metalúrgica Alfa S/A", "tipo", "Cliente", "cidade", "São Paulo - SP"),
            Map.of("id", 2, "nome", "Distribuidora de Insumos Beta Ltda", "tipo", "Fornecedor", "cidade", "Curitiba - PR")
        );
    }
}
