package br.com.brasil_saas.rh.model;

import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "bc_rh_folha_item", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor
public class ItemFolhaPagamento extends AuditableEntity {
    // FolhaPagamento tem itens de volta: sem o @JsonIgnore o Jackson entra em
    // laco e a folha volta como 200 com JSON truncado.
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "folha_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private FolhaPagamento folha;

    @Column(name = "folha_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonProperty("folhaId")
    private Long folhaIdRef;
    @Column(name = "funcionario_id", nullable = false) private Long funcionarioId;
    @Column(nullable = false) private String tipo;
    @Column(length = 200) private String descricao;
    @Column(nullable = false, precision = 15, scale = 2) private BigDecimal valor;
}
