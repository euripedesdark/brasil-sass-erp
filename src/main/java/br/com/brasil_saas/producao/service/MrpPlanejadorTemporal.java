package br.com.brasil_saas.producao.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Planejamento de necessidades com tempo (lead time), estoque de seguranca,
 * lote minimo e multiplo de lote. Classe pura.
 *
 * <p>Percorre as datas com movimento em ordem. Quando o saldo projetado fica
 * abaixo do estoque de seguranca, planeja uma ordem para a data da falta, com
 * quantidade = max(falta, loteMinimo) arredondada para cima ao multiplo. A
 * liberacao e a data da falta menos o lead time; se isso cair antes de hoje,
 * a ordem e marcada como atrasada e liberada hoje (nao se planeja no passado).
 */
public final class MrpPlanejadorTemporal {

    public record Parametros(int leadTimeDias, BigDecimal estoqueSeguranca,
                             BigDecimal loteMinimo, BigDecimal multiplo) {
        public Parametros {
            if (leadTimeDias < 0) throw new IllegalArgumentException("Lead time negativo");
            estoqueSeguranca = estoqueSeguranca == null ? BigDecimal.ZERO : estoqueSeguranca;
            loteMinimo = loteMinimo == null ? BigDecimal.ZERO : loteMinimo;
            multiplo = multiplo == null ? BigDecimal.ZERO : multiplo;
        }
    }

    public record Evento(LocalDate data, BigDecimal quantidade) {}

    public record OrdemPlanejada(LocalDate liberacao, LocalDate necessidade,
                                 BigDecimal quantidade, boolean atrasada) {}

    private MrpPlanejadorTemporal() {}

    public static List<OrdemPlanejada> planejar(LocalDate hoje, BigDecimal estoqueInicial,
                                                List<Evento> necessidades, List<Evento> recebimentos,
                                                Parametros p) {
        TreeSet<LocalDate> datas = new TreeSet<>();
        for (Evento e : nz(necessidades)) datas.add(e.data());
        for (Evento e : nz(recebimentos)) datas.add(e.data());

        BigDecimal saldo = estoqueInicial == null ? BigDecimal.ZERO : estoqueInicial;
        List<OrdemPlanejada> ordens = new ArrayList<>();
        for (LocalDate d : datas) {
            saldo = saldo.add(soma(recebimentos, d)).subtract(soma(necessidades, d));
            if (saldo.compareTo(p.estoqueSeguranca()) < 0) {
                BigDecimal falta = p.estoqueSeguranca().subtract(saldo);
                BigDecimal qtd = falta.max(p.loteMinimo());
                if (p.multiplo().signum() > 0) {
                    qtd = qtd.divide(p.multiplo(), 0, RoundingMode.CEILING).multiply(p.multiplo());
                }
                LocalDate liberar = d.minusDays(p.leadTimeDias());
                boolean atrasada = liberar.isBefore(hoje);
                ordens.add(new OrdemPlanejada(atrasada ? hoje : liberar, d, qtd, atrasada));
                saldo = saldo.add(qtd);
            }
        }
        return ordens;
    }

    private static BigDecimal soma(List<Evento> l, LocalDate d) {
        BigDecimal s = BigDecimal.ZERO;
        for (Evento e : nz(l)) if (e.data().equals(d)) s = s.add(e.quantidade());
        return s;
    }

    private static List<Evento> nz(List<Evento> l) { return l == null ? List.of() : l; }
}
