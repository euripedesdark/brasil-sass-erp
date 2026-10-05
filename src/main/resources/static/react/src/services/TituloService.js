import axios from 'axios';
import ApiConfig from './ApiConfig';

const API_URL = `${ApiConfig.BASE_URL || ''}/api/financeiro/titulos`;
const STRIPE_API_URL = `${ApiConfig.BASE_URL || ''}/api/financeiro/stripe`;

class TituloService {
    /** status opcional: ABERTO, PENDENTE_APROVACAO, BAIXADO, CANCELADO... */
    listar(status) {
        const params = {};
        if (status) params.status = status;
        return axios.get(API_URL, { params, withCredentials: true });
    }

    // ===== Workflow de aprovação (Fase 1 — Relatório Paridade 25/09/2026) =====

    /** SolicitacaoAprovacaoRequest: fluxoAprovacaoId?, niveis?, usuarioAprovadorId?, observacao? */
    solicitarAprovacao(id, body) {
        return axios.post(`${API_URL}/${id}/aprovacao/solicitar`, body, { withCredentials: true });
    }

    aprovar(aprovacaoId, observacao) {
        return axios.post(`${API_URL}/aprovacoes/${aprovacaoId}/aprovar`,
            { acao: 'APROVAR', observacao: observacao || null }, { withCredentials: true });
    }

    rejeitar(aprovacaoId, observacao) {
        return axios.post(`${API_URL}/aprovacoes/${aprovacaoId}/rejeitar`,
            { acao: 'REJEITAR', observacao: observacao || null }, { withCredentials: true });
    }

    aprovacoesPendentes() {
        return axios.get(`${API_URL}/aprovacoes/pendentes`, { withCredentials: true });
    }

    aprovacoesDoTitulo(id) {
        return axios.get(`${API_URL}/${id}/aprovacoes`, { withCredentials: true });
    }

    buscar(id) {
        return axios.get(`${API_URL}/${id}`, { withCredentials: true });
    }

    parcelas(id) {
        return axios.get(`${API_URL}/${id}/parcelas`, { withCredentials: true });
    }

    gerarParcelas(id, condicaoPagamentoId) {
        return axios.post(
            `${API_URL}/${id}/parcelas`,
            null,
            { params: { condicaoPagamentoId }, withCredentials: true }
        );
    }

    /**
     * BaixaRequest: parcelaId?, contaBancariaId?, tipoPagamentoId?,
     * dataBaixa, valorBaixa, valorDesconto?, valorJuro?, valorMulta?, observacao?
     */
    baixar(id, body) {
        return axios.post(`${API_URL}/${id}/baixar`, body, { withCredentials: true });
    }

    vencimentos(inicio, fim) {
        return axios.get(`${API_URL}/vencimentos`, {
            params: { inicio, fim },
            withCredentials: true,
        });
    }
}

export default new TituloService();
