package br.com.brasil_saas.core.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "username é obrigatório")
    private String username;

    @NotBlank(message = "password é obrigatório")
    private String password;

    /** Fonte de identidade: AD (Managed) ou DB (Unmanaged). */
    private String provider = "AD";
}
