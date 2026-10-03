package br.com.brasil_saas.fiscal.controller;

import br.com.brasil_saas.fiscal.model.Cest;
import br.com.brasil_saas.fiscal.repository.CestRepository;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Fiscal - CEST")
@RestController
@RequestMapping("/api/fiscal/cest")
@RequiredArgsConstructor
public class CestController {

    private final CestRepository repository;

    @Operation(summary = "Lista CEST com paginação e filtro por código, descrição ou NCM")
    @GetMapping
    @PreAuthorize("hasAuthority('fiscal:cest:leitura')")
    public PageResponse<Cest> listar(@RequestParam(required = false) String descricao,
                                     @RequestParam(required = false) String busca,
                                     @PageableDefault(size = 50, sort = "codigo") Pageable pageable) {
        // `busca` e o filtro novo, e cobre codigo + descricao + NCM num campo
        // so. `descricao` continua existindo porque ja estava published e
        // mudar o nome do parametro quebraria quem chama. Se os dois vierem, o
        // novo manda — quem manda no parametro novo e a tela.
        String termo = (busca != null && !busca.isBlank())
                ? busca.trim()
                : (descricao != null && !descricao.isBlank() ? descricao.trim() : null);

        var page = (termo != null)
                ? repository.buscar(termo, normaliza(termo), pageable)
                : repository.findAll(pageable);
        return PageResponse.from(page, c -> c);
    }

    /**
     * Tira do termo tudo que nao e digito, para comparar com o NCM gravado.
     *
     * <p>Precisa ser o mesmo tratamento dos dois lados: a coluna guarda
     * {@code 84712000} e quem digita digita {@code 8471.20.00}. Sem tirar os
     * separadores, o LIKE compara "84712000" com "8471.20.00" e nao acha
     * nunca — que era o bug do filtro, junto com a tela que so olhava a
     * primeira pagina.
     */
    private String normaliza(String termo) {
        return termo == null ? "" : termo.replaceAll("[^0-9]", "");
    }

    @Operation(summary = "Busca CEST por código")
    @GetMapping("/{codigo}")
    @PreAuthorize("hasAuthority('fiscal:cest:leitura')")
    public Cest buscarPorCodigo(@PathVariable String codigo) {
        return repository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("cest", codigo));
    }

    @Operation(summary = "Lista CEST por NCM")
    @GetMapping("/por-ncm/{ncm}")
    @PreAuthorize("hasAuthority('fiscal:cest:leitura')")
    public List<Cest> listarPorNcm(@PathVariable String ncm) {
        return repository.findByNcmOrderByCodigo(ncm);
    }
}
