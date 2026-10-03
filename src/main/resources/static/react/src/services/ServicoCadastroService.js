import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = ApiConfig.API_BASE_URL;

export const ServicoCadastroService = {
    async listar(page = 0, size = 20, nome = '', codigo = '', ativo = null) {
        let url = `${BASE_URL}/cadastro/servicos?page=${page}&size=${size}`;
        if (nome) url += `&nome=${encodeURIComponent(nome)}`;
        if (codigo) url += `&codigo=${encodeURIComponent(codigo)}`;
        if (ativo !== null) url += `&ativo=${ativo}`;

        const response = await apiFetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao listar servicos');
        }
        const data = await response.json();
        return data.data || data;
    },

    async buscarPorId(id) {
        const response = await apiFetch(`${BASE_URL}/cadastro/servicos/${id}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao buscar servico');
        }
        const data = await response.json();
        return data.data || data;
    },

    async criar(servico) {
        const response = await apiFetch(`${BASE_URL}/cadastro/servicos`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(servico),
        });
        if (!response.ok) {
            throw new Error('Erro ao criar servico');
        }
        const data = await response.json();
        return data.data || data;
    },

    async atualizar(id, servico) {
        const response = await apiFetch(`${BASE_URL}/cadastro/servicos/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(servico),
        });
        if (!response.ok) {
            throw new Error('Erro ao atualizar servico');
        }
        const data = await response.json();
        return data.data || data;
    },

    async excluir(id) {
        const response = await apiFetch(`${BASE_URL}/cadastro/servicos/${id}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao excluir servico');
        }
        return true;
    },
};

export default ServicoCadastroService;
