package br.com.brasil_saas.projetos;
import br.com.brasil_saas.projetos.model.*; import br.com.brasil_saas.projetos.repository.*; import br.com.brasil_saas.projetos.service.ProjetoDependenciaService;
import org.junit.jupiter.api.Test; import org.springframework.web.server.ResponseStatusException;
import java.util.*; import static org.junit.jupiter.api.Assertions.*; import static org.mockito.Mockito.*;
class ProjetoDependenciaServiceTest {
 private final PrjProjetoRepository projetos=mock(PrjProjetoRepository.class);
 private final PrjEtapaRepository etapas=mock(PrjEtapaRepository.class);
 private final PrjDependenciaRepository repo=mock(PrjDependenciaRepository.class);
 private final ProjetoDependenciaService svc=new ProjetoDependenciaService(projetos,etapas,repo);
 private void setup(){
  when(projetos.findByIdAndEmpresaIdAndDeletedAtIsNull(1L,2L)).thenReturn(Optional.of(new PrjProjeto()));
  for(long id=10;id<=12;id++){PrjEtapa e=new PrjEtapa();e.setId(id);e.setProjetoId(1L);
   when(etapas.findByIdAndEmpresaIdAndDeletedAtIsNull(id,2L)).thenReturn(Optional.of(e));}
 }
 private PrjDependencia edge(long a,long b){PrjDependencia d=new PrjDependencia();d.setAntecessoraId(a);d.setSucessoraId(b);return d;}
 @Test void detectaCicloTransitivo(){
  setup();when(repo.findByProjetoIdAndEmpresaIdAndDeletedAtIsNull(1L,2L)).thenReturn(List.of(edge(10,11),edge(11,12)));
  assertThrows(ResponseStatusException.class,()->svc.adicionar(2L,1L,12L,10L,0));verify(repo,never()).save(any());
 }
 @Test void bloqueiaInicioSemAntecessoraConcluida(){
  setup();when(repo.findByProjetoIdAndEmpresaIdAndDeletedAtIsNull(1L,2L)).thenReturn(List.of(edge(10,11)));
  assertThrows(ResponseStatusException.class,()->svc.validarAvanco(2L,1L,11L));
 }
 @Test void criaDependenciaValida(){
  setup();when(repo.findByProjetoIdAndEmpresaIdAndDeletedAtIsNull(1L,2L)).thenReturn(List.of());
  when(repo.save(any())).thenAnswer(a->a.getArgument(0));
  PrjDependencia d=svc.adicionar(2L,1L,10L,11L,0);
  assertEquals(2L,d.getEmpresaId());assertEquals(10L,d.getAntecessoraId());assertEquals(11L,d.getSucessoraId());
 }
 @Test void protegeContraEtapaDeOutroProjeto(){
  setup();PrjEtapa e=new PrjEtapa();e.setProjetoId(99L);
  when(etapas.findByIdAndEmpresaIdAndDeletedAtIsNull(12L,2L)).thenReturn(Optional.of(e));
  assertThrows(ResponseStatusException.class,()->svc.adicionar(2L,1L,10L,12L,0));
 }
 @Test void semAutodependencia(){
  setup();assertThrows(ResponseStatusException.class,()->svc.adicionar(2L,1L,10L,10L,0));
 }
}
