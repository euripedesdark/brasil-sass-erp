package br.com.brasil_saas.estoque.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name="bc_est_transferencia_item", schema="brasil_saas")
@Getter @Setter @NoArgsConstructor
public class TransferenciaEstoqueItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private Long transferenciaId;
    @Column(nullable=false) private Long produtoId;
    @Column(name="lote_id") private Long loteId;
    @Column(name="endereco_origem_id") private Long enderecoOrigemId;
    @Column(name="endereco_destino_id") private Long enderecoDestinoId;
    @Column(nullable=false, precision=15, scale=3) private BigDecimal quantidade;
}