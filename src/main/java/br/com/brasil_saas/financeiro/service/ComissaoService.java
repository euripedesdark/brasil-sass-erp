package br.com.brasil_saas.financeiro.service;

import br.com.brasil_saas.financeiro.dto.ComissaoDtos.RegraRequest;
import br.com.brasil_saas.financeiro.model.Comissao;
import br.com.brasil_saas.vendas.model.RegraComissao;

import java.util.List;

/**
 * Comissao do vendedor.
 *
 * Todas as operacoes recebem o {@code empresaId}: o tenant vem do token, no
 * mesmo padrao aplicado ao PedidoVendaController depois de uma IDOR em que
 * trocar o id na URL dava acesso a comissao de outra empresa.
 */
public interface ComissaoService {

    List<Comissao> listar(Long empresaId, String status, Long funcionarioId);

    /**
     * Marca a comissao como paga.
     *
     * @throws br.com.brasil_saas.shared.exception.BusinessException
     *         se ja estiver paga — nao ha pagamento duplo.
     */
    Comissao pagar(Long id, Long empresaId);

    // --- regras -----------------------------------------------------------
    // Sem regra cadastrada, calcularPercentualComissao devolve 0,00% e a
    // comissao e gravada zerada sem nenhum aviso. O crud existe para que a
    // regra possa ser cadastrada pela interface.

    List<RegraComissao> listarRegras(Long empresaId, Long vendedorId);

    RegraComissao criarRegra(RegraRequest request, Long empresaId);

    RegraComissao atualizarRegra(Long id, RegraRequest request, Long empresaId);

    void excluirRegra(Long id, Long empresaId);
    int estornarPorPedido(Long empresaId, Long pedidoId, String motivo);
}
