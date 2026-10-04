package br.com.brasil_saas.fiscal.service;

import br.com.brasil_saas.vendas.model.PedidoVenda;
import br.com.brasil_saas.fiscal.model.Nfe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NFeService {
    String emitirNFe(Long empresaId, PedidoVenda pedido) throws Exception;
    String cancelarNFe(Long empresaId, String chaveAcesso, String motivo) throws Exception;
    String consultarSituacao(Long empresaId, String chaveAcesso) throws Exception;
    Nfe consultarPersistida(Long empresaId, String chaveAcesso);
    Page<Nfe> listar(Long empresaId, Pageable pageable);
    Nfe consultarPersistidaPorId(Long empresaId, Long id);
}
