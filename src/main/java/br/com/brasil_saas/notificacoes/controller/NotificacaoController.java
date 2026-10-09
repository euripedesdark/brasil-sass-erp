package br.com.brasil_saas.notificacoes.controller;

import br.com.brasil_saas.notificacoes.model.Notificacao;
import br.com.brasil_saas.notificacoes.service.NotificacaoService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/core/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {
    private final NotificacaoService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Notificacao> listar(@AuthenticationPrincipal AuthenticatedUser u,
                                    @RequestParam(defaultValue = "false") boolean soNaoLidas) {
        return service.listar(u.getEmpresaId(), u.getId(), soNaoLidas);
    }

    @GetMapping("/contador")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> contador(@AuthenticationPrincipal AuthenticatedUser u) {
        return Map.of("naoLidas", service.naoLidas(u.getEmpresaId(), u.getId()));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Notificacao> criar(@RequestBody Notificacao n, @AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(u.getEmpresaId(), n));
    }

    @PostMapping("/{id}/lida")
    @PreAuthorize("isAuthenticated()")
    public Notificacao lida(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        return service.marcarLida(u.getEmpresaId(), id);
    }

    @PostMapping("/ler-todas")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> lerTodas(@AuthenticationPrincipal AuthenticatedUser u) {
        return Map.of("marcadas", service.marcarTodasLidas(u.getEmpresaId(), u.getId()));
    }
}
