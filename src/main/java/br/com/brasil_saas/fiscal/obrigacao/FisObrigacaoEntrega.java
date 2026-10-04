package br.com.brasil_saas.fiscal.obrigacao;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="bc_fis_obrigacao_entrega", schema="brasil_saas") @Getter @Setter
public class FisObrigacaoEntrega extends TenantEntity {
    @Column(name="obrigacao_id", nullable=false) private Long obrigacaoId;
    @Column(nullable=false, length=7) private String competencia;
    @Column(nullable=false, length=20) private String status = "PENDENTE";
    @Column(name="entregue_em") private LocalDateTime entregueEm;
    @Column(length=200) private String protocolo;
}
