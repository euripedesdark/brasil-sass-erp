package br.com.brasil_saas.workflow.controller;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.workflow.model.*;
import br.com.brasil_saas.workflow.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/workflow") @RequiredArgsConstructor
public class WorkflowController {
    private final WorkflowService svc;
    public record AbrirReq(String entidadeTipo, Long entidadeId, Long definitionId, String observacao) {}
    public record DecidirReq(Boolean aprovar, String comentario) {}
    public record DelegarReq(Long usuarioId) {}
    @GetMapping("/definitions") @PreAuthorize("hasAuthority('workflow:leitura')")
    public List<WkfDefinition> definitions(@AuthenticationPrincipal AuthenticatedUser u) {
        return svc.definitions(u.getEmpresaId());
    }
    @PostMapping("/definitions") @PreAuthorize("hasAuthority('workflow:escrita')")
    public ResponseEntity<WkfDefinition> salvar(@AuthenticationPrincipal AuthenticatedUser u, @Valid @RequestBody WkfDefinition d) {
        return ResponseEntity.status(HttpStatus.CREATED).body(svc.salvarDefinition(u.getEmpresaId(), u.getId(), d));
    }
    @PostMapping("/definitions/{id}/stages") @PreAuthorize("hasAuthority('workflow:escrita')")
    public WkfDefinition addStage(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @Valid @RequestBody WkfStage s) {
        return svc.addStage(u.getEmpresaId(), id, s);
    }
    @GetMapping("/definitions/{id}/stages") @PreAuthorize("hasAuthority('workflow:leitura')")
    public List<WkfStage> stages(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        return svc.stages(u.getEmpresaId(), id);
    }
    @PostMapping("/definitions/{id}/inativar") @PreAuthorize("hasAuthority('workflow:escrita')")
    public ResponseEntity<Void> inativar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        svc.inativarDefinition(u.getEmpresaId(), id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/instances") @PreAuthorize("hasAuthority('workflow:escrita')")
    public ResponseEntity<WkfInstance> abrir(@AuthenticationPrincipal AuthenticatedUser u, @Valid @RequestBody AbrirReq r) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(svc.abrir(u.getEmpresaId(), u.getId(), r.entidadeTipo(), r.entidadeId(), r.definitionId(), r.observacao()));
    }
    @GetMapping("/instances") @PreAuthorize("hasAuthority('workflow:leitura')")
    public List<WkfInstance> instances(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam(required = false) String status) {
        return svc.instances(u.getEmpresaId(), status);
    }
    @GetMapping("/instances/{id}/tasks") @PreAuthorize("hasAuthority('workflow:leitura')")
    public List<WkfTask> tasks(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        return svc.tasks(u.getEmpresaId(), id);
    }
    @GetMapping("/tasks/pendentes") @PreAuthorize("hasAuthority('workflow:leitura')")
    public List<WkfTask> pendentes(@AuthenticationPrincipal AuthenticatedUser u) {
        return svc.pendentes(u.getEmpresaId());
    }
    @PostMapping("/tasks/{id}/delegar") @PreAuthorize("hasAuthority('workflow:delegar')")
    public WkfTask delegar(@AuthenticationPrincipal AuthenticatedUser u,@PathVariable Long id,@RequestBody DelegarReq r){return svc.delegar(u.getEmpresaId(),u.getId(),id,r.usuarioId());}
    @PostMapping("/tasks/{id}/decidir") @PreAuthorize("hasAuthority('workflow:escrita')")
    public WkfTask decidir(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody DecidirReq r) {
        return svc.decidir(u.getEmpresaId(), u.getId(), id, Boolean.TRUE.equals(r.aprovar()), r.comentario());
    }
}
