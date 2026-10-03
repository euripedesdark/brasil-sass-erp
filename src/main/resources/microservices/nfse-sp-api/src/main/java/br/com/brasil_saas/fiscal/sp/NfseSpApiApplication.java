package br.com.brasil_saas.fiscal.sp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * API de NFS-e da Prefeitura de Sao Paulo.
 *
 * <p>Substitui o bridge em Ruby que existia em {@code nfse-sp-bridge}. A
 * diferenca que importa: o certificado A1 da ICP-Brasil foi emitido com
 * RC2-40-CBC, cifra que o OpenSSL 3 removeu. Em Ruby isso obrigava a carregar o
 * provider "legacy" e a definir {@code OPENSSL_MODULES} antes do load, e ainda
 * assim falhava em metade das vezes. O provider SUN do Java aceita, entao aqui
 * o certificado abre direto.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class NfseSpApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NfseSpApiApplication.class, args);
    }
}
