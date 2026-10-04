package br.com.brasil_saas.dms.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.time.LocalDate;
@Entity @Table(name="bc_dms_documento", schema="brasil_saas") @Getter @Setter
public class DmsDocumento extends TenantEntity {
    @Column(nullable=false, length=60) private String codigo;
    @Column(nullable=false, length=300) private String titulo;
    @Column(length=100) private String categoria;
    @Column(name="entidade_tipo", length=60) private String entidadeTipo;
    @Column(name="entidade_id") private Long entidadeId;
    @Column(nullable=false, length=20) private String status = "RASCUNHO";
    @Column(name="versao_atual", nullable=false) private Integer versaoAtual = 1;
    @Column(name="reter_ate") private LocalDate reterAte;
}
