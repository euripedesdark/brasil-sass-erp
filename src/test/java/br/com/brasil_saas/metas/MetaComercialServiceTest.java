package br.com.brasil_saas.metas;
import br.com.brasil_saas.metas.model.MetaComercial;
import br.com.brasil_saas.metas.repository.MetaComercialRepository;
import br.com.brasil_saas.metas.service.MetaComercialService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class MetaComercialServiceTest {
    @Mock MetaComercialRepository repository;
    @InjectMocks MetaComercialService service;
    @Test void salvaMeta() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        MetaComercial m = new MetaComercial();
        m.setAno(2026); m.setMes(10); m.setValorMeta(new BigDecimal("1000"));
        MetaComercial s = service.salvar(1L, m);
        assertEquals(1L, s.getEmpresaId());
    }
}
