/* src/main/java/com/sysfluxo/web/service/CadastrosService.java */
package com.sysfluxo.web.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.Map;

@Service
public class CadastrosService {

    public Flux<Map<String, Object>> listarClientesFornecedores() {
        return Flux.just(
            Map.of("id", 1, "nome", "Indústria Metalúrgica Alfa S/A", "tipo", "Cliente", "cidade", "São Paulo - SP"),
            Map.of("id", 2, "nome", "Distribuidora de Insumos Beta Ltda", "tipo", "Fornecedor", "cidade", "Curitiba - PR")
        );
    }

    public Flux<Map<String, Object>> listarProdutos() {
        return Flux.just(
            Map.of("id", 101, "descricao", "Manutenção Preventiva de Servidores", "tipo", "Serviço", "preco", 350.00),
            Map.of("id", 102, "descricao", "Licença Anual de Virtualização", "tipo", "Produto", "preco", 1200.00)
        );
    }
}
