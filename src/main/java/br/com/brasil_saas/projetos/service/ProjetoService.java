package br.com.brasil_saas.projetos.service;
import br.com.brasil_saas.projetos.model.*;
import java.util.List;
import java.util.Map;
public interface ProjetoService {
    List<PrjProjeto> projetos(Long empresaId, String status);
    PrjProjeto salvar(Long empresaId, PrjProjeto p);
    List<PrjEtapa> etapas(Long empresaId, Long projetoId);
    PrjEtapa salvarEtapa(Long empresaId, Long projetoId, PrjEtapa e);
    PrjEtapa avancarEtapa(Long empresaId, Long projetoId, Long etapaId, Integer pct);
    List<PrjMovimento> movimentos(Long empresaId, Long projetoId, String tipo);
    PrjMovimento lancarMovimento(Long empresaId, Long projetoId, PrjMovimento m);
    List<PrjRisco> riscos(Long empresaId, Long projetoId);
    PrjRisco salvarRisco(Long empresaId, Long projetoId, PrjRisco r);
    List<PrjMudanca> mudancas(Long empresaId, Long projetoId);
    PrjMudanca solicitarMudanca(Long empresaId, Long projetoId, PrjMudanca m);
    PrjMudanca decidirMudanca(Long empresaId, Long userId, Long projetoId, Long mudancaId, boolean aprovar);
    List<PrjFaturamento> faturamentos(Long empresaId, Long projetoId);
    PrjFaturamento salvarFaturamento(Long empresaId, Long projetoId, PrjFaturamento f);
    PrjFaturamento faturar(Long empresaId, Long projetoId, Long faturamentoId);
    Map<String, Object> resumo(Long empresaId, Long projetoId);
}
