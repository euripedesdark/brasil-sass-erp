package br.com.brasil_saas.producao.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "bc_prod_item", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ItemProducao extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producao_id", nullable = false)
    // Sem isto o Jackson percorre producao -> itens -> producao -> itens e a
    // resposta morre em "Document nesting depth (1001) exceeds the maximum
    // allowed (1000)". O status ja vinha 200 porque os cabecalhos tinham saido
    // antes da falha: a tela recebia JSON cortado sem nenhuma mensagem.
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Producao producao;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "quantidade", precision = 15, scale = 4, nullable = false)
    private BigDecimal quantidade;

    @Column(name = "custo_unitario", precision = 15, scale = 2)
    private BigDecimal custoUnitario;

    @Column(name = "custo_total", precision = 15, scale = 2)
    private BigDecimal custoTotal;
}
