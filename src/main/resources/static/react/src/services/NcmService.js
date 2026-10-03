import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = ApiConfig.API_BASE_URL;

export const NcmService = {
    async listar(page = 0, size = 50, descricao = '') {
        let url = `${BASE_URL}/fiscal/ncm?page=${page}&size=${size}`;
        if (descricao) {
            url += `&descricao=${encodeURIComponent(descricao)}`;
        }

        const response = await apiFetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao listar NCM');
        }
        const data = await response.json();
        return data;
    },

    async buscarPorCodigo(codigo) {
        const response = await apiFetch(`${BASE_URL}/fiscal/ncm/${codigo}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao buscar NCM');
        }
        const data = await response.json();
        return data.data || data;
    },
};

export default NcmService;
