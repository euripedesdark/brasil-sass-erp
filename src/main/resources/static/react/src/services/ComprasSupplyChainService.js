import axios from 'axios';

const BASE = '/api/compras/supply-chain';

/**
 * Fluxo de suprimentos: solicitação interna, aprovação, cotação multi-fornecedor,
 * mapa comparativo e geração do pedido.
 *
 * A empresa não vai na URL. Mandá-la na query permitia pedir ou gravar dados de
 * outra empresa — o backend agora tira tudo do token.
 */
export default {
    listarSolicitacoes: () => axios.get(`${BASE}/solicitacoes`),
    criarSolicitacao: (solicitanteId, body) => axios.post(
        `${BASE}/solicitacoes`, body,
        { params: solicitanteId ? { solicitanteId } : {} }),
    aprovar: (id) => axios.post(`${BASE}/solicitacoes/${id}/aprovar`),
    criarCotacao: (solicitacaoId, body) => axios.post(
        `${BASE}/cotacoes`, body, { params: { solicitacaoId } }),
    mapa: (id) => axios.get(`${BASE}/cotacoes/${id}/mapa`),
    gerarPedido: (cotacaoFornecedorId) => axios.post(
        `${BASE}/cotacoes/fornecedor/${cotacaoFornecedorId}/gerar-pedido`)
};
