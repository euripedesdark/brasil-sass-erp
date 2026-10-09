package br.com.brasil_saas.fiscal.service;

import br.com.brasil_saas.fiscal.model.Apuracao;
import br.com.brasil_saas.fiscal.model.Reinf;
import br.com.brasil_saas.fiscal.repository.ApuracaoRepository;
import br.com.brasil_saas.fiscal.repository.ReinfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Painel consolidado: apurações, REINF, regras, NFe do período. */
@Service
@RequiredArgsConstructor
public class FiscalPainelService {

    private final ApuracaoRepository apuracaoRepository;
    private final ReinfRepository reinfRepository;
    private final RegraTributariaService regraService;
    private final JdbcTemplate jdbc;

    public Map<String, Object> resumo(Long empresaId, String competencia) {
        String comp = competencia == null || competencia.isBlank()
                ? YearMonth.now().toString()
                : competencia.trim();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("competencia", comp);

        List<Apuracao> aps = apuracaoRepository.findByEmpresaIdAndCompetencia(empresaId, comp);
        out.put("apuracoes", aps);
        out.put("apuracoesCount", aps.size());
        out.put("apuracoesEncerradas", aps.stream().filter(a -> "ENCERRADA".equalsIgnoreCase(a.getStatus())).count());

        List<Reinf> reinf = reinfRepository.findByEmpresaIdAndCompetencia(empresaId, comp);
        out.put("reinf", reinf);
        out.put("reinfCount", reinf.size());

        out.put("regrasAtivas", regraService.listar(empresaId).stream().filter(r -> Boolean.TRUE.equals(r.getAtiva())).count());

        LocalDate ini = YearMonth.parse(comp).atDay(1);
        LocalDate fim = YearMonth.parse(comp).atEndOfMonth();
        try {
            Integer nfe = jdbc.queryForObject(
                    "select count(*) from brasil_saas.bc_fis_nfe where empresa_id=? and deleted_at is null " +
                    "and data_emissao >= ? and data_emissao < ?",
                    Integer.class, empresaId, java.sql.Date.valueOf(ini), java.sql.Date.valueOf(fim.plusDays(1)));
            out.put("nfePeriodo", nfe == null ? 0 : nfe);
        } catch (Exception e) {
            out.put("nfePeriodo", null);
            out.put("nfeErro", e.getMessage());
        }
        try {
            Integer nfse = jdbc.queryForObject(
                    "select count(*) from brasil_saas.bc_fis_nfse where empresa_id=? and deleted_at is null " +
                    "and data_emissao >= ? and data_emissao < ?",
                    Integer.class, empresaId, java.sql.Date.valueOf(ini), java.sql.Date.valueOf(fim.plusDays(1)));
            out.put("nfsePeriodo", nfse == null ? 0 : nfse);
        } catch (Exception e) {
            out.put("nfsePeriodo", null);
        }
        return out;
    }
}
