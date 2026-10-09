package br.com.brasil_saas.core.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "username é obrigatório")
    private String username;

    @NotBlank(message = "password é obrigatório")
    private String password;

    /** Provider do IAM: AD ou POSTGRES; DB é alias legado de POSTGRES. */
    private String provider = "AD";
}
