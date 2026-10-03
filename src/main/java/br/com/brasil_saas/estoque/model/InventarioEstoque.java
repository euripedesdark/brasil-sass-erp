package br.com.brasil_saas.estoque.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name="bc_est_inventario", schema="brasil_saas")
@Getter @Setter @NoArgsConstructor
public class InventarioEstoque extends TenantEntity {
    @Column(name="deposito_id", nullable=false) private Long depositoId;
    @Column(nullable=false, length=20) private String status = "ABERTO";
    @Column(name="data_contagem", nullable=false) private LocalDateTime dataContagem = LocalDateTime.now();
    @Column(columnDefinition="TEXT") private String observacoes;
}