package br.com.brasil_saas.fiscal.controller;

import br.com.brasil_saas.fiscal.busca.BuscaFiscalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Busca de código fiscal que não exige que o usuário saiba o código.
 *
 * <p>Existe porque o cadastro fiscal tem tela, mas as telas só filtram por
 * descrição ou por código exato. Quem cadastra produto pensa em "cerveja" e
 * "detergente", não em "22030000". A consequência já está no banco: 4 produtos
 * com um NCM que não existe, 7 medicamentos num código que tem 68 respostas
 * possíveis, e 172 sem classificação nenhuma.
 *
 * <p>O ranking é do servidor. O cliente recebe a ordem e o motivo, e só exibe.
 */
@Tag(name = "Fiscal - Busca")
@RestController
@RequestMapping("/api/fiscal/busca")
@RequiredArgsConstructor
public class BuscaFiscalController {

    private final BuscaFiscalService service;

    @Operation(summary = "Busca código fiscal por código, descrição ou palavra-chave")
    @GetMapping
    @PreAuthorize("hasAuthority('fiscal:ncm:leitura')")
    public List<BuscaFiscalService.Resultado> buscar(
            @RequestParam(name = "q") final String termo,
            @RequestParam(required = false) final String tabela,
            @RequestParam(defaultValue = "20") final int limite) {
        return service.buscar(termo, tabela, limite);
    }
}
