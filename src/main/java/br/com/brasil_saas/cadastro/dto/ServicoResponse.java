package br.com.brasil_saas.cadastro.dto;

import br.com.brasil_saas.cadastro.model.Servico;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServicoResponse(
    Long id, String uuid, String codigo, String nome, String descricao,
    String lc116Codigo, String codigoTributacaoMunicipal, String nbs,
    BigDecimal aliquotaIss, BigDecimal preco,
    String unidadeSigla, Boolean ativo, LocalDateTime createdAt, LocalDateTime updatedAt
) {
    public static ServicoResponse from(Servico s) {
        return new ServicoResponse(s.getId(), s.getUuid() != null ? s.getUuid().toString() : null,
            s.getCodigo(), s.getNome(), s.getDescricao(),
            s.getLc116Codigo(), s.getCodigoTributacaoMunicipal(), s.getNbs(),
            s.getAliquotaIss(), s.getPreco(),
            s.getUnidadeMedida() != null ? s.getUnidadeMedida().getSigla() : null,
            s.getAtivo(), s.getCreatedAt(), s.getUpdatedAt());
    }
}
