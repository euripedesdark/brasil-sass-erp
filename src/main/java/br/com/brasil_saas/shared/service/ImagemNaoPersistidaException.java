package br.com.brasil_saas.shared.service;

import lombok.Getter;

import java.nio.file.Path;

/**
 * A imagem foi preservada em staging, mas nao chegou ao MongoDB.
 * Substitui a imagem antiga que segue no ar — nada se perde.
 */
@Getter
public class ImagemNaoPersistidaException extends RuntimeException {

    private final transient Path temporario;

    public ImagemNaoPersistidaException(String message, Path temporario, Throwable causa) {
        super(message, causa);
        this.temporario = temporario;
    }
}
