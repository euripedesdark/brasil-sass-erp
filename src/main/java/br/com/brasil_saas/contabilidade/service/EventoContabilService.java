package br.com.brasil_saas.contabilidade.service;

import br.com.brasil_saas.contabilidade.model.CtbLancamento;
import br.com.brasil_saas.contabilidade.model.CtbPartida;
import br.com.brasil_saas.contabilidade.repository.CtbLancamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventoContabilService {

    public static final String EVT_BAIXA_TITULO = "BAIXA_TITULO";
    public static final String EVT_RECEBIMENTO_COMPRA = "RECEBIMENTO_COMPRA";
    public static final String EVT_FATURAMENTO_VENDA = "FATURAMENTO_VENDA";
    public static final String EVT_MOVIMENTO_ESTOQUE = "MOVIMENTO_ESTOQUE";
    public static final String EVT_FOLHA = "FOLHA_PROCESSADA";

    private final ContabilidadeService contabilidade;
    private final CtbLancamentoRepository lancamentos;
    private final JdbcTemplate jdbc;

    public record Resultado(Optional<CtbLancamento> lancamento, boolean pendente, String motivo) {
        public static Resultado ok(CtbLancamento l) { return new Resultado(Optional.of(l), false, null); }
        public static Resultado pend(String m) { return new Resultado(Optional.empty(), true, m); }
        public static Resultado jaExistia(CtbLancamento l) { return new Resultado(Optional.of(l), false, "JA_EXISTIA"); }
    }

    @Transactional
    public Resultado contabilizar(Long empresaId, String evento, String origemTipo, Long origemId,
                                  LocalDate data, BigDecimal valor, String historico) {
        if (valor == null || valor.signum() <= 0) {
            return Resultado.pend("Valor invalido para contabilizar");
        }
        List<CtbLancamento> existentes = lancamentos
                .findByEmpresaIdAndOrigemTipoAndOrigemIdAndStatusInAndDeletedAtIsNull(
                        empresaId, origemTipo, origemId, List.of("LANCADO", "RASCUNHO"));
        if (!existentes.isEmpty()) {
            return Resultado.jaExistia(existentes.get(0));
        }

        Regra r = buscarRegra(empresaId, evento);
        if (r == null || r.contaDebitoId() == null || r.contaCreditoId() == null) {
            registrarPendencia(empresaId, evento, origemTipo, origemId, valor,
                    "Sem regra/contas configuradas para evento " + evento);
            return Resultado.pend("Sem regra/contas para " + evento);
        }

        String hist = historico != null ? historico : (r.historico() != null ? r.historico() : evento + " #" + origemId);
        CtbLancamento l = new CtbLancamento();
        l.setEmpresaId(empresaId);
        l.setData(data != null ? data : LocalDate.now());
        l.setHistorico(hist);
        l.setOrigemTipo(origemTipo);
        l.setOrigemId(origemId);
        try {
            l = contabilidade.salvar(empresaId, l);
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY
                    || (ex.getReason() != null && ex.getReason().toLowerCase().contains("fechado"))) {
                registrarPendencia(empresaId, evento, origemTipo, origemId, valor, "Periodo fechado: " + ex.getReason());
                throw ex;
            }
            throw ex;
        }

        CtbPartida deb = new CtbPartida();
        deb.setEmpresaId(empresaId);
        deb.setContaId(r.contaDebitoId());
        deb.setCentroCustoId(r.centroCustoId());
        deb.setDebito(valor);
        deb.setCredito(BigDecimal.ZERO);
        deb.setHistorico(hist);
        contabilidade.addPartida(empresaId, l.getId(), deb);

        CtbPartida cre = new CtbPartida();
        cre.setEmpresaId(empresaId);
        cre.setContaId(r.contaCreditoId());
        cre.setCentroCustoId(r.centroCustoId());
        cre.setDebito(BigDecimal.ZERO);
        cre.setCredito(valor);
        cre.setHistorico(hist);
        contabilidade.addPartida(empresaId, l.getId(), cre);

        CtbLancamento lancado = contabilidade.lancar(empresaId, l.getId());
        BigDecimal[] t = contabilidade.totais(empresaId, lancado.getId());
        if (t[0].compareTo(t[1]) != 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Partida dobrada nao fecha");
        }
        resolverPendenciaSeHouver(empresaId, origemTipo, origemId, evento);
        return Resultado.ok(lancado);
    }

    private record Regra(Long contaDebitoId, Long contaCreditoId, Long centroCustoId, String historico) {}

    private Regra buscarRegra(Long empresaId, String evento) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "select conta_debito_id, conta_credito_id, centro_custo_id, historico from brasil_saas.bc_cont_regra_lancamento where empresa_id=? and codigo=? and ativo=true limit 1",
                empresaId, evento);
        if (rows.isEmpty()) return null;
        Map<String, Object> m = rows.get(0);
        return new Regra(asLong(m.get("conta_debito_id")), asLong(m.get("conta_credito_id")),
                asLong(m.get("centro_custo_id")), (String) m.get("historico"));
    }

    private static Long asLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        return Long.valueOf(o.toString());
    }

    private void registrarPendencia(Long empresaId, String evento, String origemTipo, Long origemId,
                                    BigDecimal valor, String motivo) {
        jdbc.update("""
            insert into brasil_saas.bc_ctb_pendencia(empresa_id,evento,origem_tipo,origem_id,motivo,valor,status)
            values(?,?,?,?,?,?,'ABERTA')
            on conflict(empresa_id,origem_tipo,origem_id,evento) do update
              set motivo=excluded.motivo, valor=excluded.valor, status='ABERTA', resolvido_em=null
            """, empresaId, evento, origemTipo, origemId, motivo, valor);
    }

    private void resolverPendenciaSeHouver(Long empresaId, String origemTipo, Long origemId, String evento) {
        jdbc.update("""
            update brasil_saas.bc_ctb_pendencia set status='RESOLVIDA', resolvido_em=now()
            where empresa_id=? and origem_tipo=? and origem_id=? and evento=? and status='ABERTA'
            """, empresaId, origemTipo, origemId, evento);
    }
}
