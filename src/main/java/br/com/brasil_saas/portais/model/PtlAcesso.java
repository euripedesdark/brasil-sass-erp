package br.com.brasil_saas.portais.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="bc_ptl_acesso", schema="brasil_saas") @Getter @Setter
public class PtlAcesso extends TenantEntity {
    @Column(nullable=false, length=20) private String tipo;
    @Column(name="pessoa_id", nullable=false) private Long pessoaId;
    @Column(nullable=false, length=64) private String token;
    @Column(name="expira_em", nullable=false) private LocalDateTime expiraEm;
    @Column(nullable=false) private Boolean ativo = true;
    @Column(name="ultimo_uso_em") private LocalDateTime ultimoUsoEm;
}
