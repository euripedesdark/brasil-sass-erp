package br.com.brasil_saas.workflow.service;
import br.com.brasil_saas.workflow.model.*;
import java.util.List;
public interface WorkflowService {
    List<WkfDefinition> definitions(Long empresaId);
    WkfDefinition salvarDefinition(Long empresaId, Long userId, WkfDefinition d);
    WkfDefinition addStage(Long empresaId, Long definitionId, WkfStage s);
    List<WkfStage> stages(Long empresaId, Long definitionId);
    WkfInstance abrir(Long empresaId, Long userId, String entidadeTipo, Long entidadeId, Long definitionId, String observacao);
    List<WkfInstance> instances(Long empresaId, String status);
    List<WkfTask> tasks(Long empresaId, Long instanceId);
    List<WkfTask> pendentes(Long empresaId);
    java.util.Optional<WkfInstance> instanciaPara(Long empresaId, String entidadeTipo, Long entidadeId);
    WkfTask decidir(Long empresaId, Long userId, Long taskId, boolean aprovar, String comentario);
    void inativarDefinition(Long empresaId, Long id);
}
