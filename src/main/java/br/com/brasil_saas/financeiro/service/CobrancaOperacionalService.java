package br.com.brasil_saas.financeiro.service;

import br.com.brasil_saas.financeiro.model.CobrancaAcao;
import br.com.brasil_saas.financeiro.model.PromessaPagamento;
import br.com.brasil_saas.financeiro.model.Titulo;
import br.com.brasil_saas.financeiro.repository.CobrancaAcaoRepository;
import br.com.brasil_saas.financeiro.repository.PromessaPagamentoRepository;
import br.com.brasil_saas.financeiro.repository.TituloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CobrancaOperacionalService {

    private final TituloRepository titulos;
    private final CobrancaAcaoRepository acoes;
    private final PromessaPagamentoRepository promessas;

    private static final Set<String> TIPOS = Set.of(
            "LEMBRETE", "AVISO", "NEGATIVACAO", "LIGACAO", "EMAIL", "WHATSAPP", "OUTRO");

    @Transactional(readOnly = true)
    public List<Map<String, Object>> carteira(Long empresaId) {
        List<Titulo> lista = titulos.findByEmpresaIdAndDeletedAtIsNullOrderByDataVencimento(empresaId);
        LocalDate hoje = LocalDate.now();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Titulo t : lista) {
            if (!"R".equalsIgnoreCase(t.getTipo())) continue;
            if (!"ABERTO".equals(t.getStatus()) && !"PARCIAL".equals(t.getStatus())) continue;
            long dias = ChronoUnit.DAYS.between(t.getDataVencimento(), hoje);
            int nivelSugerido = dias <= 0 ? 0 : dias <= 15 ? 1 : dias <= 30 ? 2 : dias <= 60 ? 3 : 4;
            Integer maxNivel = acoes.maxNivel(empresaId, t.getId());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("tituloId", t.getId());
            m.put("descricao", t.getDescricao());
            m.put("pessoaId", t.getPessoaId());
            m.put("valorSaldo", t.getValorSaldo());
            m.put("dataVencimento", t.getDataVencimento());
            m.put("status", t.getStatus());
            m.put("diasAtraso", dias);
            m.put("nivelSugerido", nivelSugerido);
            m.put("ultimoNivel", maxNivel == null ? 0 : maxNivel);
            out.add(m);
        }
        out.sort((a, b) -> Long.compare((Long) b.get("diasAtraso"), (Long) a.get("diasAtraso")));
        return out;
    }

    @Transactional
    public CobrancaAcao registrarAcao(Long empresaId, Long tituloId, String tipo, Integer nivel, String observacao) {
        Titulo t = exigirtituloReceber(empresaId, tituloId);
        String tip = tipo == null ? "" : tipo.trim().toUpperCase();
        if (!TIPOS.contains(tip)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de ação inválido");
        }
        int niv = nivel == null ? 1 : nivel;
        if (niv < 1 || niv > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nível deve ser 1..5");
        }
        CobrancaAcao a = new CobrancaAcao();
        a.setEmpresaId(empresaId);
        a.setTituloId(tituloId);
        a.setPessoaId(t.getPessoaId());
        a.setTipo(tip);
        a.setNivel(niv);
        a.setObservacao(observacao);
        return acoes.save(a);
    }

    @Transactional(readOnly = true)
    public List<CobrancaAcao> historicoAcoes(Long empresaId, Long tituloId) {
        exigirtituloReceber(empresaId, tituloId);
        return acoes.findByEmpresaIdAndTituloIdOrderByCreatedAtDesc(empresaId, tituloId);
    }

    @Transactional
    public PromessaPagamento criarPromessa(Long empresaId, Long tituloId, BigDecimal valor,
                                           LocalDate dataPrometida, String observacao) {
        Titulo t = exigirtituloReceber(empresaId, tituloId);
        if (valor == null || valor.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valor prometido deve ser positivo");
        }
        if (dataPrometida == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data prometida obrigatória");
        }
        if (dataPrometida.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data prometida não pode ser no passado");
        }
        BigDecimal saldo = t.getValorSaldo() == null ? BigDecimal.ZERO : t.getValorSaldo();
        if (valor.compareTo(saldo) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Promessa não pode exceder o saldo do título");
        }
        for (PromessaPagamento p : promessas.findByEmpresaIdAndTituloIdOrderByCreatedAtDesc(empresaId, tituloId)) {
            if ("ABERTA".equals(p.getStatus())) {
                p.setStatus("CANCELADA");
                promessas.save(p);
            }
        }
        PromessaPagamento p = new PromessaPagamento();
        p.setEmpresaId(empresaId);
        p.setTituloId(tituloId);
        p.setPessoaId(t.getPessoaId());
        p.setValorPrometido(valor);
        p.setDataPrometida(dataPrometida);
        p.setStatus("ABERTA");
        p.setObservacao(observacao);
        return promessas.save(p);
    }

    @Transactional
    public PromessaPagamento atualizarPromessa(Long empresaId, Long id, String novoStatus) {
        PromessaPagamento p = promessas.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promessa inexistente"));
        if (!"ABERTA".equals(p.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Somente promessa ABERTA pode mudar de status");
        }
        String s = novoStatus == null ? "" : novoStatus.trim().toUpperCase();
        if (!Set.of("CUMPRIDA", "QUEBRADA", "CANCELADA").contains(s)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status inválido");
        }
        p.setStatus(s);
        p.setUpdatedAt(LocalDateTime.now());
        return promessas.save(p);
    }

    @Transactional(readOnly = true)
    public List<PromessaPagamento> listarPromessas(Long empresaId, String status) {
        if (status != null && !status.isBlank()) {
            return promessas.findByEmpresaIdAndStatusOrderByDataPrometidaAsc(empresaId, status.trim().toUpperCase());
        }
        return promessas.findByEmpresaIdOrderByDataPrometidaAsc(empresaId);
    }

    @Transactional
    public int processarPromessasVencidas(Long empresaId) {
        LocalDate hoje = LocalDate.now();
        int n = 0;
        for (PromessaPagamento p : promessas.findByEmpresaIdAndStatusOrderByDataPrometidaAsc(empresaId, "ABERTA")) {
            if (p.getDataPrometida().isBefore(hoje)) {
                p.setStatus("QUEBRADA");
                p.setUpdatedAt(LocalDateTime.now());
                promessas.save(p);
                n++;
            }
        }
        return n;
    }

    private Titulo exigirtituloReceber(Long empresaId, Long tituloId) {
        Titulo t = titulos.findByIdAndEmpresaIdAndDeletedAtIsNull(tituloId, empresaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Título inexistente"));
        if (!"R".equalsIgnoreCase(t.getTipo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cobrança só se aplica a títulos a receber");
        }
        if (!"ABERTO".equals(t.getStatus()) && !"PARCIAL".equals(t.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Título não está em aberto");
        }
        return t;
    }
}
