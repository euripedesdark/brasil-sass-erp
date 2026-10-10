package br.com.brasil_saas.compras.controller;

import br.com.brasil_saas.compras.model.AlcadaAprovacao;
import br.com.brasil_saas.compras.repository.AlcadaAprovacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import java.util.List;

@RestController
@RequestMapping("/api/compras/alcadas")
@RequiredArgsConstructor
public class AlcadaAprovacaoController {

    private final AlcadaAprovacaoRepository repository;

    @GetMapping
    @PreAuthorize("hasAuthority('compras:pedido:leitura')")
    public List<AlcadaAprovacao> listar(@AuthenticationPrincipal AuthenticatedUser u) {
        return repository.findByEmpresaIdAndAtivoTrueAndDeletedAtIsNullOrderByValorLimiteAsc(u.getEmpresaId());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('compras:pedido:escrita')")
    public ResponseEntity<AlcadaAprovacao> salvar(@RequestBody AlcadaAprovacao a, @AuthenticationPrincipal AuthenticatedUser u) {
        a.setId(null);
        a.setEmpresaId(u.getEmpresaId());
        a.setDeletedAt(null);
        if (a.getValorLimite() == null || a.getValorLimite().signum() <= 0)
            throw new br.com.brasil_saas.shared.exception.BusinessException("Limite da alcada deve ser maior que zero");
        if (a.getAtivo() == null) a.setAtivo(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(a));
    }
}
