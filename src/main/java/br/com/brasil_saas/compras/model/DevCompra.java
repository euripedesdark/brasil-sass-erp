package br.com.brasil_saas.compras.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="bc_cmp_devolucao", schema="brasil_saas") @Getter @Setter
public class DevCompra extends TenantEntity {
    @Column(name="pedido_id", nullable=false) private Long pedidoId;
    @Column(nullable=false, length=500) private String motivo;
    @Column(nullable=false, length=20) private String status = "SOLICITADA";
    @Column(name="devolvida_em") private LocalDateTime devolvidaEm;
    @Column(length=500) private String observacao;
}
