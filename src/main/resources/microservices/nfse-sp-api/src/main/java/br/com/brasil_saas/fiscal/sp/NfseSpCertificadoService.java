package br.com.brasil_saas.fiscal.sp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Enumeration;

/**
 * Carrega o certificado A1 e expoe a chave privada para assinar.
 *
 * <p><b>Por que a parte em Java e mais simples que a do bridge Ruby.</b> O
 * certificado da ICP-Brasil foi emitido com <b>RC2-40-CBC</b>, cifra que o
 * OpenSSL 3 removeu do provider default. Sofre com isso o Ruby/OpenSSL: precisa
 * do provider "legacy", da variavel {@code OPENSSL_MODULES} e mesmo assim falha
 * se a variavel nao estiver setada ANTES do load.
 *
 * <p>O provider SUN do Java ainda aceita RC2-40-CBC. Por isso a tela
 * /fiscal/certificados sempre funcionou com este mesmo arquivo enquanto o
 * microservico Ruby nao conseguia abrir.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NfseSpCertificadoService {

    private final NfseSpProperties properties;

    /** Chave privada + certificado, o par que toda assinatura precisa. */
    public record ParDeAssinatura(PrivateKey chavePrivada, X509Certificate certificado) {

        /** Alias dentro do PKCS#12, util para log. */
        public String alias() {
            return certificado == null ? "?" : certificado.getSubjectX500Principal().getName();
        }
    }

    /** Carrega o certificado configurado em {@code brasil-saas.fiscal.nfse-sp.*}. */
    public ParDeAssinatura carregar() {
        return carregarDoCaminho(properties.getCertificadoCaminho(), properties.getCertificadoSenha());
    }

    /**
     * Carrega um certificado de caminho e senha explicitos.
     *
     * <p>A senha nunca vem do banco: e informada pela estacao que esta emitindo.
     */
    public ParDeAssinatura carregarDoCaminho(String caminho, String senha) {
        if (caminho == null || caminho.isBlank()) {
            throw new NfseSpException(
                    "Configure o certificado A1 em brasil-saas.fiscal.nfse-sp.certificado-caminho. "
                            + "Sem ele a prefeitura nao aceita a nota: empresa LTDA exige A1.");
        }
        if (senha == null || senha.isBlank()) {
            throw new NfseSpException(
                    "Informe a senha do certificado (NFSE_SP_CERT_PASS). "
                            + "Sem ela nao da para abrir o .pfx nem assinar.");
        }

        Path p = caminhoValido(caminho);

        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            try (InputStream in = Files.newInputStream(p)) {
                ks.load(in, senha.toCharArray());
            }

            String alias = primeiroAliasComChave(ks);
            if (alias == null) {
                throw new NfseSpException(
                        "O PKCS#12 " + p.getFileName() + " nao tem nenhuma chave privada. "
                                + "Confirme que o arquivo e o certificado A1 e nao so o .cer/.crt.");
            }

            PrivateKey chave = (PrivateKey) ks.getKey(alias, senha.toCharArray());
            X509Certificate certificado = (X509Certificate) ks.getCertificate(alias);

            if (chave == null || certificado == null) {
                throw new NfseSpException(
                        "Nao foi possivel extrair a chave privada de " + p.getFileName() + ".");
            }
            if (!valido(certificado)) {
                // Nao bloqueia aqui: a prefeitura daria um erro mais claro, e
                // assim o usuario descobre o problema antes de montar a nota.
                log.warn("Certificado {} esta fora da validade (ate {}). A prefeitura deve recusar.",
                        certificado.getSubjectX500Principal(), certificado.getNotAfter());
            }

            log.debug("Certificado A1 carregado: {} (validade {})",
                    certificado.getSubjectX500Principal(), certificado.getNotAfter());
            return new ParDeAssinatura(chave, certificado);

        } catch (NfseSpException e) {
            throw e;
        } catch (Exception e) {
            // Senha errada e o caso comum e o PKCS12 estoura com IOException
            // sem mensagem util.
            throw new NfseSpException(
                    "Nao foi possivel abrir o certificado " + p.getFileName()
                            + ". Cheque a senha e se o arquivo e um PKCS#12 valido. Detalhe: "
                            + e.getMessage(), e);
        }
    }

    /** Certificado em base64, sem cabecalho PEM nem quebras de linha. */
    public String certificadoBase64(X509Certificate certificado) {
        try {
            return Base64.getEncoder().encodeToString(certificado.getEncoded());
        } catch (java.security.cert.CertificateEncodingException e) {
            throw new NfseSpException("Nao foi possivel codificar o certificado em base64.", e);
        }
    }

    private Path caminhoValido(String caminho) {
        Path p;
        try {
            p = Paths.get(caminho).toAbsolutePath().normalize();
        } catch (Exception e) {
            throw new NfseSpException("Caminho de certificado invalido: " + caminho, e);
        }
        if (!Files.isRegularFile(p)) {
            throw new NfseSpException("Certificado nao encontrado: " + p);
        }
        return p;
    }

    /** {@code X509Certificate} nao tem isValid(); a checagem e por data. */
    private boolean valido(X509Certificate certificado) {
        try {
            certificado.checkValidity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String primeiroAliasComChave(KeyStore ks) throws Exception {        Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (ks.isKeyEntry(alias)) {
                return alias;
            }
        }
        return null;
    }
}
