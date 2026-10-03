package br.com.brasil_saas.fiscal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateService {

    /**
     * Carrega um certificado digital A1 (.pfx) a partir de um caminho e senha.
     *
     * @param path Caminho para o arquivo .pfx
     * @param password Senha do certificado
     * @return KeyStore contendo o certificado
     * @throws KeyStoreException Se houver erro ao carregar o KeyStore
     */
    public KeyStore loadCertificate(String path, String password) throws KeyStoreException {
        try {
            log.info("Carregando certificado digital a partir de: {}", path);
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(path)) {
                keyStore.load(fis, password.toCharArray());
            }
            return keyStore;
        } catch (IOException | CertificateException | NoSuchAlgorithmException e) {
            log.error("Erro ao carregar certificado digital em {}: {}", path, e.getMessage());
            throw new KeyStoreException("Falha ao carregar certificado digital: " + e.getMessage(), e);
        }
    }

    /**
     * Carrega a cadeia de certificados da SEFAZ (Cacerts).
     *
     * @param path Caminho para o arquivo .jks ou .cacerts
     * @param password Senha da cadeia (geralmente 'changeit' ou definida no sistema)
     * @return KeyStore contendo a cadeia de certificados
     */
    public KeyStore loadCadeiaCertificados(String path, String password) throws KeyStoreException {
        try {
            log.info("Carregando cadeia de certificados da SEFAZ em: {}", path);
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (FileInputStream fis = new FileInputStream(path)) {
                keyStore.load(fis, password.toCharArray());
            }
            return keyStore;
        } catch (IOException | CertificateException | NoSuchAlgorithmException e) {
            log.error("Erro ao carregar cadeia de certificados em {}: {}", path, e.getMessage());
            throw new KeyStoreException("Falha ao carregar cadeia de certificados: " + e.getMessage(), e);
        }
    }
}
