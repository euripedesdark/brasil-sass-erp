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
import java.util.List;
@Service @RequiredArgsConstructor
public class ApuracaoService {
    private final ApuracaoRepository apuracoes;
    private final ImpostoRepository impostos;
    private final NfeRepository nfes;
    private final NfseRepository nfses;
    @Transactional(readOnly = true)
    public List<Apuracao> listar(Long empresaId) { return apuracoes.findByEmpresaIdOrderByCompetenciaDesc(empresaId); }
    @Transactional
    public Apuracao calcular(Long empresaId, Long impostoId, String competencia) {
        Imposto imp = impostos.findById(impostoId).filter(i -> empresaId.equals(i.getEmpresaId()))
                .orElseThrow(() -> new ResourceNotFoundException("imposto", String.valueOf(impostoId)));
        LocalDate ini = inicioCompetencia(competencia);
        LocalDateTime de = ini.atStartOfDay();
        LocalDateTime ate = ini.plusMonths(1).atStartOfDay();
        String sigla = imp.getSigla() == null ? "" : imp.getSigla().trim().toUpperCase();
        BigDecimal base; BigDecimal devido; BigDecimal credito;
        if ("ICMS".equals(sigla) || "IPI".equals(sigla)) {
            List<Nfe> s = ativas(nfes.findByEmpresaIdAndTipoOperacaoAndDataEmissaoBetweenAndDeletedAtIsNull(empresaId, "S", de, ate));
            List<Nfe> e = ativas(nfes.findByEmpresaIdAndTipoOperacaoAndDataEmissaoBetweenAndDeletedAtIsNull(empresaId, "E", de, ate));
            base = s.stream().map(n -> nz(n.getValorProdutos())).reduce(BigDecimal.ZERO, BigDecimal::add);
            devido = "ICMS".equals(sigla)
                    ? s.stream().map(n -> nz(n.getValorIcms())).reduce(BigDecimal.ZERO, BigDecimal::add)
                    : s.stream().map(n -> nz(n.getValorIpi())).reduce(BigDecimal.ZERO, BigDecimal::add);
            credito = "ICMS".equals(sigla)
                    ? e.stream().map(n -> nz(n.getValorIcms())).reduce(BigDecimal.ZERO, BigDecimal::add)
                    : e.stream().map(n -> nz(n.getValorIpi())).reduce(BigDecimal.ZERO, BigDecimal::add);
        } else if ("PIS".equals(sigla) || "COFINS".equals(sigla)) {
            BigDecimal aliq = aliquotaObrigatoria(imp);
            List<Nfe> s = ativas(nfes.findByEmpresaIdAndTipoOperacaoAndDataEmissaoBetweenAndDeletedAtIsNull(empresaId, "S", de, ate));
            List<Nfe> e = ativas(nfes.findByEmpresaIdAndTipoOperacaoAndDataEmissaoBetweenAndDeletedAtIsNull(empresaId, "E", de, ate));
            base = s.stream().map(n -> nz(n.getValorProdutos())).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal baseE = e.stream().map(n -> nz(n.getValorProdutos())).reduce(BigDecimal.ZERO, BigDecimal::add);
            devido = pct(base, aliq);
            credito = pct(baseE, aliq);
        } else if ("ISS".equals(sigla) || "ISSQN".equals(sigla)) {
            base = BigDecimal.ZERO; devido = BigDecimal.ZERO;
            for (Nfse n : nfses.findByEmpresaIdAndDataEmissaoBetweenAndDeletedAtIsNull(empresaId, de, ate)) {
                if (cancelada(n.getStatus())) continue;
                base = base.add(nz(n.getBaseCalculo()));
                devido = devido.add(nz(n.getValorIss()));
            }
            credito = BigDecimal.ZERO;
        } else {
            BigDecimal aliq = aliquotaObrigatoria(imp);
            List<Nfe> s = ativas(nfes.findByEmpresaIdAndTipoOperacaoAndDataEmissaoBetweenAndDeletedAtIsNull(empresaId, "S", de, ate));
            base = s.stream().map(n -> nz(n.getValorProdutos())).reduce(BigDecimal.ZERO, BigDecimal::add);
            devido = pct(base, aliq);
            credito = BigDecimal.ZERO;
        }
        base = dinheiro(base); devido = dinheiro(devido); credito = dinheiro(credito);
        BigDecimal pagar = devido.subtract(credito);
        if (pagar.signum() < 0) pagar = BigDecimal.ZERO;
        pagar = dinheiro(pagar);
        Apuracao a = apuracoes.findByEmpresaIdAndImpostoIdAndCompetencia(empresaId, impostoId, competencia).orElse(null);
        if (a != null && "ABERTA".equals(a.getStatus()) == false) throw new BusinessException("Competencia ja encerrada ou transmitida");
        if (a == null) { a = new Apuracao(); a.setEmpresaId(empresaId); a.setImpostoId(impostoId); a.setCompetencia(competencia); }
        a.setBaseCalculo(base); a.setValorDevido(devido); a.setValorCredito(credito); a.setValorPagar(pagar);
        a.setStatus("ABERTA"); a.setApuradaAt(LocalDateTime.now());
        return apuracoes.save(a);
    }
    @Transactional
    public Apuracao encerrar(Long empresaId, Long id) {
        Apuracao a = porEmpresa(empresaId, id);
        if ("ABERTA".equals(a.getStatus()) == false) throw new BusinessException("Somente apuracao ABERTA pode ser encerrada");
        a.setStatus("ENCERRADA");
        return apuracoes.save(a);
    }
    @Transactional
    public Apuracao reabrir(Long empresaId, Long id) {
        Apuracao a = porEmpresa(empresaId, id);
        if ("ENCERRADA".equals(a.getStatus()) == false) throw new BusinessException("Somente apuracao ENCERRADA pode ser reaberta");
        a.setStatus("ABERTA");
        return apuracoes.save(a);
    }
    @Transactional
    public Apuracao transmitir(Long empresaId, Long id) {
        Apuracao a = porEmpresa(empresaId, id);
        if ("ENCERRADA".equals(a.getStatus()) == false) throw new BusinessException("Somente apuracao ENCERRADA pode ser transmitida");
        a.setStatus("TRANSMITIDA");
        return apuracoes.save(a);
    }
    private Apuracao porEmpresa(Long empresaId, Long id) {
        return apuracoes.findById(id).filter(a -> empresaId.equals(a.getEmpresaId()))
                .orElseThrow(() -> new ResourceNotFoundException("apuracao", String.valueOf(id)));
    }
    private LocalDate inicioCompetencia(String competencia) {
        try {
            String[] p = competencia.split("/");
            return LocalDate.of(Integer.parseInt(p[1]), Integer.parseInt(p[0]), 1);
        } catch (Exception e) { throw new BusinessException("Competencia invalida, use MM/AAAA"); }
    }
    private List<Nfe> ativas(List<Nfe> notas) {
        return notas.stream().filter(n -> cancelada(n.getStatus()) == false).toList();
    }
    private boolean cancelada(String status) {
        if (status == null) return false;
        String s = status.trim().toUpperCase();
        return s.startsWith("CANCEL");
    }
    private BigDecimal aliquotaObrigatoria(Imposto imp) {
        if (imp.getAliquotaPadrao() == null || imp.getAliquotaPadrao().signum() <= 0)
            throw new BusinessException("Configure a aliquota padrao do imposto " + imp.getSigla());
        return imp.getAliquotaPadrao();
    }
    private BigDecimal pct(BigDecimal base, BigDecimal aliq) {
        return dinheiro(base.multiply(aliq).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
    }
    private BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private BigDecimal dinheiro(BigDecimal v) { return nz(v).setScale(2, RoundingMode.HALF_UP); }
}
