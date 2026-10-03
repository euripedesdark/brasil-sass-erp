import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = ApiConfig.API_BASE_URL;

export const IssqnService = {
    async listarPorUf(uf) {
        const response = await apiFetch(`${BASE_URL}/fiscal/issqn/uf/${uf}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao listar ISSQN por UF');
        }
        const data = await response.json();
        return data;
    },

    async buscarPorMunicipio(codigoIbge) {
        const response = await apiFetch(`${BASE_URL}/fiscal/issqn/municipio/${codigoIbge}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao buscar ISSQN por municipio');
        }
        const data = await response.json();
        return data.data || data;
    },
};

export default IssqnService;
