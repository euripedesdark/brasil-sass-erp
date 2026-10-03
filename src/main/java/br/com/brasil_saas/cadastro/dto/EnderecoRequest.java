package br.com.brasil_saas.cadastro.dto;

import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record EnderecoRequest(
    @Size(max = 20) String tipo,
    @Size(max = 200) String logradouro,
    @Size(max = 20) String numero,
    @Size(max = 100) String complemento,
    @Size(max = 100) String bairro,
    @Size(max = 8) String cep,
    Long municipioId,
    @Size(max = 2) String uf,
    BigDecimal latitude,
    BigDecimal longitude,
    Boolean principal
) {}
