package br.com.brasil_saas.ativos.controller;

import br.com.brasil_saas.ativos.model.*;
import br.com.brasil_saas.ativos.repository.*;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ativos")
@RequiredArgsConstructor
public class AtivosController {
    private final AtivoImobilizadoRepository ativos;
    private final ManutencaoRepository manutencoes;

    @GetMapping
    public List<AtivoImobilizado> listar(@AuthenticationPrincipal AuthenticatedUser u){
        return ativos.findAllByEmpresaIdAndDeletedAtIsNullOrderByCodigo(u.getEmpresaId());
    }
    @PostMapping
    public ResponseEntity<AtivoImobilizado> criar(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody AtivoImobilizado a){
        a.setId(null); a.setEmpresaId(u.getEmpresaId()); a.setDeletedAt(null);
        if(a.getStatus()==null)a.setStatus("ATIVO");
        return ResponseEntity.ok(ativos.save(a));
    }
    @PostMapping("/{id}/baixar")
    public ResponseEntity<AtivoImobilizado> baixar(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id){
        return ativos.findById(id).map(a -> {
            if(!u.getEmpresaId().equals(a.getEmpresaId())) return null;
            a.setStatus("BAIXADO"); return ativos.save(a);
        }).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/manutencoes")
    public List<Manutencao> manutencoes(@AuthenticationPrincipal AuthenticatedUser u){
        return manutencoes.findAllByEmpresaIdAndDeletedAtIsNullOrderByDataProgramadaAsc(u.getEmpresaId());
    }
    @PostMapping("/manutencoes")
    public ResponseEntity<Manutencao> criarManutencao(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody Manutencao m){
        m.setId(null); m.setEmpresaId(u.getEmpresaId()); m.setDeletedAt(null);
        if(m.getStatus()==null)m.setStatus("ABERTA");
        return ResponseEntity.ok(manutencoes.save(m));
    }
    @PostMapping("/manutencoes/{id}/concluir")
    public ResponseEntity<Manutencao> concluir(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id){
        return manutencoes.findById(id).map(m -> {
            if(!u.getEmpresaId().equals(m.getEmpresaId())) return null;
            m.setStatus("CONCLUIDA"); m.setDataConclusao(LocalDate.now()); return manutencoes.save(m);
        }).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}