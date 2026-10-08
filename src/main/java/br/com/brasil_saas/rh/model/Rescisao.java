package br.com.brasil_saas.rh.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bc_rh_rescisao", schema = "brasil_saas")
@Getter
@Setter
public class Rescisao extends TenantEntity {

    @Column(name = "funcionario_id", nullable = false)
    private Long funcionarioId;

    @Column(name = "data_desligamento", nullable = false)
    private LocalDate dataDesligamento;

    @Column(nullable = false, length = 40)
    private String motivo;

    @Column(name = "meses_trabalhados")
    private Integer mesesTrabalhados;

    @Column(name = "saldo_salario", precision = 15, scale = 2, nullable = false)
    private BigDecimal saldoSalario = BigDecimal.ZERO;

    @Column(name = "decimo_terceiro", precision = 15, scale = 2, nullable = false)
    private BigDecimal decimoTerceiro = BigDecimal.ZERO;

    @Column(name = "ferias", precision = 15, scale = 2, nullable = false)
    private BigDecimal ferias = BigDecimal.ZERO;

    @Column(name = "terco_ferias", precision = 15, scale = 2, nullable = false)
    private BigDecimal tercoFerias = BigDecimal.ZERO;

    @Column(name = "aviso_previo", precision = 15, scale = 2, nullable = false)
    private BigDecimal avisoPrevio = BigDecimal.ZERO;

    @Column(name = "multa_40", precision = 15, scale = 2, nullable = false)
    private BigDecimal multa40 = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    private String status = "EFETIVADA";
}
