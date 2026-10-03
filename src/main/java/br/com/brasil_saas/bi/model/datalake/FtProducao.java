package br.com.brasil_saas.bi.model.datalake;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ft_producao", schema = "brasil_saas_dl")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FtProducao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "data_id", nullable = false)
    private Long dataId; // FK to dim_tempo

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId; // FK to dim_empresa

    @Column(name = "ordem_producao_id", nullable = false)
    private Long ordemProducaoId;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId; // FK to dim_produto

    @Column(name = "quantidade_produzida", nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidadeProduzida;

    @Column(name = "quantidade_prevista", precision = 15, scale = 4)
    private BigDecimal quantidadePrevista;

    @Column(name = "tempo_producao_minutos")
    private Integer tempoProducaoMinutos;

    @Column(name = "tempo_padrao_minutos")
    private Integer tempoPadraoMinutos;

    @Column(name = "custo_material", precision = 15, scale = 2)
    private BigDecimal custoMaterial;

    @Column(name = "custo_mao_obra", precision = 15, scale = 2)
    private BigDecimal custoMaoObra;

    @Column(name = "custo_indireto", precision = 15, scale = 2)
    private BigDecimal custoIndireto;

    @Column(name = "custo_total", precision = 15, scale = 2)
    private BigDecimal custoTotal;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // PLANEJADO, EM_PRODUCAO, CONCLUIDO, CANCELADO

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "centro_custo_id")
    private Long centroCustoId; // FK to dim_centro_custo

    @Column(name = "data_criacao")
    private LocalDate dataCriacao = LocalDate.now();
}
