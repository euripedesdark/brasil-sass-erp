import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = ApiConfig.API_BASE_URL;

export const TransportadoraService = {
    async listar(page = 0, size = 20) {
        const response = await apiFetch(`${BASE_URL}/cadastro/transportadoras?page=${page}&size=${size}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao listar transportadoras');
        }
        const data = await response.json();
        return data;
    },

    async buscarPorId(id) {
        const response = await apiFetch(`${BASE_URL}/cadastro/transportadoras/${id}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao buscar transportadora');
        }
        const data = await response.json();
        return data.data || data;
    },

    async criar(transportadora) {
        const response = await apiFetch(`${BASE_URL}/cadastro/transportadoras`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(transportadora),
        });
        if (!response.ok) {
            throw new Error('Erro ao criar transportadora');
        }
        const data = await response.json();
        return data.data || data;
    },

    async atualizar(id, transportadora) {
        const response = await apiFetch(`${BASE_URL}/cadastro/transportadoras/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(transportadora),
        });
        if (!response.ok) {
            throw new Error('Erro ao atualizar transportadora');
        }
        const data = await response.json();
        return data.data || data;
    },

    async excluir(id) {
        const response = await apiFetch(`${BASE_URL}/cadastro/transportadoras/${id}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao excluir transportadora');
        }
        return true;
    },
};

export default TransportadoraService;
