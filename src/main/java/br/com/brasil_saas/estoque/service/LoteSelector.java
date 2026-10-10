package br.com.brasil_saas.estoque.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Escolhe de quais lotes uma saida/reserva deve consumir.
 *
 * <p>FEFO: primeiro o que vence primeiro (lote sem validade vai por ultimo).
 * FIFO: primeiro o lote mais antigo (data de fabricacao; id como desempate,
 * ja que o id cresce na ordem de entrada).
 *
 * <p>Somente lotes ATIVO, com saldo livre e nao vencidos entram. Quarentena,
 * bloqueado e vencido nunca sao sugeridos. Classe pura: sem acesso a banco,
 * para o chamador travar (PESSIMISTIC_WRITE) e gravar as alocacoes.
 */
public final class LoteSelector {

    public enum Estrategia { FEFO, FIFO }

    public record LoteDisponivel(Long id, String status, LocalDate validade,
                                 LocalDate fabricacao, BigDecimal disponivel) {}

    public record Alocacao(Long loteId, BigDecimal quantidade) {}

    public record Resultado(List<Alocacao> alocacoes, BigDecimal faltante) {
        public boolean completo() { return faltante.signum() == 0; }
    }

    private LoteSelector() {}

    public static Resultado selecionar(List<LoteDisponivel> lotes, BigDecimal necessario,
                                       Estrategia estrategia, LocalDate hoje) {
        if (necessario == null || necessario.signum() < 0) {
            throw new IllegalArgumentException("Quantidade necessaria invalida");
        }
        Comparator<LoteDisponivel> ordem = estrategia == Estrategia.FIFO
                ? Comparator.comparing(LoteDisponivel::fabricacao, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(LoteDisponivel::id)
                : Comparator.comparing(LoteDisponivel::validade, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(LoteDisponivel::id);

        List<LoteDisponivel> aptos = new ArrayList<>();
        if (lotes != null) {
            for (LoteDisponivel l : lotes) {
                if (!"ATIVO".equalsIgnoreCase(l.status())) continue;
                if (l.disponivel() == null || l.disponivel().signum() <= 0) continue;
                if (l.validade() != null && l.validade().isBefore(hoje)) continue;
                aptos.add(l);
            }
        }
        aptos.sort(ordem);

        List<Alocacao> out = new ArrayList<>();
        BigDecimal restante = necessario;
        for (LoteDisponivel l : aptos) {
            if (restante.signum() == 0) break;
            BigDecimal tira = l.disponivel().min(restante);
            out.add(new Alocacao(l.id(), tira));
            restante = restante.subtract(tira);
        }
        return new Resultado(List.copyOf(out), restante);
    }
}
