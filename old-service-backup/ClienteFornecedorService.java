/* src/main/java/com/sysfluxo/web/service/ClienteFornecedorService.java */
package com.sysfluxo.web.service;

import com.sysfluxo.web.model.ClienteFornecedor;
import com.sysfluxo.web.repository.ClienteFornecedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDate;

@Service
public class ClienteFornecedorService {

    @Autowired
    private ClienteFornecedorRepository repository;

    public Flux<ClienteFornecedor> listarTodos() {
        return repository.findAll();
    }

    public Mono<ClienteFornecedor> salvar(ClienteFornecedor cfo) {
        // Regra legado: Pais padrão BRASIL e código 1058[cite: 118]
        if (cfo.getPais() == null || cfo.getPais().isEmpty()) {
            cfo.setPais("BRASIL");
            cfo.setCodPais("1058");
        }

        // Regra legado: Data de cadastro automática[cite: 118]
        if (cfo.getDataCadastro() == null) {
            cfo.setDataCadastro(LocalDate.now());
        }

        // Regra legado: Se Razão Social estiver vazia, copia Nome Fantasia[cite: 118]
        if (cfo.getNomeRazao() == null || cfo.getNomeRazao().isBlank()) {
            cfo.setNomeRazao(cfo.getNomeFantasia());
        }

        // Regra legado: Formatação de CEP (remove traços e pontos, garante XXXXX-XXX)[cite: 118]
        if (cfo.getCep() != null) {
            String cepLimpo = cfo.getCep().replaceAll("[-.]", "");
            if (cepLimpo.length() == 8) {
                cfo.setCep(cepLimpo.substring(0, 5) + "-" + cepLimpo.substring(5, 8));
            }
        }

        return repository.save(cfo);
    }
}
