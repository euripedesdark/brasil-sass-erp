package br.com.brasil_saas.producao.model;

import br.com.brasil_saas.shared.model.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bc_prod_romaneio_item", schema = "brasil_saas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RomaneioProducaoItem extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "romaneio_id", nullable = false)
    private RomaneioProducao romaneio;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "descricao", length = 200)
    private String descricao;

    @Column(name = "quantidade", nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "unidade_medida", nullable = false, length = 20)
    private String unidadeMedida;

    @Column(name = "lote", length = 100)
    private String lote;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;
}
