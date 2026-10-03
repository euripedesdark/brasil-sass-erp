package br.com.brasil_saas.producao.service.impl;

import br.com.brasil_saas.producao.model.CentroTrabalho;
import br.com.brasil_saas.producao.model.OperacaoRoteiro;
import br.com.brasil_saas.producao.model.RoteiroProducao;
import br.com.brasil_saas.producao.repository.CentroTrabalhoRepository;
import br.com.brasil_saas.producao.repository.OperacaoRoteiroRepository;
import br.com.brasil_saas.producao.repository.RoteiroProducaoRepository;
import br.com.brasil_saas.producao.service.CapacidadeService;
import br.com.brasil_saas.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * Capacidade finita simplificada.
 *
 * Tempo da operacao = setup (uma vez por lote) + maquina_minutos * quantidade.
 * Operacoes rodam em sequencia; cada uma consome dias uteis (seg-sex) do seu
 * centro conforme a capacidade_horas_dia. A operacao seguinte comeca no dia em
 * que a anterior termina. Nao considera outras OPs ja alocadas no centro:
 * isso exige calendario de carga persistido, que ainda nao existe.
 */
@Service
@RequiredArgsConstructor
public class CapacidadeServiceImpl implements CapacidadeService {

    private static final BigDecimal SESSENTA = BigDecimal.valueOf(60);

    private final RoteiroProducaoRepository roteiros;
    private final OperacaoRoteiroRepository operacoes;
    private final CentroTrabalhoRepository centros;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> simular(Long empresaId, Request req) {
        if (req == null || req.produtoId() == null || req.quantidade() == null || req.quantidade().signum() <= 0) {
            throw new BusinessException("Produto e quantidade positiva são obrigatórios");
        }
        LocalDate inicio = proximoDiaUtil(req.dataInicio() == null ? LocalDate.now() : req.dataInicio());

        RoteiroProducao roteiro = roteiroVigente(empresaId, req.produtoId(), inicio);
        List<OperacaoRoteiro> ops = operacoes
                .findByEmpresaIdAndRoteiroIdAndDeletedAtIsNullOrderBySequenciaAsc(empresaId, roteiro.getId());

        Map<Long, CentroTrabalho> cache = new HashMap<>();
        List<Map<String, Object>> linhas = new ArrayList<>();
        Map<Long, BigDecimal> horasPorCentro = new LinkedHashMap<>();
        List<String> alertas = new ArrayList<>();

        LocalDate cursor = inicio;
        BigDecimal totalHoras = BigDecimal.ZERO;

        for (OperacaoRoteiro op : ops) {
            if (!Boolean.TRUE.equals(op.getAtivo())) continue;

            BigDecimal minutos = nz(op.getSetupMinutos())
                    .add(nz(op.getMaquinaMinutos()).multiply(req.quantidade()));
            BigDecimal horas = minutos.divide(SESSENTA, 4, RoundingMode.HALF_UP);

            CentroTrabalho ct = null;
            if (op.getCentroTrabalhoId() != null) {
                ct = cache.computeIfAbsent(op.getCentroTrabalhoId(), id ->
                        centros.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId).orElse(null));
            }
            if (ct == null) {
                alertas.add("Operação " + op.getCodigo() + " sem centro de trabalho válido: capacidade não validada.");
            } else if (!Boolean.TRUE.equals(ct.getAtivo())) {
                alertas.add("Centro de trabalho " + ct.getCodigo() + " está inativo.");
            }

            BigDecimal capDia = ct == null ? null : nz(ct.getCapacidadeHorasDia());
            if (capDia != null && capDia.signum() <= 0) {
                alertas.add("Centro " + ct.getCodigo() + " com capacidade diária zero.");
                capDia = null;
            }
            int dias = capDia == null ? 1
                    : Math.max(1, horas.divide(capDia, 0, RoundingMode.CEILING).intValue());

            LocalDate fim = somarDiasUteis(cursor, dias - 1);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("sequencia", op.getSequencia());
            row.put("codigo", op.getCodigo());
            row.put("operacao", op.getNome());
            row.put("centroTrabalhoId", ct == null ? null : ct.getId());
            row.put("centroTrabalho", ct == null ? null : ct.getCodigo());
            row.put("horas", horas);
            row.put("capacidadeHorasDia", capDia);
            row.put("dias", dias);
            row.put("inicio", cursor);
            row.put("fim", fim);
            linhas.add(row);

            totalHoras = totalHoras.add(horas);
            if (ct != null) horasPorCentro.merge(ct.getId(), horas, BigDecimal::add);
            cursor = fim;
        }

