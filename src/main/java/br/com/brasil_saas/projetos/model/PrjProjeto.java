package br.com.brasil_saas.projetos.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity @Table(name="bc_prj_projeto", schema="brasil_saas") @Getter @Setter
public class PrjProjeto extends TenantEntity {
    @Column(nullable=false, length=60) private String codigo;
    @Column(nullable=false, length=200) private String nome;
    @Column(columnDefinition="text") private String descricao;
    @Column(nullable=false, length=20) private String status = "PLANEJADO";
    @Column(length=200) private String gerente;
    @Column(name="data_inicio") private LocalDate dataInicio;
    @Column(name="data_fim_prevista") private LocalDate dataFimPrevista;
    @Column(name="data_fim_real") private LocalDate dataFimReal;
    @Column(name="orcamento_total", precision=15, scale=2, nullable=false) private BigDecimal orcamentoTotal = BigDecimal.ZERO;
}
