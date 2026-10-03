package br.com.brasil_saas.estoque.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name="bc_est_inventario_item", schema="brasil_saas")
@Getter @Setter @NoArgsConstructor
public class InventarioEstoqueItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private Long inventarioId;
    @Column(nullable=false) private Long produtoId;
    @Column(name="lote_id") private Long loteId;
    @Column(name="endereco_id") private Long enderecoId;
    @Column(name="quantidade_sistema", nullable=false, precision=15, scale=3) private BigDecimal quantidadeSistema=BigDecimal.ZERO;
    @Column(name="quantidade_contada", nullable=false, precision=15, scale=3) private BigDecimal quantidadeContada=BigDecimal.ZERO;
    @Column(nullable=false, precision=15, scale=3) private BigDecimal diferenca=BigDecimal.ZERO;
    @Column(columnDefinition="TEXT") private String observacao;
}