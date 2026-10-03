/* src/main/java/com/sysfluxo/web/controller/ViewCadastrosController.java */
package com.sysfluxo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class ViewCadastrosController {

    @GetMapping("/cadastros/cfo")
    public Mono<String> paginaCfo() {
        return Mono.just("cfo"); // Retorna o template cfo.html
    }
}
