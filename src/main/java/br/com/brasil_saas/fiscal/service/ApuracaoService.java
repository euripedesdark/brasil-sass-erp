package br.com.brasil_saas.fiscal.service;

import br.com.brasil_saas.fiscal.model.Apuracao;
import br.com.brasil_saas.fiscal.model.Imposto;
import br.com.brasil_saas.fiscal.model.Nfe;
import br.com.brasil_saas.fiscal.model.Nfse;
import br.com.brasil_saas.fiscal.repository.ApuracaoRepository;
import br.com.brasil_saas.fiscal.repository.ImpostoRepository;
import br.com.brasil_saas.fiscal.repository.NfeRepository;
import br.com.brasil_saas.fiscal.repository.NfseRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Apuração mensal de impostos a partir das NFe/NFS-e do período.
 *
 * <p>Regras por sigla:
 * <ul>
 *   <li>ICMS / IPI — débito nas saídas, crédito nas entradas (valores já destacados na nota).</li>
 *   <li>PIS / COFINS — débito nas saídas e crédito nas entradas pelos valores destacados.
 *       Se a nota não trouxer valor (zero), cai na alíquota padrão do cadastro sobre o
 *       valor dos produtos (regime cumulativo simplificado).</li>
 *   <li>ISS — só NFS-e de saída; sem crédito.</li>
 *   <li>Demais — alíquota padrão sobre valor dos produtos das saídas; sem crédito.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ApuracaoService {

    private final ApuracaoRepository apuracoes;
    private final ImpostoRepository impostos;
    private final NfeRepository nfes;
    private final NfseRepository nfses;

    @Transactional(readOnly = true)
    public List<Apuracao> listar(Long empresaId) {
        return apuracoes.findByEmpresaIdOrderByCompetenciaDesc(empresaId);
    }

    /**
     * Calcula (ou recalcula, se ainda ABERTA) a apuração de um imposto na competência MM/AAAA.
     */
    @Transactional
    public Apuracao calcular(Long empresaId, Long impostoId, String competencia) {
        Imposto imp = impostos.findById(impostoId).filter(i -> empresaId.equals(i.getEmpresaId()))
                .orElseThrow(() -> new ResourceNotFoundException("imposto", String.valueOf(impostoId)));
        LocalDate ini = inicioCompetencia(competencia);
        LocalDateTime de = ini.atStartOfDay();
        LocalDateTime ate = ini.plusMonths(1).atStartOfDay();

        String sigla = imp.getSigla() == null ? "" : imp.getSigla().trim().toUpperCase();
        Resultado r = switch (sigla) {
            case "ICMS" -> apurarIcmsIpi(empresaId, de, ate, Nfe::getValorIcms);
            case "IPI"  -> apurarIcmsIpi(empresaId, de, ate, Nfe::getValorIpi);
            case "PIS"  -> apurarPisCofins(empresaId, de, ate, imp, Nfe::getValorPis);
            case "COFINS" -> apurarPisCofins(empresaId, de, ate, imp, Nfe::getValorCofins);
            case "ISS", "ISSQN" -> apurarIss(empresaId, de, ate);
            default -> apurarGenerico(empresaId, de, ate, imp);
        };

        BigDecimal base = dinheiro(r.base);
        BigDecimal devido = dinheiro(r.devido);
        BigDecimal credito = dinheiro(r.credito);
        BigDecimal pagar = dinheiro(devido.subtract(credito).max(BigDecimal.ZERO));

        Apuracao a = apuracoes.findByEmpresaIdAndImpostoIdAndCompetencia(empresaId, impostoId, competencia).orElse(null);
        if (a != null && !"ABERTA".equals(a.getStatus())) {
            throw new BusinessException("Competencia ja encerrada ou transmitida");
        }
        if (a == null) {
            a = new Apuracao();
            a.setEmpresaId(empresaId);
            a.setImpostoId(impostoId);
            a.setCompetencia(competencia);
        }
        a.setBaseCalculo(base);
        a.setValorDevido(devido);
        a.setValorCredito(credito);
        a.setValorPagar(pagar);
        a.setStatus("ABERTA");
        a.setApuradaAt(LocalDateTime.now());
        return apuracoes.save(a);
    }

    /**
     * Calcula a apuração de todos os impostos ativos da empresa na competência.
     * Impostos já ENCERRADOS/TRANSMITIDOS são só listados, sem recalcular.
     */
    @Transactional
    public List<Apuracao> calcularTodas(Long empresaId, String competencia) {
        inicioCompetencia(competencia); // valida formato
        List<Imposto> ativos = impostos.findByEmpresaIdAndDeletedAtIsNullOrderBySigla(empresaId);
        if (ativos == null) ativos = List.of();
        List<Apuracao> saida = new ArrayList<>();
        for (Imposto imp : ativos) {
            Apuracao existente = apuracoes
                    .findByEmpresaIdAndImpostoIdAndCompetencia(empresaId, imp.getId(), competencia)
                    .orElse(null);
            if (existente != null && !"ABERTA".equals(existente.getStatus())) {
                saida.add(existente);
                continue;
            }
            saida.add(calcular(empresaId, imp.getId(), competencia));
        }
        return saida;
    }

    /**
     * Resumo consolidado da competência: totais por imposto + totais gerais.
     * Não grava; só lê o que já foi apurado (chame calcularTodas antes se quiser forçar).
     */
    @Transactional(readOnly = true)
    public Map<String, Object> resumo(Long empresaId, String competencia) {
        inicioCompetencia(competencia);
        List<Apuracao> lista = apuracoes.findByEmpresaIdAndCompetencia(empresaId, competencia);
        BigDecimal totalDevido = BigDecimal.ZERO;
        BigDecimal totalCredito = BigDecimal.ZERO;
        BigDecimal totalPagar = BigDecimal.ZERO;
        List<Map<String, Object>> itens = new ArrayList<>();
        for (Apuracao a : lista) {
            Imposto imp = impostos.findById(a.getImpostoId()).orElse(null);
            Map<String, Object> linha = new LinkedHashMap<>();
            linha.put("apuracaoId", a.getId());
            linha.put("impostoId", a.getImpostoId());
            linha.put("sigla", imp != null ? imp.getSigla() : null);
            linha.put("nome", imp != null ? imp.getNome() : null);
            linha.put("baseCalculo", a.getBaseCalculo());
            linha.put("valorDevido", a.getValorDevido());
            linha.put("valorCredito", a.getValorCredito());
            linha.put("valorPagar", a.getValorPagar());
            linha.put("status", a.getStatus());
            itens.add(linha);
            totalDevido = totalDevido.add(nz(a.getValorDevido()));
            totalCredito = totalCredito.add(nz(a.getValorCredito()));
            totalPagar = totalPagar.add(nz(a.getValorPagar()));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("competencia", competencia);
        out.put("impostos", itens);
        out.put("totalDevido", dinheiro(totalDevido));
        out.put("totalCredito", dinheiro(totalCredito));
        out.put("totalPagar", dinheiro(totalPagar));
        out.put("quantidade", itens.size());
        return out;
    }

    @Transactional
    public Apuracao encerrar(Long empresaId, Long id) {
        Apuracao a = porEmpresa(empresaId, id);
        if (!"ABERTA".equals(a.getStatus())) {
            throw new BusinessException("Somente apuracao ABERTA pode ser encerrada");
        }
        a.setStatus("ENCERRADA");
        return apuracoes.save(a);
    }

    @Transactional
    public Apuracao reabrir(Long empresaId, Long id) {
        Apuracao a = porEmpresa(empresaId, id);
        if (!"ENCERRADA".equals(a.getStatus())) {
            throw new BusinessException("Somente apuracao ENCERRADA pode ser reaberta");
        }
        a.setStatus("ABERTA");
        return apuracoes.save(a);
    }

    @Transactional
    public Apuracao transmitir(Long empresaId, Long id) {
        Apuracao a = porEmpresa(empresaId, id);
        if (!"ENCERRADA".equals(a.getStatus())) {
            throw new BusinessException("Somente apuracao ENCERRADA pode ser transmitida");
        }
        a.setStatus("TRANSMITIDA");
        return apuracoes.save(a);
    }

    // ── regras por imposto ──────────────────────────────────────────────

    private Resultado apurarIcmsIpi(Long empresaId, LocalDateTime de, LocalDateTime ate,
                                    Function<Nfe, BigDecimal> extrator) {
        List<Nfe> saidas = ativas(nfes.findByEmpresaIdAndTipoOperacaoAndDataEmissaoBetweenAndDeletedAtIsNull(
                empresaId, "S", de, ate));
        List<Nfe> entradas = ativas(nfes.findByEmpresaIdAndTipoOperacaoAndDataEmissaoBetweenAndDeletedAtIsNull(
                empresaId, "E", de, ate));
        BigDecimal base = saidas.stream().map(n -> nz(n.getValorProdutos())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal devido = saidas.stream().map(n -> nz(extrator.apply(n))).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal credito = entradas.stream().map(n -> nz(extrator.apply(n))).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Resultado(base, devido, credito);
    }

    /**
     * PIS/COFINS: usa valor destacado na NFe quando &gt; 0; senão aplica alíquota padrão
     * sobre valor dos produtos (fallback cumulativo). Crédito só nas entradas com valor &gt; 0.
     */
    private Resultado apurarPisCofins(Long empresaId, LocalDateTime de, LocalDateTime ate,
                                      Imposto imp, Function<Nfe, BigDecimal> extrator) {
        List<Nfe> saidas = ativas(nfes.findByEmpresaIdAndTipoOperacaoAndDataEmissaoBetweenAndDeletedAtIsNull(
                empresaId, "S", de, ate));
        List<Nfe> entradas = ativas(nfes.findByEmpresaIdAndTipoOperacaoAndDataEmissaoBetweenAndDeletedAtIsNull(
                empresaId, "E", de, ate));

        BigDecimal base = BigDecimal.ZERO;
        BigDecimal devido = BigDecimal.ZERO;
        for (Nfe n : saidas) {
            BigDecimal prod = nz(n.getValorProdutos());
            base = base.add(prod);
            BigDecimal destacado = nz(extrator.apply(n));
            if (destacado.signum() > 0) {
                devido = devido.add(destacado);
            } else if (imp.getAliquotaPadrao() != null && imp.getAliquotaPadrao().signum() > 0) {
                devido = devido.add(pct(prod, imp.getAliquotaPadrao()));
            }
        }

        BigDecimal credito = BigDecimal.ZERO;
        for (Nfe n : entradas) {
            BigDecimal destacado = nz(extrator.apply(n));
            if (destacado.signum() > 0) {
                credito = credito.add(destacado);
            }
        }
        return new Resultado(base, devido, credito);
    }

    private Resultado apurarIss(Long empresaId, LocalDateTime de, LocalDateTime ate) {
        BigDecimal base = BigDecimal.ZERO;
        BigDecimal devido = BigDecimal.ZERO;
        for (Nfse n : nfses.findByEmpresaIdAndDataEmissaoBetweenAndDeletedAtIsNull(empresaId, de, ate)) {
            if (cancelada(n.getStatus())) continue;
            base = base.add(nz(n.getBaseCalculo()));
            devido = devido.add(nz(n.getValorIss()));
        }
        return new Resultado(base, devido, BigDecimal.ZERO);
    }

    private Resultado apurarGenerico(Long empresaId, LocalDateTime de, LocalDateTime ate, Imposto imp) {
        BigDecimal aliq = aliquotaObrigatoria(imp);
        List<Nfe> saidas = ativas(nfes.findByEmpresaIdAndTipoOperacaoAndDataEmissaoBetweenAndDeletedAtIsNull(
                empresaId, "S", de, ate));
        BigDecimal base = saidas.stream().map(n -> nz(n.getValorProdutos())).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Resultado(base, pct(base, aliq), BigDecimal.ZERO);
    }

    // ── helpers ─────────────────────────────────────────────────────────

    private Apuracao porEmpresa(Long empresaId, Long id) {
        return apuracoes.findById(id).filter(a -> empresaId.equals(a.getEmpresaId()))
                .orElseThrow(() -> new ResourceNotFoundException("apuracao", String.valueOf(id)));
    }

    private LocalDate inicioCompetencia(String competencia) {
        try {
            String[] p = competencia.split("/");
            return LocalDate.of(Integer.parseInt(p[1]), Integer.parseInt(p[0]), 1);
        } catch (Exception e) {
            throw new BusinessException("Competencia invalida, use MM/AAAA");
        }
    }

    private List<Nfe> ativas(List<Nfe> notas) {
        return notas.stream().filter(n -> !cancelada(n.getStatus())).toList();
    }

    private boolean cancelada(String status) {
        if (status == null) return false;
        return status.trim().toUpperCase().startsWith("CANCEL");
    }

    private BigDecimal aliquotaObrigatoria(Imposto imp) {
        if (imp.getAliquotaPadrao() == null || imp.getAliquotaPadrao().signum() <= 0) {
            throw new BusinessException("Configure a aliquota padrao do imposto " + imp.getSigla());
        }
        return imp.getAliquotaPadrao();
    }

    private BigDecimal pct(BigDecimal base, BigDecimal aliq) {
        return dinheiro(base.multiply(aliq).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private BigDecimal dinheiro(BigDecimal v) {
        return nz(v).setScale(2, RoundingMode.HALF_UP);
    }

    private record Resultado(BigDecimal base, BigDecimal devido, BigDecimal credito) {}
}
