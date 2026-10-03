package br.com.brasil_saas.fiscal.nacional;

/**
 * Falha na integracao com a NFS-e da Prefeitura de Sao Paulo.
 *
 * <p>Distinta de {@code IllegalArgumentException} de proposito: o handler global
 * tratava {@code IllegalArgumentException} como 500, o que escondia erro de
 * configuracao (certificado ausente, leiaute errado) como falha do servidor.
 */
public class NfseNacionalException extends RuntimeException {

    public NfseNacionalException(String message) {
        super(message);
    }

    public NfseNacionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
