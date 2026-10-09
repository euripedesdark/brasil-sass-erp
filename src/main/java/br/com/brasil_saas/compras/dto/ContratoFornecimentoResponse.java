package br.com.brasil_saas.compras.dto;

import br.com.brasil_saas.compras.model.ContratoFornecimento;
import br.com.brasil_saas.compras.model.ContratoFornecimentoItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ContratoFornecimentoResponse(Long id, String numero, String tipo, String status, Long fornecedorId,
        LocalDate vigenciaInicio, LocalDate vigenciaFim, BigDecimal valorLimite, BigDecimal valorLiberado,
        BigDecimal saldoValor, String observacao, List<Item> itens) {

    public static ContratoFornecimentoResponse from(ContratoFornecimento c) {
        BigDecimal saldo = c.getValorLimite() == null ? null : c.getValorLimite().subtract(c.getValorLiberado());
        return new ContratoFornecimentoResponse(c.getId(), c.getNumero(), c.getTipo(), c.getStatus(), c.getFornecedorId(),
                c.getVigenciaInicio(), c.getVigenciaFim(), c.getValorLimite(), c.getValorLiberado(), saldo,
                c.getObservacao(), c.getItens().stream().map(Item::from).toList());
    }

    public record Item(Long id, Integer numeroItem, Long produtoId, String descricao, String unidade,
                       BigDecimal quantidadeContratada, BigDecimal quantidadeLiberada, BigDecimal saldo,
                       BigDecimal valorUnitario) {
        static Item from(ContratoFornecimentoItem i) {
            return new Item(i.getId(), i.getNumeroItem(), i.getProdutoId(), i.getDescricao(), i.getUnidade(),
                    i.getQuantidadeContratada(), i.getQuantidadeLiberada(), i.saldo(), i.getValorUnitario());
        }
    }
}
