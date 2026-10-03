/* src/main/java/com/sysfluxo/web/controller/FinanceiroController.java */
package com.sysfluxo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class FinanceiroController {

    @GetMapping("/financeiro")
    public Mono<String> financeiroPage() {
        return Mono.just("financeiro");
    }
}
