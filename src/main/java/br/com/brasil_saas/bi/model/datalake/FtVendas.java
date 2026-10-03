package br.com.brasil_saas.bi.model.datalake;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ft_vendas", schema = "brasil_saas_dl")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FtVendas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "data_id", nullable = false)
    private Long dataId; // FK to dim_tempo

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId; // FK to dim_empresa

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId; // FK to dim_cliente

    @Column(name = "vendedor_id")
    private Long vendedorId; // FK to dim_vendedor

    @Column(name = "produto_id", nullable = false)
    private Long produtoId; // FK to dim_produto

    @Column(name = "categoria_id")
    private Long categoriaId; // FK to dim_categoria

    @Column(name = "pedido_venda_id", nullable = false)
    private Long pedidoVendaId;

    @Column(name = "item_pedido_id")
    private Long itemPedidoId;

    @Column(name = "quantidade", nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorUnitario;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "valor_custo", precision = 15, scale = 2)
    private BigDecimal valorCusto;

    @Column(name = "margem_bruta", precision = 15, scale = 2)
    private BigDecimal margemBruta;

    @Column(name = "percentual_margem", precision = 10, scale = 2)
    private BigDecimal percentualMargem;

    @Column(name = "tipo_venda", nullable = false, length = 50)
    private String tipoVenda; // VENDA_DIRETA, ORCAMENTO, PEDIDO

    @Column(name = "status_pedido", nullable = false, length = 50)
    private String statusPedido; // ABERTO, CONFIRMADO, FATURADO, CANCELADO

    @Column(name = "data_pedido", nullable = false)
    private LocalDate dataPedido;

    @Column(name = "data_entrega")
    private LocalDate dataEntrega;

    @Column(name = "data_faturamento")
    private LocalDate dataFaturamento;

    @Column(name = "condicao_pagamento", length = 50)
    private String condicaoPagamento;

    @Column(name = "prazo_entrega")
    private Integer prazoEntrega;

    @Column(name = "frete", precision = 15, scale = 2)
    private BigDecimal frete;

    @Column(name = "desconto", precision = 15, scale = 2)
    private BigDecimal desconto;

    @Column(name = "impostos", precision = 15, scale = 2)
    private BigDecimal impostos;

    @Column(name = "data_criacao")
    private LocalDate dataCriacao = LocalDate.now();
}
