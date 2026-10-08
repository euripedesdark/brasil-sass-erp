import { api } from './ApiConfig';

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
    },

    movimentar: async (id, tipo, valor, observacao) => {
        const response = await api.post(`${BASE}/${id}/movimentos`, { tipo, valor, observacao });
        return response.data;
    },

    listarMovimentos: async (id) => {
        const response = await api.get(`${BASE}/${id}/movimentos`);
        return response.data;
    }
};

export default CaixaService;
