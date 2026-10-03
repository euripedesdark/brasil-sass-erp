package br.com.brasil_saas.fiscal.controller;

import br.com.brasil_saas.fiscal.model.Imposto;
import br.com.brasil_saas.fiscal.repository.ImpostoRepository;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Fiscal - Impostos")
@RestController
@RequestMapping("/api/fiscal/impostos")
@RequiredArgsConstructor
public class ImpostoController {

    private final ImpostoRepository repository;

    @Operation(summary = "Lista impostos da empresa")
    @GetMapping
    @PreAuthorize("hasAuthority('fiscal:imposto:leitura')")
    public List<Imposto> listar(@AuthenticationPrincipal AuthenticatedUser usuario,
                                @RequestParam(required = false) String tipo) {
        if (tipo != null && !tipo.isBlank()) {
            return repository.findByEmpresaIdAndTipoAndDeletedAtIsNullOrderBySigla(usuario.getEmpresaId(), tipo.trim());
        }
        return repository.findByEmpresaIdAndDeletedAtIsNullOrderBySigla(usuario.getEmpresaId());
    }

    @Operation(summary = "Busca imposto por código")
    @GetMapping("/{codigo}")
    @PreAuthorize("hasAuthority('fiscal:imposto:leitura')")
    public Imposto buscarPorCodigo(@AuthenticationPrincipal AuthenticatedUser usuario,
                                   @PathVariable String codigo) {
        return repository.findByEmpresaIdAndSiglaAndDeletedAtIsNull(usuario.getEmpresaId(), codigo)
                .orElseThrow(() -> new ResourceNotFoundException("imposto", codigo));
    }
}
