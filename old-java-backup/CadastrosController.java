/* src/main/java/com/sysfluxo/web/controller/CadastrosController.java */
package com.sysfluxo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class CadastrosController {

    @GetMapping("/cadastros")
    public Mono<String> cadastrosPage() {
        return Mono.just("cadastros");
    }
}
