package br.com.brasil_saas.agenda;

import br.com.brasil_saas.agenda.model.EventoAgenda;
import br.com.brasil_saas.agenda.repository.EventoAgendaRepository;
import br.com.brasil_saas.agenda.service.AgendaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {
    @Mock EventoAgendaRepository repository;
    @InjectMocks AgendaService service;

    @Test
    void salvaEvento() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        EventoAgenda e = new EventoAgenda();
        e.setTitulo("Reunião");
        e.setInicio(LocalDateTime.now());
        EventoAgenda s = service.salvar(9L, e);
        assertEquals("AGENDADO", s.getStatus());
        assertEquals(9L, s.getEmpresaId());
    }
}
