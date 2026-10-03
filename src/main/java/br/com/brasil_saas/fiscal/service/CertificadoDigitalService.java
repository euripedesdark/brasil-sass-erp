package br.com.brasil_saas.fiscal.service;

import br.com.brasil_saas.shared.image.DocumentoArquivo;
import br.com.brasil_saas.shared.service.DocumentoNaoPersistidoException;
import br.com.brasil_saas.shared.service.GenericoDocumentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Certificado digital usado na emissao de nota (A1, arquivo .pfx/.p12).
 *
 * Existe porque em LTDA o certificado e obrigatorio — a prefeitura exige
 * assinatura de toda mensagem XML, e sem A1 a empresa nao emite NFS-e. Por
 * isso o caminho do .pfx nao pode ficar escondido em variavel de ambiente
 *(num servidor novo ninguem sabe qual arquivo e), entao a tela pede.
 *
 * O arquivo so e gravado no MongoDB se a pessoa pedir explicitamente. Como o
 * certificado e a credencial que libera a emissao da empresa, guardar e decisao
 * consciente, nao um efeito colateral do upload.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CertificadoDigitalService {

    private static final Set<String> EXTENSOES = Set.of(".pfx", ".p12");

    private static final long TAMANHO_MAXIMO = 10L * 1024 * 1024;

    /** Modulo que guarda documento sensivel: so SUPERUSER enxerga. */
    public static final String MODULO = "fiscal";

    public static final String TIPO = "certificado_digital";

    private final GenericoDocumentoService documentoService;

    /**
     * Le o certificado do disco e devolve os bytes, sem gravar nada.
     * Usado para assinar a mensagem sem persistir a credencial.
     */
    public byte[] lerCertificado(String caminho, String senha) throws IOException {
        Path p = validarCaminho(caminho);
        byte[] conteudo = Files.readAllBytes(p);

        // valida que abre de verdade antes de o chamador tentar assinar
        validarSenha(p, conteudo, senha);

        return conteudo;
    }

    /**
     * Le e, se a pessoa escolher, grava no MongoDB dentro do modulo fiscal.
     *
     * @param salvarSemGravar false devolve os bytes e nao persiste a credencial
     */
    public Resultado carregarEGuardar(Long empresaId, String caminho, String senha, boolean salvarSemGravar)
            throws IOException {

        Path p = validarCaminho(caminho);
        byte[] conteudo = Files.readAllBytes(p);
        validarSenha(p, conteudo, senha);

        if (!salvarSemGravar) {
            return new Resultado(conteudo, null, false);
        }

        try {
            DocumentoArquivo doc = documentoService.salvarBytes(
                    empresaId, MODULO, TIPO, null,
                    p.getFileName().toString(),
                    "application/x-pkcs12", conteudo);

            return new Resultado(conteudo, doc, true);

        } catch (RuntimeException e) {
            throw new DocumentoNaoPersistidoException(
                    "Certificado valido, mas nao foi possivel gravar no banco. "
                            + "O arquivo original continua em " + p, p, e);
        }
    }

    /**
     * A senha do .pfx nunca vai para o banco junto do arquivo: quem abre o
     * certificado e guarda a senha na propria estacao. Persistir as duas
     * juntas transformaria o banco em um cofre de credencial sem separacao.
     */
    public List<DocumentoArquivo> certificadosSalvos(Long empresaId) {
        return documentoService.listarPorModulos(empresaId, Set.of(MODULO)).stream()
                .filter(d -> TIPO.equals(d.getTipoEntidade()))
                .toList();
    }

    private Path validarCaminho(String caminho) throws IOException {
        if (caminho == null || caminho.isBlank()) {
            throw new IllegalArgumentException("Informe o caminho do certificado");
        }

        Path p;
        try {
            p = Paths.get(caminho).toAbsolutePath().normalize();
        } catch (Exception e) {
            throw new IllegalArgumentException("Caminho invalido: " + caminho);
        }

        if (!Files.isRegularFile(p)) {
            throw new IllegalArgumentException("Arquivo nao encontrado: " + p);
        }

        String nome = p.getFileName().toString().toLowerCase(Locale.ROOT);
        boolean extOk = EXTENSOES.stream().anyMatch(nome::endsWith);
        if (!extOk) {
            throw new IllegalArgumentException(
                    "Certificado deve ser .pfx ou .p12 (recebido: " + p.getFileName() + ")");
        }

        long tamanho = Files.size(p);
        if (tamanho > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("Certificado excede 10MB");
        }

        return p;
    }

    /**
     * Confere que a senha ABRE o PKCS#12.
     *
     * Nao basta checar o cabecalho do arquivo: o certificado da prefeitura
     * tem senha e o usuario pode nao saber. Sem esta validacao, o upload
     * "passava" e a falha aparecia so na hora de assinar a nota — depois de
     * todo o preenchimento. Aqui o erro sai na hora, dizendo o que fazer.
     */
    private void validarSenha(Path p, byte[] conteudo, String senha) {
        // PKCS#12 comeca com um SEQUENCE (tag 0x30) DER
        if (conteudo.length < 4 || (conteudo[0] & 0xFF) != 0x30) {
            throw new IllegalArgumentException(
                    "O arquivo nao parece um certificado PKCS#12 (.pfx/.p12). "
                            + "Se for .pem, converta antes: openssl pkcs12 -export ...");
        }

        if (senha == null || senha.isEmpty()) {
            throw new IllegalArgumentException(
                    "Informe a senha do certificado. Sem ela nao da para abrir o arquivo "
                            + "nem assinar a nota.");
        }

        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (ByteArrayInputStream in = new ByteArrayInputStream(conteudo)) {
                ks.load(in, senha.toCharArray());
            }
        } catch (IOException e) {
            // IOException do PKCS12 costuma ser senha errada/corrompido
            throw new IllegalArgumentException(
                    "Senha incorreta ou arquivo corrompido. "
                            + "Confira a senha do .pfx — sem ela o certificado nao abre.");
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(
                    "Nao foi possivel ler o certificado: " + e.getMessage());
        }

        log.info("Certificado {} validado ({} bytes, senha confere)", p.getFileName(), conteudo.length);
    }

    public record Resultado(byte[] conteudo, DocumentoArquivo documento, boolean gravado) {
    }
}
