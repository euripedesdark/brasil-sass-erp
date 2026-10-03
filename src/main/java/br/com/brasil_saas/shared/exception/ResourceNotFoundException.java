package br.com.brasil_saas.shared.exception;

/**
 * Exceção lançada quando um recurso não é encontrado (HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String code;

    public ResourceNotFoundException(String message) {
        super(message);
        this.code = "NOT_FOUND";
    }

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " " + id + " not found");
        this.code = "NOT_FOUND";
    }

    public String getCode() {
        return code;
    }
}

