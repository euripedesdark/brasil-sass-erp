package br.com.brasil_saas.fiscal.service;

import br.com.brasil_saas.fiscal.model.Nfse;
import br.com.brasil_saas.fiscal.model.Reinf;
import br.com.brasil_saas.fiscal.repository.NfseRepository;
import br.com.brasil_saas.fiscal.repository.ReinfRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * EFD-Reinf — geração local de eventos a partir das NFS-e do período.
 *
 * <p>Não transmite à RFB: produz o payload JSON e grava em {@code bc_fis_reinf}
 * com status GERADO. Transmissão real exige certificado A1, XML assinado e
 * ambiente de homologação — fora do escopo deste serviço.
 *
 * <ul>
 *   <li>R-2020 — serviços prestados (NFS-e de saída)</li>
 *   <li>R-2010 — serviços tomados (NFS-e de entrada, se houver)</li>
 *   <li>R-2099 — fechamento da competência (só se houver eventos gerados)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ReinfService {

    private final ReinfRepository reinfRepo;
    private final NfseRepository nfses;

    private static final ObjectMapper JSON = new ObjectMapper();

    private static final DateTimeFormatter ISO_DIA = DateTimeFormatter.ISO_LOCAL_DATE;

    @Transactional(readOnly = true)
    public List<Reinf> listar(Long empresaId) {
        return reinfRepo.findByEmpresaIdOrderByCompetenciaDescGeradoAtDesc(empresaId);
    }

    @Transactional(readOnly = true)
    public List<Reinf> listarCompetencia(Long empresaId, String competencia) {
        validarCompetencia(competencia);
        return reinfRepo.findByEmpresaIdAndCompetencia(empresaId, competencia);
    }

    /**
     * Gera R-2020 (prestados) e R-2010 (tomados) para a competência.
     * Eventos já FECHADO/TRANSMITIDO não são sobrescritos.
     */
    @Transactional
    public List<Reinf> gerarPeriodo(Long empresaId, String competencia) {
        LocalDate ini = validarCompetencia(competencia);
        reinfRepo.bloquearEmpresa(empresaId).orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
        if (reinfRepo.findByEmpresaIdAndCompetencia(empresaId, competencia).stream()
                .anyMatch(e -> Reinf.FECHADO.equals(e.getStatus()) || Reinf.TRANSMITIDO.equals(e.getStatus())))
            throw new BusinessException("Competencia ja fechada ou transmitida");
        LocalDateTime de = ini.atStartOfDay();
        LocalDateTime ate = ini.plusMonths(1).atStartOfDay();

        List<Nfse> todas = nfses.findNoPeriodo(empresaId, de, ate);
        List<Nfse> prestados = new ArrayList<>();
        List<Nfse> tomados = new ArrayList<>();
        for (Nfse n : todas) {
            if (!emitida(n.getStatus())) continue;
            String tipo = n.getTipoOperacao() == null ? "S" : n.getTipoOperacao().trim().toUpperCase(Locale.ROOT);
            if ("E".equals(tipo)) tomados.add(n);
            else prestados.add(n);
        }

        List<Reinf> saida = new ArrayList<>();
        saida.add(salvarEvento(empresaId, competencia, "R-2020", montarR2020(competencia, prestados), prestados));
        saida.add(salvarEvento(empresaId, competencia, "R-2010", montarR2010(competencia, tomados), tomados));
        return saida;
    }

    /**
     * Gera R-2099 de fechamento. Exige pelo menos um R-2010 ou R-2020 GERADO na competência.
     */
    @Transactional
    public Reinf fechar(Long empresaId, String competencia) {
        validarCompetencia(competencia);
        reinfRepo.bloquearEmpresa(empresaId).orElseThrow(() -> new ResourceNotFoundException("Empresa nao encontrada"));
        List<Reinf> eventos = reinfRepo.findByEmpresaIdAndCompetencia(empresaId, competencia);
        for (Reinf evento : eventos) {
            if ("R-2099".equals(evento.getEvento()) && Reinf.TRANSMITIDO.equals(evento.getStatus()))
                throw new BusinessException("Competencia ja transmitida");
            if ("R-2099".equals(evento.getEvento()) && Reinf.FECHADO.equals(evento.getStatus())) return evento;
        }
        boolean temPeriodico = eventos.stream()
                .anyMatch(e -> ("R-2010".equals(e.getEvento()) || "R-2020".equals(e.getEvento()))
                        && (Reinf.GERADO.equals(e.getStatus()) || Reinf.FECHADO.equals(e.getStatus()) || Reinf.TRANSMITIDO.equals(e.getStatus())));
        if (!temPeriodico) {
            throw new BusinessException("Gere R-2010/R-2020 antes de fechar a competencia");
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("evento", "R-2099");
        payload.put("competencia", competencia);
        payload.put("ideEvento", Map.of(
                "perApur", competenciaParaPerApur(competencia),
                "tpAmb", 2,
                "procEmi", 1,
                "verProc", "BrasilSaaSERP-1.0"
        ));
        payload.put("infoFech", Map.of(
                "evtServTm", eventos.stream().anyMatch(e -> "R-2010".equals(e.getEvento()) && nz(e.getTotalDocs()) > 0) ? "S" : "N",
                "evtServPr", eventos.stream().anyMatch(e -> "R-2020".equals(e.getEvento()) && nz(e.getTotalDocs()) > 0) ? "S" : "N",
                "evtAssDespRec", "N",
                "evtAssDespRep", "N",
                "evtComProd", "N",
                "evtCPRB", "N",
                "evtAquis", "N"
        ));
        payload.put("observacao", "Fechamento local — transmissao RFB nao implementada");

        Reinf r = salvarEvento(empresaId, competencia, "R-2099", payload, List.of());
        // marca periodicos como FECHADO
        for (Reinf e : eventos) {
            if (("R-2010".equals(e.getEvento()) || "R-2020".equals(e.getEvento())) && Reinf.GERADO.equals(e.getStatus())) {
                e.setStatus(Reinf.FECHADO);
                reinfRepo.save(e);
            }
        }
        r.setStatus(Reinf.FECHADO);
        return reinfRepo.save(r);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detalhe(Long empresaId, Long id) {
        Reinf r = reinfRepo.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("reinf", String.valueOf(id)));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", r.getId());
        out.put("competencia", r.getCompetencia());
        out.put("evento", r.getEvento());
        out.put("status", r.getStatus());
        out.put("protocolo", r.getProtocolo());
        out.put("geradoAt", r.getGeradoAt());
        out.put("totalDocs", r.getTotalDocs());
        out.put("valorTotal", r.getValorTotal());
        out.put("payload", r.getPayload());
        return out;
    }

    // ── builders ──────────────────────────────────────────────────────

    private Map<String, Object> montarR2020(String competencia, List<Nfse> notas) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("evento", "R-2020");
        root.put("competencia", competencia);
        root.put("ideEvento", Map.of(
                "perApur", competenciaParaPerApur(competencia),
                "tpAmb", 2,
                "procEmi", 1,
                "verProc", "BrasilSaaSERP-1.0"
        ));
        List<Map<String, Object>> nfs = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Nfse n : notas) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("nfseId", n.getId());
            item.put("numero", n.getNumero());
            item.put("serieRps", n.getSerieRps());
            item.put("numeroRps", n.getNumeroRps());
            item.put("dataEmissao", n.getDataEmissao() == null ? null : n.getDataEmissao().toLocalDate().format(ISO_DIA));
            item.put("tomadorId", n.getClienteId() != null ? n.getClienteId() : n.getPessoaId());
            item.put("vlrBruto", dinheiro(n.getValorTotal()));
            item.put("baseCalculo", dinheiro(n.getBaseCalculo()));
            item.put("vlrIss", dinheiro(n.getValorIss()));
            item.put("aliqIss", n.getAliquotaIss());
            item.put("lc116", n.getLc116Codigo());
            nfs.add(item);
            total = total.add(nz(n.getValorTotal()));
        }
        root.put("nfs", nfs);
        root.put("totais", Map.of("quantidade", nfs.size(), "valorBruto", dinheiro(total)));
        return root;
    }

    private Map<String, Object> montarR2010(String competencia, List<Nfse> notas) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("evento", "R-2010");
        root.put("competencia", competencia);
        root.put("ideEvento", Map.of(
                "perApur", competenciaParaPerApur(competencia),
                "tpAmb", 2,
                "procEmi", 1,
                "verProc", "BrasilSaaSERP-1.0"
        ));
        List<Map<String, Object>> nfs = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Nfse n : notas) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("nfseId", n.getId());
            item.put("numero", n.getNumero());
            item.put("dataEmissao", n.getDataEmissao() == null ? null : n.getDataEmissao().toLocalDate().format(ISO_DIA));
            item.put("prestadorId", n.getPessoaId());
            item.put("vlrBruto", dinheiro(n.getValorTotal()));
            item.put("baseCalculo", dinheiro(n.getBaseCalculo()));
            item.put("vlrIss", dinheiro(n.getValorIss()));
            nfs.add(item);
            total = total.add(nz(n.getValorTotal()));
        }
        root.put("nfs", nfs);
        root.put("totais", Map.of("quantidade", nfs.size(), "valorBruto", dinheiro(total)));
        return root;
    }

    private Reinf salvarEvento(Long empresaId, String competencia, String evento,
                               Map<String, Object> payload, List<?> docs) {
        Reinf r = reinfRepo.findByEmpresaIdAndCompetenciaAndEvento(empresaId, competencia, evento).orElse(null);
        if (r != null && (Reinf.FECHADO.equals(r.getStatus()) || Reinf.TRANSMITIDO.equals(r.getStatus()))) {
            throw new BusinessException("Evento " + evento + " da competencia " + competencia + " ja esta " + r.getStatus());
        }
        if (r == null) {
            r = new Reinf();
            r.setEmpresaId(empresaId);
            r.setCompetencia(competencia);
            r.setEvento(evento);
        }
        BigDecimal valor = BigDecimal.ZERO;
        if (docs != null) {
            for (Object o : docs) {
                if (o instanceof Nfse n) valor = valor.add(nz(n.getValorTotal()));
            }
        }
        r.setTotalDocs(docs == null ? 0 : docs.size());
        r.setValorTotal(dinheiro(valor));
        r.setPayload(toJson(payload));
        r.setStatus(Reinf.GERADO);
        r.setGeradoAt(LocalDateTime.now());
        r.setProtocolo("LOCAL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return reinfRepo.save(r);
    }

    // ── helpers ───────────────────────────────────────────────────────

    private LocalDate validarCompetencia(String competencia) {
        if (competencia == null || !competencia.matches("(0[1-9]|1[0-2])/[0-9]{4}"))
            throw new BusinessException("Competencia invalida, use MM/AAAA");
        try {
            String[] p = competencia.split("/");
            return LocalDate.of(Integer.parseInt(p[1]), Integer.parseInt(p[0]), 1);
        } catch (Exception e) {
            throw new BusinessException("Competencia invalida, use MM/AAAA");
        }
    }

    /** REINF usa AAAA-MM no perApur. */
    private String competenciaParaPerApur(String competencia) {
        String[] p = competencia.split("/");
        return p[1] + "-" + p[0];
    }

    private boolean emitida(String status) {
        return status != null && List.of("EMITIDA", "AUTORIZADA").contains(status.trim().toUpperCase(Locale.ROOT));
    }

    private int nz(Integer v) { return v == null ? 0 : v; }

    private BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private BigDecimal dinheiro(BigDecimal v) {
        return nz(v).setScale(2, RoundingMode.HALF_UP);
    }

    private String toJson(Map<String, Object> map) {
        try {
            return JSON.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Falha ao serializar evento Reinf");
        }
    }
}
