/* src/main/java/com/sysfluxo/web/controller/OperacoesAvancadasController.java */
package com.sysfluxo.web.controller;

import com.sysfluxo.web.service.FechamentoMovimentoService;
import com.sysfluxo.web.service.FuncionarioService;
import com.sysfluxo.web.service.RelatorioSinteticoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/operacoes")
public class OperacoesAvancadasController {

    @Autowired
    private FechamentoMovimentoService fechamentoService;

    @Autowired
    private FuncionarioService funcionarioService;

    @Autowired
    private RelatorioSinteticoService relatorioService;

    @PostMapping("/fechar-os")
    public Mono<ResponseEntity<Map<String, Object>>> fecharOS(@RequestBody Map<String, Object> payload) {
        int idMov = Integer.parseInt(payload.getOrDefault("idMovimento", 0).toString());
        double valor = Double.parseDouble(payload.getOrDefault("valorLiquido", 0.0).toString());
        int parcelas = Integer.parseInt(payload.getOrDefault("numeroParcelas", 1).toString());
        int idCondicao = Integer.parseInt(payload.getOrDefault("idCondicao", 1).toString());

        return fechamentoService.fecharOrdemServico(idMov, valor, parcelas, idCondicao)
                .map(ResponseEntity::ok)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/funcionarios/ativos")
    public Flux<Map<String, Object>> getFuncionariosAtivos() {
        return funcionarioService.listarFuncionariosAtivos();
    }

    @GetMapping("/relatorio-resumo")
    public Mono<ResponseEntity<Map<String, Object>>> getResumo(
            @RequestParam String dataInicial, 
            @RequestParam String dataFinal, 
            @RequestParam int idEmpresa) {
        
        return relatorioService.gerarResumoPagamentosRecebimentos(
                LocalDate.parse(dataInicial), 
                LocalDate.parse(dataFinal), 
                idEmpresa)
                .map(ResponseEntity::ok)
                .subscribeOn(Schedulers.boundedElastic());
    }
}
