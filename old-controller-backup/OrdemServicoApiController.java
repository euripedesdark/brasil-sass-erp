/* src/main/java/com/sysfluxo/web/controller/OrdemServicoApiController.java */
package com.sysfluxo.web.controller;

import com.sysfluxo.web.model.ItemOrdemServico;
import com.sysfluxo.web.model.OrdemServico;
import com.sysfluxo.web.repository.ItemOrdemServicoRepository;
import com.sysfluxo.web.repository.OrdemServicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping({"/api/os", "/api/movimentos/os"})
public class OrdemServicoApiController {

    @Autowired
    private OrdemServicoRepository repository;

    @Autowired
    private ItemOrdemServicoRepository itemRepository;

    @GetMapping
    public Flux<OrdemServico> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<OrdemServico>> buscarPorId(@PathVariable Integer id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/itens/{idMov}")
    public Flux<ItemOrdemServico> listarItensDaOs(@PathVariable Integer idMov) {
        return itemRepository.findByIdMov(idMov);
    }

    @PostMapping("/itens")
    public Mono<ResponseEntity<ItemOrdemServico>> adicionarItem(@RequestBody ItemOrdemServico item) {
        if (item.getQuantidade() == null) item.setQuantidade(BigDecimal.ONE);
        if (item.getValorUnitario() == null) item.setValorUnitario(BigDecimal.ZERO);

        // 1. Calcula o total do item individual (Qtd * VlrUnitario)
        item.setTotalItem(item.getQuantidade().multiply(item.getValorUnitario()));

        // 2. Salva o item e atualiza o total da O.S. principal
        return itemRepository.save(item)
                .flatMap(itemSalvo ->
                    itemRepository.somarTotalItensPorOs(itemSalvo.getIdMov())
                        .flatMap(somaTotal ->
                            repository.findById(itemSalvo.getIdMov())
                                .flatMap(os -> {
                                    os.setValorBrutoMov(somaTotal);
                                    os.setValorLiquidoMov(somaTotal);
                                    return repository.save(os);
                                })
                        ).thenReturn(itemSalvo)
                )
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<OrdemServico>> salvar(@RequestBody OrdemServico os) {
        // Define valores padrão obrigatórios para evitar erro 500 por colunas nulas no banco
        if (os.getDataMov() == null) {
            os.setDataMov(LocalDate.now());
        }
        if (os.getBaixaMov() == null) {
            os.setBaixaMov("N");
        }
        if (os.getIdEmpresaMov() == null) {
            os.setIdEmpresaMov(1); // Empresa padrão
        }
        if (os.getValorBrutoMov() == null) {
            os.setValorBrutoMov(BigDecimal.ZERO);
        }
        if (os.getValorLiquidoMov() == null) {
            os.setValorLiquidoMov(BigDecimal.ZERO);
        }

        return repository.save(os)
                .map(ResponseEntity::ok);
    }
}
