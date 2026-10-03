package br.com.brasil_saas.crm.service;
import br.com.brasil_saas.crm.model.*;
import java.util.List;
import java.util.Map;
public interface CrmService {
    List<CrmLead> leads(Long empresaId, String etapa, String status);
    CrmLead salvar(Long empresaId, CrmLead l);
    CrmLead moverEtapa(Long empresaId, Long id, String etapa);
    List<Map<String, Object>> pipeline(Long empresaId);
    Map<String, Object> forecast(Long empresaId, Integer ano, Integer mes);
    List<CrmAtividade> atividades(Long empresaId, Long leadId, Boolean pendentes);
    CrmAtividade salvarAtividade(Long empresaId, CrmAtividade a);
    CrmAtividade concluir(Long empresaId, Long id);
    void excluirLead(Long empresaId, Long id);
}
