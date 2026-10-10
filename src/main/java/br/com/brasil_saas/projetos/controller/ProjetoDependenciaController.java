package br.com.brasil_saas.projetos.controller;
import br.com.brasil_saas.projetos.model.PrjDependencia; import br.com.brasil_saas.projetos.service.ProjetoDependenciaService;
import br.com.brasil_saas.shared.security.AuthenticatedUser; import lombok.RequiredArgsConstructor;
import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/projetos/{projetoId}/dependencias") @RequiredArgsConstructor
public class ProjetoDependenciaController {
 private final ProjetoDependenciaService service;
 public record Nova(Long antecessoraId,Long sucessoraId,Integer defasagemDias){}
 @GetMapping @PreAuthorize("hasAuthority('projetos:leitura')")
 public List<PrjDependencia> listar(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long projetoId){return service.listar(u.getEmpresaId(),projetoId);}
 @PostMapping @PreAuthorize("hasAuthority('projetos:escrita')")
 public ResponseEntity<PrjDependencia> adicionar(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long projetoId,@RequestBody Nova n){return ResponseEntity.status(HttpStatus.CREATED).body(service.adicionar(u.getEmpresaId(),projetoId,n.antecessoraId(),n.sucessoraId(),n.defasagemDias()));}
 @DeleteMapping("/{id}") @PreAuthorize("hasAuthority('projetos:escrita')")
 public ResponseEntity<Void> excluir(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long projetoId,@PathVariable Long id){service.excluir(u.getEmpresaId(),projetoId,id);return ResponseEntity.noContent().build();}
}
