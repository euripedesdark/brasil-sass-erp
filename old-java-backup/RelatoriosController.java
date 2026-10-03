/* src/main/java/com/sysfluxo/web/controller/RelatoriosController.java */
package com.sysfluxo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class RelatoriosController {

    @GetMapping("/relatorios")
    public Mono<String> relatoriosPage() {
        return Mono.just("relatorios");
    }
}
