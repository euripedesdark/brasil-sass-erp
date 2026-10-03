package br.com.brasil_saas.shared.web;

/**
 * Generic simple API response wrapper used by controllers.
 */
public final class ApiResponse<T> {

    private final boolean success;
    private final String code;
    private final T data;

    private ApiResponse(boolean success, String code, T data) {
        this.success = success;
        this.code = code;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCode() {
        return code;
    }

    public T getData() {
        return data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "OK", data);
    }

    public static <T> ApiResponse<T> error(String code, T data) {
        return new ApiResponse<>(false, code, data);
    }
}

