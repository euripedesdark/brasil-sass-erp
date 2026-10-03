package br.com.brasil_saas.fiscal.controller;

import br.com.brasil_saas.fiscal.model.Ncm;
import br.com.brasil_saas.fiscal.repository.NcmRepository;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Fiscal - NCM")
@RestController
@RequestMapping("/api/fiscal/ncm")
@RequiredArgsConstructor
public class NcmController {

    private final NcmRepository repository;

    @Operation(summary = "Lista NCM com paginação e filtro por descrição")
    @GetMapping
    @PreAuthorize("hasAuthority('fiscal:ncm:leitura')")
    public PageResponse<Ncm> listar(@RequestParam(required = false) String descricao,
                                    @PageableDefault(size = 50, sort = "codigo") Pageable pageable) {
        var page = (descricao != null && !descricao.isBlank())
                ? repository.findByDescricaoContainingIgnoreCaseOrderByCodigo(descricao.trim(), pageable)
                : repository.findAllByOrderByCodigo(pageable);
        return PageResponse.from(page, n -> n);
    }

    @Operation(summary = "Busca NCM por código")
    @GetMapping("/{codigo}")
    @PreAuthorize("hasAuthority('fiscal:ncm:leitura')")
    public Ncm buscarPorCodigo(@PathVariable String codigo) {
        return repository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("ncm", codigo));
    }
}
