import { api } from './ApiConfig';

// A tela Caixa.jsx chamava "/api/caixa", que nao existia em lugar nenhum do
// backend: nem entidade, nem tabela, nem controller. O caminho segue o padrao
// do modulo financeiro (/api/financeiro/...).
//
// O import tambem estava errado: o default de ApiConfig e o objeto de
// configuracao, sem `.get`; a instancia do axios e o export nomeado `api`.
const BASE = '/api/financeiro/caixas';

export const CaixaService = {
    getAll: async (page = 0, size = 20) => {
        const response = await api.get(`${BASE}?page=${page}&size=${size}`);
        return response.data;
    },

    getById: async (id) => {
        const response = await api.get(`${BASE}/${id}`);
        return response.data;
    },

    save: async (caixa) => {
        const response = await api.post(BASE, caixa);
        return response.data;
    },

    update: async (id, caixa) => {
        const response = await api.put(`${BASE}/${id}`, caixa);
        return response.data;
    },

    /**
     * Remove. O backend desativa em vez de apagar, para nao quebrar o
     * historico de um caixa que ja teve movimento.
     */
    delete: async (id) => {
        const response = await api.delete(`${BASE}/${id}`);
        return response.data;
    },

    search: async (term, page = 0, size = 20) => {
        const url = term
            ? `${BASE}?busca=${encodeURIComponent(term)}&page=${page}&size=${size}`
            : `${BASE}?page=${page}&size=${size}`;
        const response = await api.get(url);
        return response.data;
    }
};

export default CaixaService;
