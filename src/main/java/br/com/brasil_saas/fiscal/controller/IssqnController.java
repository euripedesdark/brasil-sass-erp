package br.com.brasil_saas.fiscal.controller;

import br.com.brasil_saas.fiscal.model.Issqn;
import br.com.brasil_saas.fiscal.repository.IssqnRepository;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Fiscal - ISSQN")
@RestController
@RequestMapping("/api/fiscal/issqn")
@RequiredArgsConstructor
public class IssqnController {

    private final IssqnRepository repository;

    @Operation(summary = "Busca alíquota ISS por código IBGE do município")
    @GetMapping("/municipio/{codigoIbge}")
    @PreAuthorize("hasAuthority('fiscal:issqn:leitura')")
    public Issqn buscarPorMunicipio(@PathVariable String codigoIbge) {
        return repository.findByCodIbge(codigoIbge)
                .orElseThrow(() -> new ResourceNotFoundException("issqn", codigoIbge));
    }

    @Operation(summary = "Lista alíquotas ISS por UF")
    @GetMapping("/uf/{uf}")
    @PreAuthorize("hasAuthority('fiscal:issqn:leitura')")
    public List<Issqn> listarPorUf(@PathVariable String uf) {
        return repository.findByUfOrderByMunicipio(uf.toUpperCase());
    }
}
