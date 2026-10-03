package br.com.brasil_saas.estoque.controller;

import br.com.brasil_saas.estoque.model.Deposito;
import br.com.brasil_saas.estoque.repository.DepositoRepository;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estoque/depositos")
@RequiredArgsConstructor
public class DepositoController {
    private final DepositoRepository repository;

    @GetMapping
    @PreAuthorize("hasAuthority('estoque:deposito:leitura')")
    public List<Deposito> listar(@AuthenticationPrincipal AuthenticatedUser user) {
        return repository.findByEmpresaIdAndAtivoTrueOrderByNomeAsc(user.getEmpresaId());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('estoque:deposito:escrita')")
    public ResponseEntity<Deposito> criar(@AuthenticationPrincipal AuthenticatedUser user,
                                           @Valid @RequestBody DepositoRequest request) {
        repository.findByEmpresaIdAndCodigoAndAtivoTrue(user.getEmpresaId(), request.codigo())
                .ifPresent(d -> { throw new IllegalArgumentException("Já existe um depósito ativo com este código"); });
        Deposito d = new Deposito();
        d.setEmpresaId(user.getEmpresaId());
        d.setCodigo(request.codigo().trim().toUpperCase());
        d.setNome(request.nome().trim());
        d.setTipo(request.tipo() == null || request.tipo().isBlank() ? "PADRAO" : request.tipo().trim().toUpperCase());
        d.setAtivo(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(d));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('estoque:deposito:escrita')")
    public ResponseEntity<Deposito> atualizar(@AuthenticationPrincipal AuthenticatedUser user,
                                               @PathVariable Long id,
                                               @Valid @RequestBody DepositoRequest request) {
        Deposito d = repository.findByIdAndEmpresaIdAndAtivoTrue(id, user.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Depósito não encontrado"));
        d.setCodigo(request.codigo().trim().toUpperCase());
        d.setNome(request.nome().trim());
        d.setTipo(request.tipo() == null || request.tipo().isBlank() ? "PADRAO" : request.tipo().trim().toUpperCase());
        return ResponseEntity.ok(repository.save(d));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('estoque:deposito:escrita')")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        Deposito d = repository.findByIdAndEmpresaIdAndAtivoTrue(id, user.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Depósito não encontrado"));
        d.setAtivo(false);
        d.setDeletedAt(java.time.LocalDateTime.now());
        repository.save(d);
        return ResponseEntity.noContent().build();
    }

    public record DepositoRequest(@NotBlank String codigo, @NotBlank String nome, String tipo) {}
}
