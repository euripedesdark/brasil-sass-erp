import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = ApiConfig.API_BASE_URL;

export const ImpostoService = {
    async listar(tipo = '') {
        let url = `${BASE_URL}/fiscal/impostos`;
        if (tipo) {
            url += `?tipo=${encodeURIComponent(tipo)}`;
        }

        const response = await apiFetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao listar impostos');
        }
        const data = await response.json();
        return data;
    },

    async buscarPorCodigo(codigo) {
        const response = await apiFetch(`${BASE_URL}/fiscal/impostos/${codigo}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao buscar imposto');
        }
        const data = await response.json();
        return data.data || data;
    },
};

export default ImpostoService;
