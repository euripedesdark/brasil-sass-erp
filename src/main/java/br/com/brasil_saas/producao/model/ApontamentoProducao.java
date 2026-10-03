package br.com.brasil_saas.producao.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_prod_apontamento", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ApontamentoProducao extends TenantEntity {

    // As voltas para producao e itemProducao ficam fora do JSON. Lazy e
    // bidirecional ao mesmo tempo: a sessao ja fecha quando o Jackson serializa,
    // e Producao ainda tem itens de volta — o mesmo par que fazia a listagem de
    // producao estourar. Os ids saem pelos getters abaixo, que e o que a tela
    // precisa de verdade.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producao_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Producao producao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_producao_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private ItemProducao itemProducao;

    // Somente leitura da propria coluna, para nao precisar do proxy. Ler
    // getId() do proxy pode disparar a inicializacao — que e exatamente o que
    // estoura aqui — entao o id vem da FK, que e uma coluna e nao uma relacao.
    @Column(name = "producao_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonProperty("producaoId")
    private Long producaoIdRef;

    @Column(name = "item_producao_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonProperty("itemProducaoId")
    private Long itemProducaoIdRef;

    @Column(name = "funcionario_id")
    private Long funcionarioId;

    @Column(name = "data_apontamento")
    private LocalDateTime dataApontamento = LocalDateTime.now();

    @Column(name = "horas_trabalhadas", precision = 10, scale = 2)
    private BigDecimal horasTrabalhadas = BigDecimal.ZERO;

    @Column(name = "quantidade_produzida", precision = 15, scale = 4)
    private BigDecimal quantidadeProduzida = BigDecimal.ZERO;

    @Column(name = "quantidade_refugo", precision = 15, scale = 4)
    private BigDecimal quantidadeRefugo = BigDecimal.ZERO;

    @Column(name = "status", length = 20)
    private String status = "INICIADO"; // INICIADO, EM_ANDAMENTO, FINALIZADO, CANCELADO

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "maquina_equipamento_id")
    private Long maquinaEquipamentoId;

    @Column(name = "turno", length = 50)
    private String turno;
}
