package br.com.brasil_saas.shared.web;

import br.com.brasil_saas.core.service.ModuloAcessoService;
import br.com.brasil_saas.shared.image.DocumentoArquivo;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.shared.service.DocumentoNaoPersistidoException;
import br.com.brasil_saas.shared.service.GenericoDocumentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Documentos de negocio gravados no MongoDB (colecao "documentos").
 *
 * A colecao e dividida por modulo. Quem manda um documento escolhe o modulo;
 * quem busca so recebe o que tem permissao. Modulo marcado como
 * exige_superuser (certificados) so o SUPERUSER enxerga, mesmo que a pessoa
 * tenha o modulo liberado.
 *
 * O upload passa por staging em disco antes de ir ao banco; se a comunicacao
 * falhar o arquivo fica preservado e a resposta diz onde.
 */
@Slf4j
@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final GenericoDocumentoService documentoService;
    private final ModuloAcessoService moduloService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> enviar(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam("file") MultipartFile arquivo,
            @RequestParam("tipoEntidade") String tipoEntidade,
            @RequestParam(value = "modulo", required = false) String modulo,
            @RequestParam(value = "entidadeId", required = false) Long entidadeId) {

        // quem envia precisa poder gravar no modulo de destino
        if (modulo != null && !modulo.isBlank()
                && !moduloService.podeAcessar(user.getId(), modulo)) {
            return ResponseEntity.status(403).body(ApiResponse.error(
                    "MODULO_SEM_PERMISSAO", "Voce nao tem acesso ao modulo " + modulo));
        }

        try {
            DocumentoArquivo doc = documentoService.salvar(
                    user.getEmpresaId(),
                    (modulo == null || modulo.isBlank()) ? null : modulo,
                    tipoEntidade, entidadeId, arquivo);

            return ResponseEntity.ok(ApiResponse.success(Resumo.de(doc)));

        } catch (DocumentoNaoPersistidoException e) {
            return ResponseEntity.status(503).body(
                    ApiResponse.error("ARMAZENAMENTO_INDISPONIVEL",
                            "O arquivo foi preservado em " + e.getTemporario()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("ARQUIVO_INVALIDO", e.getMessage()));

        } catch (IOException e) {
            log.error("Falha de I/O no envio de documento", e);
            return ResponseEntity.status(500).body(ApiResponse.error("FALHA_IO", e.getMessage()));
        }
    }

    @GetMapping("/{id}/conteudo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> baixar(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String id) {

        DocumentoArquivo doc = documentoService.buscarPorId(id);

        if (doc == null || doc.getConteudo() == null) {
            return ResponseEntity.notFound().build();
        }

        // o documento e do tenant de quem esta logado
        if (doc.getEmpresaId() != null && !doc.getEmpresaId().equals(user.getEmpresaId())) {
            return ResponseEntity.notFound().build();
        }

        // e so sai se o modulo estiver liberado; documento sem modulo e restrito
        // a quem tem acesso total
        if (!podeVer(user, doc.getModulo())) {
            log.warn("Usuario {} tentou baixar documento {} do modulo {} sem permissao",
                    user.getUsername(), id, doc.getModulo());
            return ResponseEntity.status(403).body(ApiResponse.error(
                    "MODULO_SEM_PERMISSAO", "Sem acesso ao modulo deste documento"));
        }

        MediaType tipo = MediaType.APPLICATION_OCTET_STREAM;
        if (doc.getContentType() != null && !doc.getContentType().isBlank()) {
            try {
                tipo = MediaType.parseMediaType(doc.getContentType());
            } catch (IllegalArgumentException ignored) {
                // contentType legado invalido: entrega o binario assim mesmo
            }
        }

        return ResponseEntity.ok()
                .contentType(tipo)
                .contentLength(doc.getConteudo().length)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(doc.getNomeArquivo() == null ? id : doc.getNomeArquivo(),
                                StandardCharsets.UTF_8)
                        .build().toString())
                .body(doc.getConteudo());
    }

    /** Documentos do usuario, ja filtrados pelos modulos liberados. */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> listar(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(value = "modulo", required = false) String modulo) {

        if (modulo != null && !modulo.isBlank()) {
            if (!podeVer(user, modulo)) {
                return ResponseEntity.status(403).body(ApiResponse.error(
                        "MODULO_SEM_PERMISSAO", "Sem acesso ao modulo " + modulo));
            }
            return ResponseEntity.ok(ApiResponse.success(documentoService
                    .listarPorModulos(user.getEmpresaId(), Set.of(modulo))
                    .stream().map(Resumo::de).toList()));
        }

        Set<String> liberados = moduloService.modulosDoUsuario(user.getId()).stream()
                .map(br.com.brasil_saas.core.model.Modulo::getChave)
                .collect(Collectors.toSet());

        List<Map<String, Object>> visiveis = documentoService
                .listarPorModulos(user.getEmpresaId(), liberados)
                .stream().map(Resumo::de).toList();

        return ResponseEntity.ok(ApiResponse.success(visiveis));
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> remover(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam("tipoEntidade") String tipoEntidade,
            @RequestParam("entidadeId") Long entidadeId) {

        documentoService.remover(user.getEmpresaId(), tipoEntidade, entidadeId);
        return ResponseEntity.ok(ApiResponse.success("Documento removido"));
    }

    /**
     * Documento sem modulo definido so quem tem acesso total enxerga: sem o
     * modulo nao da para saber a que area pertence, entao o padrao e negar.
     */
    private boolean podeVer(AuthenticatedUser user, String modulo) {
        if (modulo == null || modulo.isBlank()) {
            return moduloService.modulosDoUsuario(user.getId()).size() >= 12;
        }
        return moduloService.podeVerDocumentos(user.getId(), modulo);
    }

    /** Nao devolve os bytes: so os metadados. */
    static final class Resumo {
        private Resumo() {
        }

        static Map<String, Object> de(DocumentoArquivo d) {
            return Map.of(
                    "id", d.getId(),
                    "modulo", d.getModulo() == null ? "" : d.getModulo(),
                    "tipoEntidade", d.getTipoEntidade(),
                    "entidadeId", d.getEntidadeId() == null ? "" : d.getEntidadeId(),
                    "nomeArquivo", d.getNomeArquivo() == null ? "" : d.getNomeArquivo(),
                    "contentType", d.getContentType() == null ? "" : d.getContentType(),
                    "tamanho", d.getTamanho(),
                    "hash", d.getHash() == null ? "" : d.getHash(),
                    "criadoEm", d.getCriadoEm() == null ? "" : d.getCriadoEm().toString());
        }
    }
}
