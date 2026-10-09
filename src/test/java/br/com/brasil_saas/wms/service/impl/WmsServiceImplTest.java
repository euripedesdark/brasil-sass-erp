package br.com.brasil_saas.wms.service.impl;

import br.com.brasil_saas.cadastro.repository.ProdutoRepository;
import br.com.brasil_saas.estoque.repository.*;
import br.com.brasil_saas.wms.model.*;
import br.com.brasil_saas.wms.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WmsServiceImplTest {
    @Mock WmsOndaRepository ondas;
    @Mock WmsOndaItemRepository itens;
    @Mock WmsVolumeRepository volumes;
    @Mock WmsVolumeItemRepository volumeItens;
    @Mock EnderecoEstoqueRepository enderecos;
    @Mock ExpedicaoEstoqueRepository expedicoes;
    @Mock ExpedicaoEstoqueItemRepository expedicaoItens;
    @Mock ReservaEstoqueRepository reservas;
    @Mock ProdutoRepository produtos;
    @Mock br.com.brasil_saas.core.service.DocumentoFluxoService documentoFluxo;
    @InjectMocks WmsServiceImpl service;

    private WmsOndaItem itemSeparado(String separado) {
        var oi = new WmsOndaItem();
        oi.setId(9L); oi.setProdutoId(6L);
        oi.setQtdSolicitada(new BigDecimal("10"));
        oi.setQtdSeparada(new BigDecimal(separado));
        return oi;
    }

    private WmsVolume volumeAberto() {
        var v = new WmsVolume();
        v.setId(3L); v.setStatus("ABERTO");
        when(volumes.findByIdAndEmpresaIdAndDeletedAtIsNull(3L, 2L)).thenReturn(Optional.of(v));
        return v;
    }

    private WmsVolumeItem embalagem(String qtd) {
        var i = new WmsVolumeItem();
        i.setProdutoId(6L); i.setOndaItemId(9L); i.setQuantidade(new BigDecimal(qtd));
        return i;
    }

    @Test
    void separarRejeitaQuantidadeNegativaOuZero() {
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> service.separar(2L, 1L, 9L, new BigDecimal("-1"), null)).getStatusCode().value());
        assertThrows(ResponseStatusException.class, () -> service.separar(2L, 1L, 9L, null, null));
        verifyNoInteractions(itens);
    }

    @Test
    void embalarNaoPassaDaQuantidadeSeparadaSomandoVolumes() {
        volumeAberto();
        when(produtos.findByIdAndEmpresaIdAndDeletedAtIsNull(6L, 2L)).thenReturn(Optional.of(new br.com.brasil_saas.cadastro.model.Produto()));
        when(itens.findByIdAndEmpresaIdAndDeletedAtIsNull(9L, 2L)).thenReturn(Optional.of(itemSeparado("6")));
        when(volumeItens.findByOndaItemIdAndEmpresaIdAndDeletedAtIsNull(9L, 2L))
                .thenReturn(List.of(embalagem("4")));
        var ex = assertThrows(ResponseStatusException.class, () -> service.embalar(2L, 3L, embalagem("3")));
        assertTrue(ex.getReason().contains("acima da separada"));
        verify(volumeItens, never()).save(any());
    }

    @Test
    void embalarAceitaAteOLimiteSeparado() {
        volumeAberto();
        when(produtos.findByIdAndEmpresaIdAndDeletedAtIsNull(6L, 2L)).thenReturn(Optional.of(new br.com.brasil_saas.cadastro.model.Produto()));
        when(itens.findByIdAndEmpresaIdAndDeletedAtIsNull(9L, 2L)).thenReturn(Optional.of(itemSeparado("6")));
        when(volumeItens.findByOndaItemIdAndEmpresaIdAndDeletedAtIsNull(9L, 2L))
                .thenReturn(List.of(embalagem("4")));
        when(volumeItens.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals(new BigDecimal("2"), service.embalar(2L, 3L, embalagem("2")).getQuantidade());
    }

    @Test
    void naoFechaVolumeVazio() {
        volumeAberto();
        when(volumeItens.findByVolumeIdAndEmpresaIdAndDeletedAtIsNull(3L, 2L)).thenReturn(List.of());
        assertThrows(ResponseStatusException.class, () -> service.fecharVolume(2L, 3L));
        verify(volumes, never()).save(any());
    }

    @Test
    void fecharVolumeJaFechadoEIdempotente() {
        var v = volumeAberto();
        v.setStatus("FECHADO");
        assertSame(v, service.fecharVolume(2L, 3L));
        verify(volumes, never()).save(any());
    }
}
