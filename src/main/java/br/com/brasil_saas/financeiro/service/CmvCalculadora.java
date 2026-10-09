package br.com.brasil_saas.financeiro.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Regras puras do CMV do CO-PA: escolhe a melhor fonte de custo e calcula o custo da linha. */
public final class CmvCalculadora {
    private CmvCalculadora() {}

    public static final BigDecimal PERCENTUAL_ESTIMADO = new BigDecimal("0.70");

    public enum Origem { MEDIO_COMPRAS, CADASTRO, ESTIMADO }

    public record Custo(BigDecimal valor, Origem origem) {
        public boolean real() { return origem != Origem.ESTIMADO; }
    }

    /**
     * Prioridade: custo médio ponderado das compras recebidas, depois preço de custo do cadastro.
     * Sem nenhum dos dois (ou zerado), cai no percentual estimado da receita da linha.
     */
    public static Custo custoLinha(BigDecimal quantidade, BigDecimal receitaLinha,
                                   BigDecimal medioCompras, BigDecimal precoCadastro) {
        BigDecimal qtd = quantidade == null ? BigDecimal.ZERO : quantidade;
        BigDecimal rec = receitaLinha == null ? BigDecimal.ZERO : receitaLinha;
        if (medioCompras != null && medioCompras.signum() > 0 && qtd.signum() > 0)
            return new Custo(qtd.multiply(medioCompras).setScale(2, RoundingMode.HALF_UP), Origem.MEDIO_COMPRAS);
        if (precoCadastro != null && precoCadastro.signum() > 0 && qtd.signum() > 0)
            return new Custo(qtd.multiply(precoCadastro).setScale(2, RoundingMode.HALF_UP), Origem.CADASTRO);
        return new Custo(rec.multiply(PERCENTUAL_ESTIMADO).setScale(2, RoundingMode.HALF_UP), Origem.ESTIMADO);
    }

    /** Percentual da receita cujo custo veio de fonte real (0 a 100). */
    public static BigDecimal cobertura(BigDecimal receitaComCustoReal, BigDecimal receitaTotal) {
        if (receitaTotal == null || receitaTotal.signum() == 0) return BigDecimal.ZERO;
        return receitaComCustoReal.multiply(new BigDecimal("100")).divide(receitaTotal, 2, RoundingMode.HALF_UP);
    }
}
