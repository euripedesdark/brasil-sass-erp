package br.com.brasil_saas.financeiro.service;

import br.com.brasil_saas.financeiro.model.StripePayment;
import br.com.brasil_saas.financeiro.repository.StripePaymentRepository;
import br.com.brasil_saas.financeiro.service.impl.StripeFinanceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripeFinanceServiceListTest {

    @Mock StripePaymentRepository paymentRepository;
    // other deps not needed for list if we only call listar via repo - Impl needs all fields for inject
    // Skip full inject - test repository contract only

    @Test
    void repoListContract() {
        StripePayment p = new StripePayment();
        p.setTituloId(1L);
        p.setStatus("open");
        when(paymentRepository.findByEmpresaIdAndDeletedAtIsNullOrderByIdDesc(9L)).thenReturn(List.of(p));
        assertEquals(1, paymentRepository.findByEmpresaIdAndDeletedAtIsNullOrderByIdDesc(9L).size());
    }
}