        if (linhas.isEmpty()) {
            throw new BusinessException("Roteiro " + roteiro.getCodigo() + " não possui operações ativas");
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("produtoId", req.produtoId());
        out.put("quantidade", req.quantidade());
        out.put("roteiroId", roteiro.getId());
        out.put("roteiro", roteiro.getCodigo());
        out.put("versao", roteiro.getVersao());
        out.put("operacoes", linhas);
        out.put("horasTotais", totalHoras);
        out.put("dataInicio", inicio);
        out.put("dataTermino", cursor);
        out.put("leadTimeDiasUteis", contarDiasUteis(inicio, cursor));
        out.put("horasPorCentro", horasPorCentro);
        out.put("alertas", alertas);
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> cargaPorCentro(Long empresaId, List<Request> pedidos) {
        Map<Long, BigDecimal> total = new LinkedHashMap<>();
        for (Request p : pedidos == null ? List.<Request>of() : pedidos) {
            @SuppressWarnings("unchecked")
            Map<Long, BigDecimal> m = (Map<Long, BigDecimal>) simular(empresaId, p).get("horasPorCentro");
            m.forEach((id, h) -> total.merge(id, h, BigDecimal::add));
        }
        List<Map<String, Object>> out = new ArrayList<>();
        total.forEach((id, horas) -> {
            CentroTrabalho ct = centros.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId).orElse(null);
            if (ct == null) return;
            BigDecimal cap = nz(ct.getCapacidadeHorasDia());
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("centroTrabalhoId", id);
            r.put("centroTrabalho", ct.getCodigo());
            r.put("horasNecessarias", horas);
            r.put("capacidadeHorasDia", cap);
            r.put("diasNecessarios", cap.signum() > 0 ? horas.divide(cap, 2, RoundingMode.HALF_UP) : null);
            out.add(r);
        });
        out.sort((a, b) -> {
            BigDecimal x = (BigDecimal) a.get("diasNecessarios"), y = (BigDecimal) b.get("diasNecessarios");
            if (x == null) return 1;
            if (y == null) return -1;
            return y.compareTo(x);
        });
        return out;
    }

    // ---------------------------------------------------------------- helpers

    private RoteiroProducao roteiroVigente(Long empresaId, Long produtoId, LocalDate data) {
        return roteiros.findByEmpresaIdAndProdutoIdAndDeletedAtIsNullOrderByVersaoDesc(empresaId, produtoId)
                .stream()
                .filter(r -> Boolean.TRUE.equals(r.getAtivo()))
                .filter(r -> r.getVigenciaInicio() == null || !r.getVigenciaInicio().isAfter(data))
                .filter(r -> r.getVigenciaFim() == null || !r.getVigenciaFim().isBefore(data))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "Produto " + produtoId + " não possui roteiro ativo e vigente em " + data));
    }

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private static boolean util(LocalDate d) {
        return d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY;
    }

    private static LocalDate proximoDiaUtil(LocalDate d) {
        while (!util(d)) d = d.plusDays(1);
        return d;
    }

    private static LocalDate somarDiasUteis(LocalDate d, int n) {
        while (n > 0) {
            d = d.plusDays(1);
            if (util(d)) n--;
        }
        return d;
    }

    private static long contarDiasUteis(LocalDate ini, LocalDate fim) {
        long n = 0;
        for (LocalDate d = ini; !d.isAfter(fim); d = d.plusDays(1)) if (util(d)) n++;
        return n;
    }
}
