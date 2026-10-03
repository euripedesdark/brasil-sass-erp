package br.com.brasil_saas.contabilidade.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.time.LocalDate;
@Entity @Table(name="bc_ctb_lancamento", schema="brasil_saas") @Getter @Setter
public class CtbLancamento extends TenantEntity {
    @Column(nullable=false) private LocalDate data;
    @Column(nullable=false, length=7) private String periodo;
    @Column(nullable=false, length=500) private String historico;
    @Column(name="origem_tipo", length=60) private String origemTipo;
    @Column(name="origem_id") private Long origemId;
    @Column(nullable=false, length=20) private String status = "RASCUNHO";
}
