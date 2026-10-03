/* src/main/java/com/sysfluxo/web/controller/ParametrosAuxiliaresController.java */
package com.sysfluxo.web.controller;

import com.sysfluxo.web.service.CondicaoPagamentoService;
import com.sysfluxo.web.service.DocumentoService;
import com.sysfluxo.web.service.MunicipioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import java.util.Map;

@RestController
@RequestMapping("/api/parametros")
public class ParametrosAuxiliaresController {

    @Autowired
    private DocumentoService documentoService;

    @Autowired
    private CondicaoPagamentoService condicaoPagamentoService;

    @Autowired
    private MunicipioService municipioService;

    @GetMapping("/documentos")
    public Flux<Map<String, Object>> getDocumentos() {
        return documentoService.listarDocumentos();
    }

    @GetMapping("/condicoes-pagamento")
    public Flux<Map<String, Object>> getCondicoesPagamento() {
        return condicaoPagamentoService.listarCondicoesPagamento();
    }

    @GetMapping("/municipios")
    public Flux<Map<String, Object>> getMunicipios() {
        return municipioService.listarMunicipios();
    }
}
