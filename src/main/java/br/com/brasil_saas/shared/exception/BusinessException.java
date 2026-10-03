package br.com.brasil_saas.shared.exception;

import lombok.Getter;

/**
 * Violação de regra de negócio (HTTP 422).
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
    }

    public BusinessException(String message, String code) {
        super(message);
        this.code = code;
    }
}
