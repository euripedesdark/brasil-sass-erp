package br.com.brasil_saas.producao.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bc_prod_ordem", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Producao extends TenantEntity {

    @Column(name = "numero", nullable = false, unique = true)
    private String numero;

    @Column(name = "tipo_producao", length = 50)
    private String tipoProducao; // INDUSTRIA, PECUARIA, AGRICULTURA

    @Column(name = "status", nullable = false)
    private String status; // ABERTO, EM_PROCESSO, FINALIZADO, CANCELADO

    @Column(name = "produto_final_id", nullable = false)
    private Long produtoFinalId;

    @Column(name = "quantidade_planejada", precision = 15, scale = 4)
    private BigDecimal quantidadePlanejada;

    @Column(name = "unidade_medida", length = 20)
    private String unidadeMedida; // KG, L, UN, M3, etc.

    @Column(name = "densidade")
    private BigDecimal densidade;

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @Column(name = "custo_total", precision = 15, scale = 2)
    private BigDecimal custoTotal = BigDecimal.ZERO;

    @OneToMany(mappedBy = "producao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemProducao> itens = new ArrayList<>();
}
