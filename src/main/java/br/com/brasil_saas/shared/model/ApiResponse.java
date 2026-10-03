package br.com.brasil_saas.shared.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Wrapper padrão de resposta: { data, errors, meta }.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private T data;
    private List<ApiError> errors;
    private Meta meta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiError {
        private String field;
        private String message;
        private String code;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private LocalDateTime timestamp;
        private String path;
        private Integer statusCode;
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .data(data)
                .meta(Meta.builder().timestamp(LocalDateTime.now()).build())
                .build();
    }

    public static ApiResponse<Void> error(List<ApiError> errors, String path, int statusCode) {
        return ApiResponse.<Void>builder()
                .errors(errors)
                .meta(Meta.builder()
                        .timestamp(LocalDateTime.now())
                        .path(path)
                        .statusCode(statusCode)
                        .build())
                .build();
    }
}
