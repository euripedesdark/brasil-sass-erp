package br.com.brasil_saas.contabilidade.service.impl;

import br.com.brasil_saas.contabilidade.model.CtbFechamento;
import br.com.brasil_saas.contabilidade.model.CtbLancamento;
import br.com.brasil_saas.contabilidade.model.CtbPartida;
import br.com.brasil_saas.contabilidade.repository.CtbFechamentoRepository;
import br.com.brasil_saas.contabilidade.repository.CtbLancamentoRepository;
import br.com.brasil_saas.contabilidade.repository.CtbPartidaRepository;
import br.com.brasil_saas.financeiro.model.PlanoContas;
import br.com.brasil_saas.financeiro.repository.PlanoContasRepository;
import br.com.brasil_saas.financeiro.repository.TituloRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContabilidadeApuracaoTest {
    @Mock CtbLancamentoRepository lancamentos;
    @Mock CtbPartidaRepository partidas;
    @Mock CtbFechamentoRepository fechamentos;
    @Mock PlanoContasRepository contas;
    @Mock TituloRepository titulos;
    @InjectMocks ContabilidadeServiceImpl service;

    private PlanoContas conta(Long id, String codigo) {
        var c = new PlanoContas();
        c.setId(id); c.setCodigo(codigo);
        return c;
    }

    private CtbFechamento fechado(int mes) {
        var f = new CtbFechamento();
        f.setPeriodo(String.format("%04d-%02d", 2024, mes)); f.setStatus("FECHADO");
        return f;
    }

    private void anoFechadoJanNov() {
        var lista = new ArrayList<CtbFechamento>();
        for (int m = 1; m <= 11; m++) lista.add(fechado(m));
        when(fechamentos.findByEmpresaIdAndDeletedAtIsNull(1L)).thenReturn(lista);
    }

    private CtbLancamento lancadoMarco() {
        var l = new CtbLancamento();
        l.setId(50L); l.setData(LocalDate.of(2024, 3, 10));
        l.setPeriodo("2024-03"); l.setStatus("LANCADO");
        return l;
    }

    private CtbPartida partida(Long conta, String debito, String credito) {
        var p = new CtbPartida();
        p.setContaId(conta);
        p.setDebito(new BigDecimal(debito)); p.setCredito(new BigDecimal(credito));
        return p;
    }

    private void contasLucrosEReceita() {
        when(contas.findByIdAndEmpresaIdAndDeletedAtIsNull(900L, 1L))
                .thenReturn(Optional.of(conta(900L, "2.3.01")));
    }

    private void contaReceita301() {
        when(contas.findByIdAndEmpresaIdAndDeletedAtIsNull(301L, 1L))
                .thenReturn(Optional.of(conta(301L, "3.1.01")));
    }

    private void semRascunhoSemApurado() {
        when(lancamentos.findByEmpresaIdAndPeriodoAndDeletedAtIsNullOrderByDataDescIdDesc(eq(1L), eq("2024-12")))
                .thenReturn(List.of());
        when(lancamentos.findByEmpresaIdAndDeletedAtIsNull(1L)).thenReturn(List.of());
    }

    @Test
    void apuraLucroContraLucrosAcumulados() {
        contasLucrosEReceita();
        anoFechadoJanNov();
        semRascunhoSemApurado();
        when(lancamentos.findByEmpresaIdAndDeletedAtIsNull(1L))
                .thenReturn(List.of(lancadoMarco()));
        when(partidas.findByLancamentoIdAndEmpresaIdAndDeletedAtIsNull(50L, 1L))
                .thenReturn(List.of(partida(301L, "0", "100.00")));
        contaReceita301();
        var gravadas = new ArrayList<CtbPartida>();
        when(partidas.save(any())).thenAnswer(i -> {
            CtbPartida p = i.getArgument(0); gravadas.add(p); return p;
        });
        when(lancamentos.save(any())).thenAnswer(i -> {
            CtbLancamento l = i.getArgument(0); l.setId(60L); return l;
        });
        when(lancamentos.findByIdForUpdate(60L, 1L)).thenAnswer(i -> {
            var l = new CtbLancamento();
            l.setId(60L); l.setStatus("RASCUNHO"); l.setPeriodo(lancadoDezembro()); l.setOrigemId(8L); l.setOrigemTipo("ENCERRAMENTO");
            return Optional.of(l);
        });
        when(partidas.findByLancamentoIdAndEmpresaIdAndDeletedAtIsNull(60L, 1L))
                .thenAnswer(i -> List.copyOf(gravadas));
        var r = service.apurarResultado(1L, 9L, 2024, 900L);
        assertEquals("LANCADO", r.getStatus());
        assertEquals("2024-12", r.getPeriodo());
        assertEquals(2, gravadas.size());
        var porConta = new java.util.HashMap<Long, CtbPartida>();
        for (var g : gravadas) porConta.put(g.getContaId(), g);
        assertEquals(0, new BigDecimal("100.00").compareTo(porConta.get(301L).getDebito()));
        assertEquals(0, new BigDecimal("100.00").compareTo(porConta.get(900L).getCredito()));
        assertEquals("ENCERRAMENTO", r.getOrigemTipo());
    }

    private String lancadoDezembro() {
        return "2024-12";
    }

    @Test
    void apuraPrejuizoInverteLados() {
        contasLucrosEReceita();
        anoFechadoJanNov();
        semRascunhoSemApurado();
        when(lancamentos.findByEmpresaIdAndDeletedAtIsNull(1L))
                .thenReturn(List.of(lancadoMarco()));
        when(partidas.findByLancamentoIdAndEmpresaIdAndDeletedAtIsNull(50L, 1L))
                .thenReturn(List.of(partida(301L, "50.00", "0")));
        contaReceita301();
        var gravadas = new ArrayList<CtbPartida>();
        when(partidas.save(any())).thenAnswer(i -> {
            CtbPartida p = i.getArgument(0); gravadas.add(p); return p;
        });
        when(lancamentos.save(any())).thenAnswer(i -> {
            CtbLancamento l = i.getArgument(0); l.setId(60L); return l;
        });
        when(lancamentos.findByIdForUpdate(60L, 1L)).thenAnswer(i -> {
            var l = new CtbLancamento();
            l.setId(60L); l.setStatus("RASCUNHO"); l.setPeriodo(lancadoDezembro()); l.setOrigemId(8L); l.setOrigemTipo("ENCERRAMENTO");
            return Optional.of(l);
        });
        when(partidas.findByLancamentoIdAndEmpresaIdAndDeletedAtIsNull(60L, 1L))
                .thenAnswer(i -> List.copyOf(gravadas));
        service.apurarResultado(1L, 9L, 2024, 900L);
        var porConta = new java.util.HashMap<Long, CtbPartida>();
        for (var g : gravadas) porConta.put(g.getContaId(), g);
        assertEquals(0, new BigDecimal("50.00").compareTo(porConta.get(900L).getDebito()));
        assertEquals(0, new BigDecimal("50.00").compareTo(porConta.get(301L).getCredito()));
    }

    @Test
    void recusaSemJanNovFechados() {
        contasLucrosEReceita();
        when(fechamentos.findByEmpresaIdAndDeletedAtIsNull(1L)).thenReturn(List.of(fechado(1)));
        assertThrows(ResponseStatusException.class, () -> service.apurarResultado(1L, 9L, 2024, 900L));
        verify(lancamentos, never()).save(any());
    }

    @Test
    void recusaDezembroFechado() {
        contasLucrosEReceita();
        var lista = new ArrayList<CtbFechamento>();
        for (int m = 1; m <= 12; m++) lista.add(fechado(m));
        when(fechamentos.findByEmpresaIdAndDeletedAtIsNull(1L)).thenReturn(lista);
        assertThrows(ResponseStatusException.class, () -> service.apurarResultado(1L, 9L, 2024, 900L));
        verify(lancamentos, never()).save(any());
    }

    @Test
    void recusaJaApurado() {
        contasLucrosEReceita();
        anoFechadoJanNov();
        var apurado = new CtbLancamento();
        apurado.setOrigemTipo("ENCERRAMENTO"); apurado.setStatus("LANCADO");
        when(lancamentos.findByEmpresaIdAndPeriodoAndDeletedAtIsNullOrderByDataDescIdDesc(eq(1L), eq("2024-12")))
                .thenReturn(List.of(apurado));
        assertThrows(ResponseStatusException.class, () -> service.apurarResultado(1L, 9L, 2024, 900L));
        verify(lancamentos, never()).save(any());
    }

    @Test
    void recusaExercicioFuturoEContaDeResultado() {
        assertThrows(ResponseStatusException.class, () -> service.apurarResultado(1L, 9L, 2099, 900L));
        when(contas.findByIdAndEmpresaIdAndDeletedAtIsNull(301L, 1L))
                .thenReturn(Optional.of(conta(301L, "3.1.01")));
        assertThrows(ResponseStatusException.class, () -> service.apurarResultado(1L, 9L, 2024, 301L));
        verifyNoInteractions(lancamentos, partidas, fechamentos);
    }

    @Test
    void recusaSemMovimentoDeResultado() {
        contasLucrosEReceita();
        anoFechadoJanNov();
        semRascunhoSemApurado();
        when(lancamentos.findByEmpresaIdAndDeletedAtIsNull(1L))
                .thenReturn(List.of(lancadoMarco()));
        when(partidas.findByLancamentoIdAndEmpresaIdAndDeletedAtIsNull(50L, 1L))
                .thenReturn(List.of(partida(100L, "100.00", "0")));
        when(contas.findByIdAndEmpresaIdAndDeletedAtIsNull(100L, 1L))
                .thenReturn(Optional.of(conta(100L, "1.1.01")));
        assertThrows(ResponseStatusException.class, () -> service.apurarResultado(1L, 9L, 2024, 900L));
        verify(lancamentos, never()).save(any());
    }
}
