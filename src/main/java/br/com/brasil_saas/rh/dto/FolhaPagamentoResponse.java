package br.com.brasil_saas.rh.dto;

import br.com.brasil_saas.rh.model.FolhaPagamento;
import br.com.brasil_saas.rh.model.ItemFolhaPagamento;
import java.math.BigDecimal;
import java.util.List;

public record FolhaPagamentoResponse(Long id, String competencia, String status, BigDecimal valorTotal,
                                     Long tituloId, List<Item> itens) {
    public static FolhaPagamentoResponse from(FolhaPagamento folha) {
        return new FolhaPagamentoResponse(folha.getId(), folha.getCompetencia(), folha.getStatus(),
                folha.getValorTotal(), folha.getTituloId(), folha.getItens().stream().map(Item::from).toList());
    }
    public record Item(Long id, Long funcionarioId, String tipo, String descricao, BigDecimal valor) {
        static Item from(ItemFolhaPagamento item) {
            return new Item(item.getId(), item.getFuncionarioId(), item.getTipo(), item.getDescricao(), item.getValor());
        }
    }
}
