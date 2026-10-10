package br.com.brasil_saas.compras.controller;

import br.com.brasil_saas.compras.model.ToleranciaConferencia;
import br.com.brasil_saas.compras.repository.ToleranciaConferenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/compras/tolerancias")
@RequiredArgsConstructor
public class ToleranciaConferenciaController {

    private static final Set<String> TIPOS = Set.of("VALOR", "PERCENTUAL");
    private final ToleranciaConferenciaRepository repository;

    @GetMapping
    @PreAuthorize("hasAuthority('compras:pedido:leitura')")
    public List<ToleranciaConferencia> listar(@AuthenticationPrincipal AuthenticatedUser u) {
        return repository.findByEmpresaIdAndAtivoTrueAndDeletedAtIsNullOrderById(u.getEmpresaId());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('compras:pedido:escrita')")
    public ResponseEntity<ToleranciaConferencia> salvar(@RequestBody ToleranciaConferencia t, @AuthenticationPrincipal AuthenticatedUser u) {
        t.setId(null);
        t.setEmpresaId(u.getEmpresaId());
        t.setDeletedAt(null);
        if (t.getTipo() == null || !TIPOS.contains(t.getTipo()))
            throw new BusinessException("Tipo deve ser VALOR ou PERCENTUAL");
        if (t.getLimite() == null || t.getLimite().signum() < 0)
            throw new BusinessException("Limite invalido");
        if ("PERCENTUAL".equals(t.getTipo()) && t.getLimite().compareTo(new java.math.BigDecimal("100")) > 0)
            throw new BusinessException("Percentual nao pode passar de 100");
        if (t.getAtivo() == null) t.setAtivo(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(t));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('compras:pedido:escrita')")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        ToleranciaConferencia t = repository.findById(id)
                .filter(x -> u.getEmpresaId().equals(x.getEmpresaId()) && x.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Tolerancia nao encontrada"));
        t.setDeletedAt(java.time.LocalDateTime.now());
        repository.save(t);
        return ResponseEntity.noContent().build();
    }
}
