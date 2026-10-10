package br.com.brasil_saas.workflow;
import br.com.brasil_saas.workflow.model.*; import br.com.brasil_saas.workflow.repository.*; import br.com.brasil_saas.workflow.service.impl.WorkflowServiceImpl;
import org.junit.jupiter.api.Test; import org.springframework.web.server.ResponseStatusException;
import java.util.*; import static org.junit.jupiter.api.Assertions.*; import static org.mockito.Mockito.*;
class WorkflowDelegacaoServiceTest {
 private final WkfDefinitionRepository defs=mock(WkfDefinitionRepository.class);
 private final WkfStageRepository stages=mock(WkfStageRepository.class);
 private final WkfInstanceRepository instances=mock(WkfInstanceRepository.class);
 private final WkfTaskRepository tasks=mock(WkfTaskRepository.class);
 private final WorkflowServiceImpl svc=new WorkflowServiceImpl(defs,stages,instances,tasks);
 private WkfTask pendente(){
  WkfTask t=new WkfTask();t.setId(100L);t.setInstanceId(200L);t.setResponsavel("GESTOR");t.setStatus("PENDENTE");
  WkfInstance i=new WkfInstance();i.setStatus("EM_ANDAMENTO");
  when(tasks.findByIdAndEmpresaIdAndDeletedAtIsNull(100L,1L)).thenReturn(Optional.of(t));
  when(instances.findByIdAndEmpresaIdAndDeletedAtIsNull(200L,1L)).thenReturn(Optional.of(i));
  when(tasks.save(any())).thenAnswer(a->a.getArgument(0));return t;
 }
 @Test void delegacaoGeraTrilha(){
  WkfTask t=pendente();svc.delegar(1L,9L,100L,25L);
  assertEquals("GESTOR",t.getResponsavelAnterior());assertEquals("25",t.getResponsavel());
  assertEquals(9L,t.getDelegadoPor());assertNotNull(t.getDelegadoEm());
 }
 @Test void antigoResponsavelNaoPodeDecidir(){
  pendente();svc.delegar(1L,9L,100L,25L);
  assertThrows(ResponseStatusException.class,()->svc.decidir(1L,9L,100L,true,null));
 }
 @Test void tarefaJaDecididaNaoDelegavel(){
  WkfTask t=pendente();t.setStatus("APROVADA");
  assertThrows(ResponseStatusException.class,()->svc.delegar(1L,9L,100L,25L));
 }
 @Test void destinoNaoPodeSerMesmoResponsavel(){
  WkfTask t=pendente();t.setResponsavel("25");
  assertThrows(ResponseStatusException.class,()->svc.delegar(1L,9L,100L,25L));
 }
}
