package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Table(name="bc_fin_renegociacao", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class Renegociacao extends AuditableEntity {
    @Column(name="titulo_original_id", nullable=false) private Long tituloOriginalId;
    @Column(name="novo_titulo_id") private Long novoTituloId;
    @Column(name="data_renegociacao", nullable=false) private LocalDate dataRenegociacao;
    @Column(name="valor_acrescimo", precision=15, scale=2) private BigDecimal valorAcrescimo = BigDecimal.ZERO;
    @Column(columnDefinition="text") private String observacao;
}
