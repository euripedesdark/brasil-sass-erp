package br.com.brasil_saas.estoque.model;

import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name="bc_est_expedicao_item", schema="brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ExpedicaoEstoqueItem extends AuditableEntity {
    @Column(name="expedicao_id", nullable=false) private Long expedicaoId;
    @Column(name="reserva_id") private Long reservaId;
    @Column(name="produto_id", nullable=false) private Long produtoId;
    @Column(name="lote_id") private Long loteId;
    @Column(name="endereco_id") private Long enderecoId;
    @Column(nullable=false, precision=15, scale=3) private BigDecimal quantidade;
    @Column(nullable=false, length=20) private String status="PENDENTE";
}