/* src/main/java/com/sysfluxo/web/controller/MovimentosController.java */
package com.sysfluxo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class MovimentosController {

    @GetMapping("/movimentos")
    public Mono<String> movimentosPage() {
        return Mono.just("movimentos");
    }
}
