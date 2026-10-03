package br.com.brasil_saas.rh.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity @Table(name="bc_rh_funcionario", schema="brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Funcionario extends TenantEntity {
    @Column(name="pessoa_id", nullable=false) private Long pessoaId;
    @Column(name="usuario_id") private Long usuarioId;
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="cargo_id") private Cargo cargo;
    @Column(length=30) private String matricula;
    @Column(name="data_admissao") private LocalDate dataAdmissao;
    @Column(name="data_demissao") private LocalDate dataDemissao;
    @Column(precision=15, scale=2) private BigDecimal salario = BigDecimal.ZERO;
    @Column(nullable=false) private Boolean ativo = true;

    @Column(name = "tipo_colaborador", length = 30)
    private String tipoColaborador; // VENDEDOR, TECNICO, PRESTADOR

    @Column(name = "percentual_comissao", precision = 5, scale = 2)
    private BigDecimal percentualComissao = BigDecimal.ZERO;

    @Column(name = "valor_hora", precision = 15, scale = 2)
    private BigDecimal valorHora = BigDecimal.ZERO;

    @Column(name = "foto_url", columnDefinition = "TEXT")
    private String fotoUrl;
    
    @Column(name = "foto_tipo_conteudo", length = 50)
    private String fotoTipoConteudo;
    
    @Column(name = "foto_tamanho")
    private Long fotoTamanho;
}
