package br.com.brasil_saas.compras.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bc_com_pedido", schema = "brasil_saas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoCompra extends TenantEntity {

    @Column(name = "fornecedor_id", nullable = false)
    private Long fornecedorId;

    @Column(name = "numero", length = 20)
    private String numero;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "condicao_pagamento_id")
    private Long condicaoPagamentoId;

    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @Column(name = "data_previsao_entrega")
    private LocalDate dataPrevisaoEntrega;

    @Column(name = "valor_produtos", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorProdutos = BigDecimal.ZERO;

    @Column(name = "valor_desconto", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(name = "valor_frete", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorFrete = BigDecimal.ZERO;

    @Column(name = "valor_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "titulo_id")
    private Long tituloId;

    @Column(name = "contrato_id")
    private Long contratoId;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroItem ASC")
    private List<ItemPedidoCompra> itens = new ArrayList<>();
}
