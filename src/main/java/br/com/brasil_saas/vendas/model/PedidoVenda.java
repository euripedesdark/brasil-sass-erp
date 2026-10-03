package br.com.brasil_saas.vendas.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bc_ven_pedido", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PedidoVenda extends TenantEntity {

    @Column(name = "numero")
    private String numero;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "vendedor_id")
    private Long vendedorId;

    @Column(name = "condicao_pagamento_id")
    private Long condicaoPagamentoId;

    @Column(name = "tabela_preco_id")
    private Long tabelaPrecoId;

    @Column(name = "percentual_desconto", precision = 7, scale = 4)
    private BigDecimal percentualDesconto = BigDecimal.ZERO;

    @Column(name = "canal_venda", length = 30)
    private String canalVenda;

    @Column(name = "origem", length = 30)
    private String origem;

    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @Column(name = "data_entrega")
    private LocalDate dataEntrega;

    @Column(name = "valor_produtos", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorProdutos = BigDecimal.ZERO;

    @Column(name = "valor_servicos", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorServicos = BigDecimal.ZERO;

    @Column(name = "valor_desconto", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(name = "valor_frete", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorFrete = BigDecimal.ZERO;

    @Column(name = "valor_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "titulo_id")
    private Long tituloId;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroItem ASC")
    private List<ItemPedidoVenda> itens = new ArrayList<>();
}