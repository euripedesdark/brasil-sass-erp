package br.com.brasil_saas.fiscal.service;

import br.com.brasil_saas.vendas.model.PedidoVenda;
import br.com.brasil_saas.fiscal.model.Nfe;
import java.math.BigDecimal;

public interface NFeService {
    /**
     * Emite uma NFe a partir de um Pedido de Venda.
     *
     * @param empresaId ID da empresa emitente
     * @param pedido Pedido de venda a ser faturado
     * @return O protocolo de autorização da nota
     * @throws Exception Se houver erro na emissão ou rejeição da SEFAZ
     */
    String emitirNFe(Long empresaId, PedidoVenda pedido) throws Exception;

    /**
     * Cancela uma NFe emitida.
     *
     * @param empresaId ID da empresa
     * @param chaveAcesso Chave de 44 dígitos da NFe
     * @param motivo Motivo do cancelamento
     * @return Resultado do cancelamento
     */
    String cancelarNFe(Long empresaId, String chaveAcesso, String motivo) throws Exception;

    /**
     * Consulta a situação de uma NFe na SEFAZ.
     */
    String consultarSituacao(Long empresaId, String chaveAcesso) throws Exception;

    Nfe consultarPersistida(Long empresaId, String chaveAcesso);
}
