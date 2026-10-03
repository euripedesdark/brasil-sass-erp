import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = ApiConfig.API_BASE_URL;

export const CargoService = {
    async listar() {
        const response = await apiFetch(`${BASE_URL}/rh/cargos`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao listar cargos');
        }
        const data = await response.json();
        return data.data || data;
    },

    async buscarPorId(id) {
        const response = await apiFetch(`${BASE_URL}/rh/cargos/${id}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao buscar cargo');
        }
        const data = await response.json();
        return data.data || data;
    },

    async criar(cargo) {
        const response = await apiFetch(`${BASE_URL}/rh/cargos`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(cargo),
        });
        if (!response.ok) {
            throw new Error('Erro ao criar cargo');
        }
        const data = await response.json();
        return data.data || data;
    },

    async atualizar(id, cargo) {
        const response = await apiFetch(`${BASE_URL}/rh/cargos/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(cargo),
        });
        if (!response.ok) {
            throw new Error('Erro ao atualizar cargo');
        }
        const data = await response.json();
        return data.data || data;
    },

    async excluir(id) {
        const response = await apiFetch(`${BASE_URL}/rh/cargos/${id}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao excluir cargo');
        }
        return true;
    },
};

export default CargoService;
