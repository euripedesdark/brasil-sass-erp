import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = ApiConfig.API_BASE_URL;

export const UnidadeMedidaService = {
    async listar() {
        const response = await apiFetch(`${BASE_URL}/cadastro/unidades-medida`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao listar unidades de medida');
        }
        const data = await response.json();
        return data.data || data;
    },

    async buscarPorId(id) {
        const response = await apiFetch(`${BASE_URL}/cadastro/unidades-medida/${id}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao buscar unidade de medida');
        }
        const data = await response.json();
        return data.data || data;
    },

    async criar(unidadeMedida) {
        const response = await apiFetch(`${BASE_URL}/cadastro/unidades-medida`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(unidadeMedida),
        });
        if (!response.ok) {
            throw new Error('Erro ao criar unidade de medida');
        }
        const data = await response.json();
        return data.data || data;
    },

    async atualizar(id, unidadeMedida) {
        const response = await apiFetch(`${BASE_URL}/cadastro/unidades-medida/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(unidadeMedida),
        });
        if (!response.ok) {
            throw new Error('Erro ao atualizar unidade de medida');
        }
        const data = await response.json();
        return data.data || data;
    },

    async excluir(id) {
        const response = await apiFetch(`${BASE_URL}/cadastro/unidades-medida/${id}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao excluir unidade de medida');
        }
        return true;
    },
};

export default UnidadeMedidaService;
