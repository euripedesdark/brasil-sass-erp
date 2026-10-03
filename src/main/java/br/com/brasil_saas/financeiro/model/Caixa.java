package br.com.brasil_saas.financeiro.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Caixa: o cofre / conta de caixa do modulo financeiro.
 *
 * A tela existed desde o inicio e chamava /api/caixa, mas nao havia entidade,
 * tabela nem controller — nenhuma chamada dela encontrava rota, e o erro era
 * engolido por um alert, entao a tela ficava vazia sem explicacao.
 *
 * Escopo deliberado: cadastro e saldo. Fluxo de caixa projetado e outra
 * coisa (ProjecaoFluxoCaixa).
 */
@Entity
@Table(name = "bc_fin_caixa", schema = "brasil_saas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Caixa extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    /** ATIVO ou INATIVO. Caixa desativado continua no historico. */
    @Column(nullable = false, length = 20)
    private String status = "ATIVO";

    public boolean isAtivo() {
        return "ATIVO".equalsIgnoreCase(status);
    }
}
