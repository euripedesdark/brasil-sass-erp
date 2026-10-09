package br.com.brasil_saas.compras.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ContratoFornecimentoRequest(
        Long empresaId, @NotNull Long fornecedorId, String numero, String tipo,
        @NotNull LocalDate vigenciaInicio, @NotNull LocalDate vigenciaFim,
        Long condicaoPagamentoId, BigDecimal valorLimite, String observacao,
        @Valid @NotEmpty List<Item> itens) {

    public record Item(Integer numeroItem, Long produtoId, String descricao, String unidade,
                       @NotNull BigDecimal quantidadeContratada, @NotNull BigDecimal valorUnitario) {}

    public ContratoFornecimentoRequest comEmpresaDa(Long empresa) {
        return new ContratoFornecimentoRequest(empresa, fornecedorId, numero, tipo, vigenciaInicio, vigenciaFim,
                condicaoPagamentoId, valorLimite, observacao, itens);
    }
}
