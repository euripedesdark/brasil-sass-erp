package br.com.brasil_saas.cadastro.dto;

import br.com.brasil_saas.cadastro.model.Endereco;
import java.math.BigDecimal;

public record EnderecoResponse(
    Long id, String tipo, String logradouro, String numero, String complemento,
    String bairro, String cep, Long municipioId, String uf,
    BigDecimal latitude, BigDecimal longitude, Boolean principal
) {
    public static EnderecoResponse from(Endereco e) {
        return new EnderecoResponse(e.getId(), e.getTipo(), e.getLogradouro(), e.getNumero(),
            e.getComplemento(), e.getBairro(), e.getCep(),
            e.getMunicipio() != null ? e.getMunicipio().getId() : null,
            e.getUf(), e.getLatitude(), e.getLongitude(), e.getPrincipal());
    }
}
