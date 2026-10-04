package br.com.brasil_saas.wms.service;
import br.com.brasil_saas.wms.model.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
public interface WmsService {
    List<WmsOnda> ondas(Long empresaId, String status);
    WmsOnda criarOnda(Long empresaId, WmsOnda o);
    WmsOndaItem addItem(Long empresaId, Long ondaId, WmsOndaItem i);
    java.util.Map<String, Object> gerarOndaDeReservas(Long empresaId, Long depositoId);
    List<WmsOndaItem> itens(Long empresaId, Long ondaId);
    WmsOnda liberar(Long empresaId, Long id);
    WmsOndaItem separar(Long empresaId, Long ondaId, Long itemId, BigDecimal qtd, Long enderecoId);
    WmsOnda concluir(Long empresaId, Long id);
    Map<String, Object> putaway(Long empresaId, Long depositoId, Long produtoId);
    List<WmsVolume> volumes(Long empresaId, Long expedicaoId);
    WmsVolume criarVolume(Long empresaId, WmsVolume v);
    WmsVolumeItem embalar(Long empresaId, Long volumeId, WmsVolumeItem i);
    WmsVolume fecharVolume(Long empresaId, Long id);
    Map<String, Object> conferir(Long empresaId, Long expedicaoId);
    Map<String, Object> finalizarExpedicao(Long empresaId, Long expedicaoId, String codigoRastreio);
}
