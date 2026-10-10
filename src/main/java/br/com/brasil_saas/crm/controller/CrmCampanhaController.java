package br.com.brasil_saas.crm.controller;
import br.com.brasil_saas.crm.model.*; import br.com.brasil_saas.crm.service.CrmCampanhaService;
import br.com.brasil_saas.shared.security.AuthenticatedUser; import lombok.RequiredArgsConstructor;
import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/crm/campanhas") @RequiredArgsConstructor
public class CrmCampanhaController {
 private final CrmCampanhaService service;
 public record StatusReq(String status){} public record VincularReq(Long leadId){}
 public record ResultadoReq(String status,String observacao){}
 @GetMapping @PreAuthorize("hasAuthority('crm:leitura')")
 public List<CrmCampanha> listar(@AuthenticationPrincipal AuthenticatedUser u){return service.listar(u.getEmpresaId());}
 @PostMapping @PreAuthorize("hasAuthority('crm:escrita')")
 public ResponseEntity<CrmCampanha> criar(@AuthenticationPrincipal AuthenticatedUser u,@RequestBody CrmCampanha c){return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(u.getEmpresaId(),c));}
 @PutMapping("/{id}") @PreAuthorize("hasAuthority('crm:escrita')")
 public CrmCampanha alterar(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id,@RequestBody CrmCampanha c){return service.alterar(u.getEmpresaId(),id,c);}
 @PostMapping("/{id}/status") @PreAuthorize("hasAuthority('crm:escrita')")
 public CrmCampanha status(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id,@RequestBody StatusReq r){return service.mudarStatus(u.getEmpresaId(),id,r.status());}
 @GetMapping("/{id}/contatos") @PreAuthorize("hasAuthority('crm:leitura')")
 public List<CrmCampanhaContato> contatos(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id){return service.contatos(u.getEmpresaId(),id);}
 @PostMapping("/{id}/contatos") @PreAuthorize("hasAuthority('crm:escrita')")
 public ResponseEntity<CrmCampanhaContato> vincular(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id,@RequestBody VincularReq r){return ResponseEntity.status(HttpStatus.CREATED).body(service.vincular(u.getEmpresaId(),id,r.leadId()));}
 @PostMapping("/{id}/contatos/{contatoId}/resultado") @PreAuthorize("hasAuthority('crm:escrita')")
 public CrmCampanhaContato resultado(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id,@PathVariable Long contatoId,@RequestBody ResultadoReq r){return service.registrar(u.getEmpresaId(),id,contatoId,r.status(),r.observacao());}
 @GetMapping("/{id}/resumo") @PreAuthorize("hasAuthority('crm:leitura')")
 public Map<String,Object> resumo(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id){return service.resumo(u.getEmpresaId(),id);}
}
