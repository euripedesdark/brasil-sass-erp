package br.com.brasil_saas.vendas.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bc_ven_tabela_preco_item", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TabelaPrecoItem extends TenantEntity {
    @Column(name="tabela_preco_id", nullable=false) private Long tabelaPrecoId;
    @Column(name="produto_id", nullable=false) private Long produtoId;
    @Column(nullable=false, precision=15, scale=4) private BigDecimal preco;
    @Column(name="preco_minimo", precision=15, scale=4) private BigDecimal precoMinimo;
    @Column(name="percentual_desconto_maximo", precision=7, scale=4, nullable=false)
    private BigDecimal percentualDescontoMaximo = BigDecimal.ZERO;
    @Column(name="vigencia_inicio") private LocalDate vigenciaInicio;
    @Column(name="vigencia_fim") private LocalDate vigenciaFim;
    @Column(nullable=false) private Boolean ativo = true;
}