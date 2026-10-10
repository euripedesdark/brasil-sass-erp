package br.com.brasil_saas.crm;
import br.com.brasil_saas.crm.model.*; import br.com.brasil_saas.crm.repository.*; import br.com.brasil_saas.crm.service.CrmCampanhaService;
import org.junit.jupiter.api.Test; import org.springframework.web.server.ResponseStatusException;
import java.util.*; import static org.junit.jupiter.api.Assertions.*; import static org.mockito.Mockito.*;
class CrmCampanhaServiceTest {
 private final CrmCampanhaRepository campanhas=mock(CrmCampanhaRepository.class);
 private final CrmCampanhaContatoRepository contatos=mock(CrmCampanhaContatoRepository.class);
 private final CrmLeadRepository leads=mock(CrmLeadRepository.class);
 private final CrmCampanhaService svc=new CrmCampanhaService(campanhas,contatos,leads);
 private CrmCampanha campanha(String status) {
  CrmCampanha c=new CrmCampanha();c.setId(10L);c.setEmpresaId(1L);c.setNome("Campanha");c.setStatus(status);
  when(campanhas.findByIdAndEmpresaIdAndDeletedAtIsNull(10L,1L)).thenReturn(Optional.of(c));return c;
 }
 @Test void naoAceitaEmpresaDoPayload() {
  CrmCampanha c=new CrmCampanha();c.setNome("Prospeccao");c.setEmpresaId(99L);c.setStatus("ENCERRADA");
  when(campanhas.save(any())).thenAnswer(a->a.getArgument(0));
  CrmCampanha salvo=svc.criar(1L,c);
  assertEquals(1L,salvo.getEmpresaId());assertEquals("RASCUNHO",salvo.getStatus());
 }
 @Test void rejeitaLeadDeOutroTenant() {
  campanha("ATIVA");assertThrows(ResponseStatusException.class,()->svc.vincular(1L,10L,88L));
  verify(contatos,never()).save(any());
 }
 @Test void evitaDuplicidade() {
  campanha("ATIVA");when(leads.findByIdAndEmpresaIdAndDeletedAtIsNull(7L,1L)).thenReturn(Optional.of(new CrmLead()));
  when(contatos.existsByCampanhaIdAndLeadIdAndEmpresaIdAndDeletedAtIsNull(10L,7L,1L)).thenReturn(true);
  assertThrows(ResponseStatusException.class,()->svc.vincular(1L,10L,7L));
 }
 @Test void bloqueiaCampanhaEncerrada() {
  campanha("ENCERRADA");assertThrows(ResponseStatusException.class,()->svc.vincular(1L,10L,7L));
 }
 @Test void calculaResumo() {
  campanha("ATIVA");CrmCampanhaContato a=new CrmCampanhaContato();a.setStatus("CONVERTIDO");
  CrmCampanhaContato b=new CrmCampanhaContato();b.setStatus("RESPONDEU");
  when(contatos.findByCampanhaIdAndEmpresaIdAndDeletedAtIsNull(10L,1L)).thenReturn(List.of(a,b));
  Map<String,Object> out=svc.resumo(1L,10L);
  assertEquals(2,out.get("totalLeads"));assertEquals(2L,out.get("respostas"));assertEquals(1L,out.get("conversoes"));
 }
 @Test void rejeitaStatusInvalido() {
  campanha("RASCUNHO");assertThrows(ResponseStatusException.class,()->svc.mudarStatus(1L,10L,"ENCERRADA"));
 }
}
