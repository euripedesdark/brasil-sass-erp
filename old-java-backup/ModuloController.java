/* src/main/java/com/sysfluxo/web/controller/ModuloController.java */
package com.sysfluxo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class ModuloController {

    @GetMapping("/inicio")
    public Mono<String> inicio() {
        return Mono.just("inicio");
    }

    @GetMapping("/financeiro")
    public Mono<String> financeiro() {
        return Mono.just("financeiro");
    }

    @GetMapping("/cadastros")
    public Mono<String> cadastros() {
        return Mono.just("cadastros");
    }

    @GetMapping("/movimentos")
    public Mono<String> movimentos() {
        return Mono.just("movimentos");
    }

    @GetMapping("/relatorios")
    public Mono<String> relatorios() {
        return Mono.just("relatorios");
    }
}
