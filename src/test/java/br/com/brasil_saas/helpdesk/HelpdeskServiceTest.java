package br.com.brasil_saas.helpdesk;

import br.com.brasil_saas.helpdesk.model.Chamado;
import br.com.brasil_saas.helpdesk.repository.ChamadoComentarioRepository;
import br.com.brasil_saas.helpdesk.repository.ChamadoRepository;
import br.com.brasil_saas.helpdesk.service.HelpdeskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HelpdeskServiceTest {
    @Mock ChamadoRepository chamadoRepository;
    @Mock ChamadoComentarioRepository comentarioRepository;
    @InjectMocks HelpdeskService service;

    @Test
    void criaComNumeroEStatusAberto() {
        when(chamadoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Chamado c = new Chamado();
        c.setTitulo("Teste");
        Chamado s = service.salvar(1L, c);
        assertEquals("ABERTO", s.getStatus());
        assertNotNull(s.getNumero());
        assertEquals(1L, s.getEmpresaId());
    }
}
