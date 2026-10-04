package br.com.brasil_saas.ativos.controller;

import br.com.brasil_saas.ativos.model.*;
import br.com.brasil_saas.ativos.repository.*;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import br.com.brasil_saas.shared.exception.BusinessException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ativos")
@RequiredArgsConstructor
public class AtivosController {
    private final AtivoImobilizadoRepository ativos;
    private final ManutencaoRepository manutencoes;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<AtivoImobilizado> listar(@AuthenticationPrincipal AuthenticatedUser u){
        return ativos.findAllByEmpresaIdAndDeletedAtIsNullOrderByCodigo(u.getEmpresaId());
    }
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AtivoImobilizado> criar(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody AtivoImobilizado a){
        a.setId(null); a.setEmpresaId(u.getEmpresaId()); a.setDeletedAt(null);
        if (a.getCodigo() == null || a.getCodigo().isBlank()) throw new BusinessException("Código do ativo é obrigatório");
        if (a.getDescricao() == null || a.getDescricao().isBlank()) throw new BusinessException("Descrição do ativo é obrigatória");
        if (a.getValorAquisicao() == null || a.getValorAquisicao().signum() < 0) throw new BusinessException("Valor de aquisição inválido");
        if (a.getValorResidual() == null) a.setValorResidual(java.math.BigDecimal.ZERO);
        if (a.getValorResidual().signum() < 0 || a.getValorResidual().compareTo(a.getValorAquisicao()) > 0) throw new BusinessException("Valor residual inválido");
        if (a.getVidaUtilMeses() == null || a.getVidaUtilMeses() <= 0) throw new BusinessException("Vida útil deve ser maior que zero");
        if(a.getStatus()==null)a.setStatus("ATIVO");
        return ResponseEntity.ok(ativos.save(a));
    }
    @PostMapping("/{id}/baixar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AtivoImobilizado> baixar(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id){
        return ativos.findById(id).map(a -> {
            if(!u.getEmpresaId().equals(a.getEmpresaId())) return null;
            if ("BAIXADO".equals(a.getStatus())) throw new BusinessException("Ativo já está baixado");
            a.setStatus("BAIXADO"); return ativos.save(a);
        }).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/manutencoes")
    @PreAuthorize("isAuthenticated()")
    public List<Manutencao> manutencoes(@AuthenticationPrincipal AuthenticatedUser u){
        return manutencoes.findAllByEmpresaIdAndDeletedAtIsNullOrderByDataProgramadaAsc(u.getEmpresaId());
    }
    @PostMapping("/manutencoes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Manutencao> criarManutencao(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody Manutencao m){
        m.setId(null); m.setEmpresaId(u.getEmpresaId()); m.setDeletedAt(null);
        if (m.getAtivoId() == null || ativos.findById(m.getAtivoId()).filter(a -> u.getEmpresaId().equals(a.getEmpresaId()) && !"BAIXADO".equals(a.getStatus())).isEmpty()) throw new BusinessException("Ativo inexistente, de outra empresa ou baixado");
        if (m.getNumero() == null || m.getNumero().isBlank()) throw new BusinessException("Número da manutenção é obrigatório");
        if (m.getCusto() != null && m.getCusto().signum() < 0) throw new BusinessException("Custo da manutenção não pode ser negativo");
        if(m.getStatus()==null)m.setStatus("ABERTA");
        return ResponseEntity.ok(manutencoes.save(m));
    }
    @PostMapping("/manutencoes/{id}/concluir")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Manutencao> concluir(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id){
        return manutencoes.findById(id).map(m -> {
            if(!u.getEmpresaId().equals(m.getEmpresaId())) return null;
            if ("CONCLUIDA".equals(m.getStatus())) throw new BusinessException("Manutenção já está concluída");
            m.setStatus("CONCLUIDA"); m.setDataConclusao(LocalDate.now()); return manutencoes.save(m);
        }).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}