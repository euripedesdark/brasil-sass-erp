package br.com.brasil_saas.ia.controller;

import br.com.brasil_saas.ia.dto.ChatMensagemRequest;
import br.com.brasil_saas.ia.model.ChatMensagem;
import br.com.brasil_saas.ia.service.ChatMensagemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ia/mensagens")
@Tag(name = "Chat Mensagens", description = "API para gerenciamento de mensagens de chat")
@RequiredArgsConstructor
public class ChatMensagemController {

    private final ChatMensagemService chatMensagemService;

    @PostMapping
    @Operation(summary = "Criar nova mensagem de chat")
    @PreAuthorize("hasAuthority('IA_WRITE')")
    public ResponseEntity<ChatMensagem> criar(@RequestHeader("X-Empresa-Id") Long empresaId,
                                              @RequestBody ChatMensagemRequest request) {
        ChatMensagem mensagem = chatMensagemService.criar(empresaId, request);
        return ResponseEntity.ok(mensagem);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar mensagem por ID")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<ChatMensagem> buscarPorId(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                    @PathVariable Long id) {
        ChatMensagem mensagem = chatMensagemService.buscarPorId(empresaId, id);
        return ResponseEntity.ok(mensagem);
    }

    @GetMapping("/sessao/{sessaoId}")
    @Operation(summary = "Listar mensagens por sessao")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<ChatMensagem>> listarPorSessao(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                              @PathVariable Long sessaoId) {
        List<ChatMensagem> mensagens = chatMensagemService.listarPorSessao(empresaId, sessaoId);
        return ResponseEntity.ok(mensagens);
    }

    @GetMapping("/sessao/{sessaoId}/ordenado")
    @Operation(summary = "Listar mensagens por sessao ordenadas")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<ChatMensagem>> listarPorSessaoOrdered(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                                      @PathVariable Long sessaoId) {
        List<ChatMensagem> mensagens = chatMensagemService.listarPorSessaoOrdered(empresaId, sessaoId);
        return ResponseEntity.ok(mensagens);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir mensagem")
    @PreAuthorize("hasAuthority('IA_DELETE')")
    public ResponseEntity<Void> excluir(@RequestHeader("X-Empresa-Id") Long empresaId,
                                        @PathVariable Long id) {
        chatMensagemService.excluir(empresaId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/sessao/{sessaoId}")
    @Operation(summary = "Excluir todas as mensagens de uma sessao")
    @PreAuthorize("hasAuthority('IA_DELETE')")
    public ResponseEntity<Void> excluirPorSessao(@RequestHeader("X-Empresa-Id") Long empresaId,
                                               @PathVariable Long sessaoId) {
        chatMensagemService.excluirPorSessao(empresaId, sessaoId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/classificar")
    @Operation(summary = "Classificar mensagem")
    @PreAuthorize("hasAuthority('IA_WRITE')")
    public ResponseEntity<Void> classificarMensagem(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                  @PathVariable Long id,
                                                  @RequestParam Integer classificacao,
                                                  @RequestParam(required = false) String feedback) {
        chatMensagemService.classificarMensagem(empresaId, id, classificacao, feedback);
        return ResponseEntity.ok().build();
    }
}
