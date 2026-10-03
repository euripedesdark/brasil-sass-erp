package br.com.brasil_saas.core.controller;

import br.com.brasil_saas.core.repository.BancoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Catalogo de bancos (COMPE).
 *
 * A tela de boleto e o cadastro de conta bancaria usavam uma lista de bancos
 * escrita a mao no frontend, com 7 entradas e um erro: Sicredi e Sicoob com o
 * mesmo codigo. Codigo COMPE errado no boleto significa pagamento caindo no
 * banco errado — e o sistema aceitava qualquer numero, sem conferir se o banco
 * existe.
 *
 * Os dados vem da V96, gerada do catalogo oficial (513 instituicoes).
 */
@RestController
@RequestMapping("/api/core/bancos")
@RequiredArgsConstructor
public class BancoController {

    private final BancoRepository repository;

    public record BancoResponse(
            String compe,
            String ispb,
            String cnpj,
            String nome,
            String nomeCurto,
            String tipo,
            boolean aceitaPix) {}

    /**
     * Busca por nome ou codigo. Sem termo, devolve os bancos de maior movimento
     * — a lista que aparece ao abrir o emissor, que precisa ser curta e util,
     * nao as 513 linhas.
     */
    @GetMapping
    public ResponseEntity<List<BancoResponse>> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Boolean apenasPix) {

        var bancos = (busca == null || busca.isBlank())
                ? repository.findTop50ByAtivoTrueOrderByNome()
                : repository.buscar(busca.trim());

        List<BancoResponse> resposta = bancos.stream()
                .filter(b -> apenasPix == null || !apenasPix || b.isAceitaPix())
                .map(b -> new BancoResponse(b.getCompe(), b.getIspb(), b.getCnpj(),
                        b.getNome(), b.getNomeCurto(), b.getTipo(), b.isAceitaPix()))
                .toList();
        return ResponseEntity.ok(resposta);
    }

    /** Um banco pelo codigo COMPE. 404 quando o codigo nao existe. */
    @GetMapping("/{compe}")
    public ResponseEntity<BancoResponse> porCodigo(@org.springframework.web.bind.annotation.PathVariable String compe) {
        return repository.findById(compe.trim())
                .map(b -> new BancoResponse(b.getCompe(), b.getIspb(), b.getCnpj(),
                        b.getNome(), b.getNomeCurto(), b.getTipo(), b.isAceitaPix()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
