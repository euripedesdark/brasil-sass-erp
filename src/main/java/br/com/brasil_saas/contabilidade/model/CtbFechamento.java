package br.com.brasil_saas.contabilidade.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="bc_ctb_fechamento", schema="brasil_saas") @Getter @Setter
public class CtbFechamento extends TenantEntity {
    @Column(nullable=false, length=7) private String periodo;
    @Column(nullable=false, length=20) private String status = "FECHADO";
    @Column(name="fechado_por") private Long fechadoPor;
    @Column(name="fechado_em") private LocalDateTime fechadoEm;
}
