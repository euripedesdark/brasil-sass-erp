import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = ApiConfig.API_BASE_URL;

export const MarcaService = {
    async listar() {
        const response = await apiFetch(`${BASE_URL}/cadastro/marcas`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao listar marcas');
        }
        const data = await response.json();
        return data.data || data;
    },

    async buscarPorId(id) {
        const response = await apiFetch(`${BASE_URL}/cadastro/marcas/${id}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao buscar marca');
        }
        const data = await response.json();
        return data.data || data;
    },

    async criar(marca) {
        const response = await apiFetch(`${BASE_URL}/cadastro/marcas`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(marca),
        });
        if (!response.ok) {
            throw new Error('Erro ao criar marca');
        }
        const data = await response.json();
        return data.data || data;
    },

    async atualizar(id, marca) {
        const response = await apiFetch(`${BASE_URL}/cadastro/marcas/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(marca),
        });
        if (!response.ok) {
            throw new Error('Erro ao atualizar marca');
        }
        const data = await response.json();
        return data.data || data;
    },

    async excluir(id) {
        const response = await apiFetch(`${BASE_URL}/cadastro/marcas/${id}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao excluir marca');
        }
        return true;
    },
};

export default MarcaService;
