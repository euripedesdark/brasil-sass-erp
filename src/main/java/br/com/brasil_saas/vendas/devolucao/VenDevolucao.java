package br.com.brasil_saas.vendas.devolucao;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="bc_ven_devolucao", schema="brasil_saas") @Getter @Setter
public class VenDevolucao extends TenantEntity {
    @Column(name="pedido_id", nullable=false) private Long pedidoId;
    @Column(name="cliente_id") private Long clienteId;
    @Column(length=30) private String numero;
    @Column(nullable=false, length=100) private String motivo;
    @Column(nullable=false, length=20) private String status = "SOLICITADA";
    @Column(name="decidida_por") private Long decididaPor;
    @Column(name="decidida_em") private LocalDateTime decididaEm;
    @Column(name="recebida_em") private LocalDateTime recebidaEm;
}
