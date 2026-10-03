package br.com.brasil_saas.ia.controller;

import br.com.brasil_saas.ia.assistente.AssistenteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Assistente ERP — o especialista que responde com o ERP, e não com o modelo.
 *
 * <p>A ordem das fontes é o desenho: busca fiscal, dados do ERP, documentação, e
 * só então o modelo. Para pergunta de classificação fiscal a busca é obrigatória
 * — o motivo está no banco, onde há 4 produtos com um NCM que não existe e 7
 * medicamentos num código que tem 68 respostas possíveis.
 *
 * <p>A permissao e IA_READ, a mesma dos outros controllers de IA: o modulo
 * inteiro usa essa, e nao existe permissao ia:* no cadastro.
 *
 * <p>A tela que consome isto é "Assistente ERP", não "Chat". A diferença não é
 * de nome: o que responde não é o modelo, é o ERP com o modelo explicando.
 */
@Tag(name = "IA - Assistente ERP")
@RestController
@RequestMapping("/api/ia/assistente")
@RequiredArgsConstructor
public class AssistenteController {

    private final AssistenteService service;

    @Operation(summary = "Pergunta ao assistente do ERP")
    @PostMapping
    @PreAuthorize("hasAuthority('IA_READ')")
    public AssistenteService.Resposta perguntar(
            @RequestHeader("X-Empresa-Id") final Long empresaId,
            final Authentication auth,
            @RequestBody final PerguntaRequest req) {
        return service.perguntar(req.pergunta(), empresaId, usuario(auth));
    }

    @Operation(summary = "Histórico do assistente com a auditoria de cada resposta")
    @GetMapping("/auditoria")
    @PreAuthorize("hasAuthority('IA_READ')")
    public List<Object> auditoria(@RequestHeader("X-Empresa-Id") final Long empresaId) {
        return service.auditoria(empresaId);
    }

    /**
     * O nome de quem perguntou.
     *
     * <p>É o nome, e não o id. O id exigiria converter o username em número, e o
     * username não é número — é "euripedes". A tentativa anterior engolia o
     * NumberFormatException e deixava o campo nulo, sem nenhum aviso. Além disso,
     * com o roteamento por {@code bc_core_auth_source} um usuário de AD pode não
     * existir em {@code bc_core_usuario}, então o id também não é um caminho
     * confiável. O nome é o mesmo em AD, Postgres e Linux.
     */
    private String usuario(final Authentication auth) {
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return "desconhecido";
        }
        return auth.getName();
    }

    /** A pergunta. Curta de propósito: é uma consulta a um ERP, não uma conversa. */
    public record PerguntaRequest(
            @NotBlank(message = "A pergunta não pode ser vazia")
            @Size(max = 500, message = "A pergunta pode ter no máximo 500 caracteres")
            String pergunta) {
    }
}
