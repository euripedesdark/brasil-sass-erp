package br.com.brasil_saas.shared.service;

import lombok.Getter;

import java.nio.file.Path;

/**
 * O arquivo foi preservado em staging, mas nao chegou ao MongoDB.
 * Carrega o caminho para que a camada de cima possa logar / expor a recuperacao.
 */
@Getter
public class DocumentoNaoPersistidoException extends RuntimeException {

    private final transient Path temporario;

    public DocumentoNaoPersistidoException(String message, Path temporario, Throwable causa) {
        super(message, causa);
        this.temporario = temporario;
    }
}
