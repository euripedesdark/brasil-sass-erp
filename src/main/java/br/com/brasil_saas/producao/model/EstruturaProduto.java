package br.com.brasil_saas.producao.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bc_prod_estrutura", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EstruturaProduto extends TenantEntity {
    @Column(name = "produto_pai_id", nullable = false)
    private Long produtoPaiId;

    @Column(name = "produto_filho_id", nullable = false)
    private Long produtoFilhoId;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "perda_percentual", nullable = false, precision = 7, scale = 4)
    private BigDecimal perdaPercentual = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer nivel = 1;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(columnDefinition = "TEXT")
    private String observacao;
}
