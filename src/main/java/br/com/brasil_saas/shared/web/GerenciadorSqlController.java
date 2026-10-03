package br.com.brasil_saas.shared.web;

import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.shared.service.GerenciadorSqlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Gerenciador SQL do SUPERUSER — "modo Interbase".
 *
 * Objetivo: permitir manutencao do banco direto pelo navegador, para quando
 * nao houver um cliente SQL na mao. Por isso o console traz navegacao por
 * tabela (estilo browse), estrutura, busca, execucao e exportacao — e nao
 * apenas uma caixa de texto.
 *
 * Restrito a SUPERUSER. Hoje SUPERUSER e ADMIN tem as mesmas permissoes
 * (ver V89), entao a separacao e papel, nao de acesso efetivo.
 */
@Slf4j
@RestController
@RequestMapping("/api/superadmin/sql")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
public class GerenciadorSqlController {

    private final GerenciadorSqlService sql;

    /** Catalogo: tabelas e visoes, para a arvore da esquerda. */
    @GetMapping("/catalogo")
    public Map<String, Object> catalogo() {
        return Map.of(
                "tabelas", sql.listarTabelas(),
                "views", sql.listarViews(),
                "estatisticas", sql.estatisticas());
    }

    @GetMapping("/tabelas/{tabela}")
    public Map<String, Object> tabela(@PathVariable String tabela) {
        return Map.of(
                "tabela", tabela,
                "colunas", sql.descreverTabela(tabela),
                "total", sql.contarRegistros(tabela));
    }

    /** Browse paginado, com busca opcional por coluna. */
    @GetMapping("/tabelas/{tabela}/dados")
    public Map<String, Object> dados(
            @PathVariable String tabela,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "50") int tamanho,
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String coluna) {
        return sql.navegar(tabela, pagina, tamanho, busca, coluna);
    }

    /**
     * Executa um comando. Alteracao exige `confirmar=true`; caso contrario
     * devolve 409 e o console pede a confirmacao na tela.
     */
    @PostMapping("/executar")
    public ResponseEntity<?> executar(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody ComandoRequest req) {

        try {
            GerenciadorSqlService.Resultado r =
                    sql.executar(req.sql(), req.confirmar(), user.getUsername());
            return ResponseEntity.ok(ApiResponse.success(r));

        } catch (GerenciadorSqlService.ConfirmacaoNecessariaException e) {
            return ResponseEntity.status(409).body(ApiResponse.error("CONFIRMACAO_NECESSARIA", e.getMessage()));

        } catch (GerenciadorSqlService.IdentificadorInvalidoException
                 | GerenciadorSqlService.ColunaInvalidaException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("SQL_INVALIDO", e.getMessage()));

        } catch (Exception e) {
            log.warn("Falha ao executar SQL por usuario='{}': {}", user.getUsername(), e.toString());
            return ResponseEntity.badRequest().body(ApiResponse.error("ERRO_SQL", e.getMessage()));
        }
    }

    /** Script com varios comandos separados por ';'. */
    @PostMapping("/script")
    public ResponseEntity<?> script(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody ScriptRequest req) {

        try {
            List<GerenciadorSqlService.Resultado> r =
                    sql.executarScript(req.script(), req.confirmar(), user.getUsername());
            return ResponseEntity.ok(ApiResponse.success(r));

        } catch (GerenciadorSqlService.ConfirmacaoNecessariaException e) {
            return ResponseEntity.status(409).body(ApiResponse.error("CONFIRMACAO_NECESSARIA", e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("ERRO_SQL", e.getMessage()));
        }
    }

    /**
     * Alteracao de um campo. Tabela e coluna sao validadas contra o catalogo
     * antes de qualquer SQL ser montado.
     */
    @PostMapping("/campo")
    public ResponseEntity<?> atualizarCampo(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody CampoRequest req) {

        try {
            GerenciadorSqlService.Resultado r = sql.atualizarCampo(
                    req.tabela(), req.coluna(), req.valor(), req.id(), req.chave(), user.getUsername());
            return ResponseEntity.ok(ApiResponse.success(r));

        } catch (GerenciadorSqlService.IdentificadorInvalidoException
                 | GerenciadorSqlService.ColunaInvalidaException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("SQL_INVALIDO", e.getMessage()));

        } catch (Exception e) {
            log.warn("Falha ao alterar campo por usuario='{}': {}", user.getUsername(), e.toString());
            return ResponseEntity.badRequest().body(ApiResponse.error("ERRO_SQL", e.getMessage()));
        }
    }

    // SQL chega no corpo, nunca na query string: nao vaza em log de proxy,
    // historico do navegador nem server access log.
    public record ComandoRequest(String sql, boolean confirmar) {
    }

    public record ScriptRequest(String script, boolean confirmar) {
    }

    public record CampoRequest(String tabela, String coluna, Object valor, Object id, String chave) {
    }
}
