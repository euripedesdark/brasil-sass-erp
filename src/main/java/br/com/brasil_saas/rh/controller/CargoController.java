package br.com.brasil_saas.rh.controller;

import br.com.brasil_saas.rh.model.Cargo;
import br.com.brasil_saas.rh.repository.CargoRepository;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import br.com.brasil_saas.shared.exception.BusinessException;

@RestController
@RequestMapping("/api/rh/cargos")
@RequiredArgsConstructor
public class CargoController {

    private final CargoRepository repo;

    @GetMapping
    @PreAuthorize("hasAuthority('rh:cargo:leitura')")
    public List<Cargo> listar(@AuthenticationPrincipal AuthenticatedUser u) {
        return repo.findByEmpresaIdAndAtivoTrueOrderByNome(u.getEmpresaId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('rh:cargo:leitura')")
    public Cargo buscar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        return repo.findById(id)
            .filter(c -> c.getEmpresaId().equals(u.getEmpresaId()))
            .orElseThrow(() -> new ResourceNotFoundException("Cargo não encontrado"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('rh:cargo:escrita')")
    @ResponseStatus(HttpStatus.CREATED)
    public Cargo criar(@RequestBody Cargo c, @AuthenticationPrincipal AuthenticatedUser u) {
        c.setId(null);
        // Empresa do token: sem isso o cargo nascia com empresa_id nulo e o
        // cadastro respondia 409 com mensagem de integridade, sem relação com
        // o que o usuário fez.
        c.setEmpresaId(u.getEmpresaId());
        if (c.getNome() == null || c.getNome().isBlank()) throw new BusinessException("Nome do cargo é obrigatório");
        if (c.getSalarioBase() != null && c.getSalarioBase().signum() < 0) throw new BusinessException("Salário base não pode ser negativo");
        if (c.getAtivo() == null) {
            c.setAtivo(true);
        }
        return repo.save(c);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('rh:cargo:escrita')")
    public Cargo atualizar(@PathVariable Long id, @RequestBody Cargo payload,
                           @AuthenticationPrincipal AuthenticatedUser u) {
        Cargo existente = repo.findById(id)
            .filter(c -> c.getEmpresaId().equals(u.getEmpresaId()))
            .orElseThrow(() -> new ResourceNotFoundException("Cargo não encontrado"));

        if (payload.getNome() != null && payload.getNome().isBlank()) throw new BusinessException("Nome do cargo é obrigatório");
        if (payload.getSalarioBase() != null && payload.getSalarioBase().signum() < 0) throw new BusinessException("Salário base não pode ser negativo");
        existente.setNome(payload.getNome());
        existente.setSalarioBase(payload.getSalarioBase());
        if (payload.getAtivo() != null) {
            existente.setAtivo(payload.getAtivo());
        }

        return repo.save(existente);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('rh:cargo:escrita')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        Cargo existente = repo.findById(id)
            .filter(c -> c.getEmpresaId().equals(u.getEmpresaId()))
            .orElseThrow(() -> new ResourceNotFoundException("Cargo não encontrado"));

        if (Boolean.TRUE.equals(existente.getAtivo())) {
            // Soft delete preserva o vinculo historico de funcionarios
            existente.setAtivo(false);
            repo.save(existente);
        }
    }
}
