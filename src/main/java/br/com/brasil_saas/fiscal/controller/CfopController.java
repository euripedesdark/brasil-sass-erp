package br.com.brasil_saas.fiscal.controller;

import br.com.brasil_saas.fiscal.model.Cfop;
import br.com.brasil_saas.fiscal.repository.CfopRepository;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Fiscal - CFOP")
@RestController
@RequestMapping("/api/fiscal/cfop")
@RequiredArgsConstructor
public class CfopController {

    private final CfopRepository repository;

    @Operation(summary = "Lista todos os CFOP ordenados por código")
    @GetMapping
    @PreAuthorize("hasAuthority('fiscal:cfop:leitura')")
    public List<Cfop> listar(@RequestParam(required = false) String tipoOperacao) {
        if (tipoOperacao != null && !tipoOperacao.isBlank()) {
            return repository.findByTipoOperacaoOrderByCodigo(tipoOperacao.trim());
        }
        return repository.findAllByOrderByCodigo();
    }

    @Operation(summary = "Busca CFOP por código")
    @GetMapping("/{codigo}")
    @PreAuthorize("hasAuthority('fiscal:cfop:leitura')")
    public Cfop buscarPorCodigo(@PathVariable String codigo) {
        return repository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("cfop", codigo));
    }
}
