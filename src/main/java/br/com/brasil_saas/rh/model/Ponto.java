package br.com.brasil_saas.rh.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
@Entity @Table(name="bc_rh_ponto", schema="brasil_saas") @Getter @Setter
public class Ponto extends TenantEntity {
    @Column(name="funcionario_id", nullable=false) private Long funcionarioId;
    @Column(nullable=false) private LocalDate data;
    private LocalTime e1;
    private LocalTime s1;
    private LocalTime e2;
    private LocalTime s2;
    @Column(name="horas_trabalhadas", precision=5, scale=2, nullable=false) private BigDecimal horasTrabalhadas = BigDecimal.ZERO;
    @Column(nullable=false) private Boolean falta = false;
    @Column(length=500) private String observacao;
}
