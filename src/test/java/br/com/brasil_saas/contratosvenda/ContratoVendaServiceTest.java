package br.com.brasil_saas.contratosvenda;
import br.com.brasil_saas.contratosvenda.model.ContratoVenda;
import br.com.brasil_saas.contratosvenda.repository.ContratoVendaRepository;
import br.com.brasil_saas.contratosvenda.service.ContratoVendaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class ContratoVendaServiceTest {
    @Mock ContratoVendaRepository repository;
    @InjectMocks ContratoVendaService service;
    @Test void criaRascunho() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        ContratoVenda c = new ContratoVenda();
        c.setTitulo("Contrato X"); c.setClienteId(10L);
        ContratoVenda s = service.salvar(1L, c);
        assertEquals("RASCUNHO", s.getStatus());
        assertNotNull(s.getNumero());
    }
}
