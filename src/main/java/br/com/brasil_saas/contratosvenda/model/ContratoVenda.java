package br.com.brasil_saas.contratosvenda.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bc_ven_contrato", schema = "brasil_saas")
@Getter @Setter
public class ContratoVenda extends TenantEntity {
    @Column(length = 40, nullable = false)
    private String numero;
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;
    @Column(length = 200, nullable = false)
    private String titulo;
    @Column(length = 30, nullable = false)
    private String status = "RASCUNHO";
    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal valor = BigDecimal.ZERO;
    private LocalDate inicio;
    private LocalDate fim;
    @Column(name = "renovacao_auto", nullable = false)
    private Boolean renovacaoAuto = Boolean.FALSE;
    @Column(columnDefinition = "text")
    private String observacao;
}
