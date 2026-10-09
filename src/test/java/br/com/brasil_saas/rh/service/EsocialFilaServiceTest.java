package br.com.brasil_saas.rh.service;

import br.com.brasil_saas.rh.model.EsocialEvento;
import br.com.brasil_saas.rh.repository.EsocialEventoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EsocialFilaServiceTest {

    @Mock EsocialEventoRepository repo;
    @InjectMocks EsocialFilaService svc;

    @Test
    void tiposIncluemAdmissaoETabelas() {
        Map<String, String> t = svc.tiposSuportados();
        assertEquals("ADMISSAO_TRABALHADOR", t.get("S-2200"));
        assertEquals("INFORMACOES_EMPREGADOR", t.get("S-1000"));
        assertEquals("REMUNERACAO_RGPS", t.get("S-1200"));
        assertEquals("FECHAMENTO_PERIODICOS", t.get("S-1299"));
        assertTrue(t.size() >= 20);
    }

    @Test
    void rejeitaTipoDesconhecido() {
        assertThrows(ResponseStatusException.class,
                () -> svc.registrar(1L, "S-9999", 10L, "{}"));
    }

    @Test
    void registraTipoValido() {
        when(repo.save(any())).thenAnswer(inv -> {
            EsocialEvento e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });
        EsocialEvento e = svc.registrar(1L, "S-2200", 99L, "{\"cpf\":\"123\"}");
        assertEquals("PENDENTE", e.getStatus());
        assertEquals("S-2200", e.getTipo());
        assertEquals(99L, e.getFuncionarioId());
    }
}
