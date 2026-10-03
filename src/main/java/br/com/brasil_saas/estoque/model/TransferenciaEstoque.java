package br.com.brasil_saas.estoque.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name="bc_est_transferencia", schema="brasil_saas")
@Getter @Setter @NoArgsConstructor
public class TransferenciaEstoque extends TenantEntity {
    @Column(name="deposito_origem_id", nullable=false) private Long depositoOrigemId;
    @Column(name="deposito_destino_id", nullable=false) private Long depositoDestinoId;
    @Column(nullable=false, length=20) private String status = "CONCLUIDA";
    @Column(name="data_transferencia", nullable=false) private LocalDateTime dataTransferencia = LocalDateTime.now();
    @Column(columnDefinition="TEXT") private String observacoes;
}