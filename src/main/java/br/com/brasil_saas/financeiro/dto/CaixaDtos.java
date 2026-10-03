package br.com.brasil_saas.financeiro.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CaixaDtos {

    public record CaixaRequest(
            @NotBlank(message = "Informe o nome do caixa")
            @Size(max = 100, message = "Nome deve ter ate 100 caracteres")
            String nome,

            @NotNull(message = "Informe o saldo")
            @DecimalMin(value = "-9999999999999.99", message = "Saldo invalido")
            BigDecimal saldo,

            @Size(max = 20)
            String status) {}

    public record CaixaResponse(
            Long id,
            String nome,
            BigDecimal saldo,
            String status,
            boolean ativo) {}
}
