import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = ApiConfig.API_BASE_URL;

export const CfopService = {
    async listar(tipoOperacao = '') {
        let url = `${BASE_URL}/fiscal/cfop`;
        if (tipoOperacao) {
            url += `?tipoOperacao=${encodeURIComponent(tipoOperacao)}`;
        }

        const response = await apiFetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao listar CFOP');
        }
        const data = await response.json();
        return data;
    },

    async buscarPorCodigo(codigo) {
        const response = await apiFetch(`${BASE_URL}/fiscal/cfop/${codigo}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao buscar CFOP');
        }
        const data = await response.json();
        return data.data || data;
    },
};

export default CfopService;
