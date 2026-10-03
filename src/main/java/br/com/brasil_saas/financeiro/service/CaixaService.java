package br.com.brasil_saas.financeiro.service;

import br.com.brasil_saas.financeiro.dto.CaixaDtos.CaixaRequest;
import br.com.brasil_saas.financeiro.model.Caixa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Caixa: cofre / conta de caixa.
 *
 * Toda operacao recebe o {@code empresaId} porque o tenant vem do token.
 * Sem isso, trocar o id na URL daria acesso ao caixa de outra empresa — o
 * mesmo problema que foi fechado no PedidoVendaController.
 */
public interface CaixaService {

    Page<Caixa> listar(Long empresaId, Pageable pageable, String termo);

    Caixa buscarPorId(Long id, Long empresaId);

    Caixa criar(CaixaRequest request, Long empresaId);

    Caixa atualizar(Long id, CaixaRequest request, Long empresaId);

    /**
     * Desativa em vez de apagar.
     *
     * Apagar caixa que ja teve movimento quebra o historico. Desativar
     * tira da lista ativa e mantem a referencia.
     */
    void desativar(Long id, Long empresaId);
}
