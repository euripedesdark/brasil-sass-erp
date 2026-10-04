package br.com.brasil_saas.dms.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="bc_dms_aprovacao", schema="brasil_saas") @Getter @Setter
public class DmsAprovacao extends TenantEntity {
    @Column(name="documento_id", nullable=false) private Long documentoId;
    @Column(nullable=false) private Integer versao;
    @Column(length=200) private String aprovador;
    @Column(nullable=false, length=20) private String status = "PENDENTE";
    @Column(name="decidido_em") private LocalDateTime decididoEm;
    @Column(name="decidido_por") private Long decididoPor;
    @Column(columnDefinition="text") private String comentario;
}
