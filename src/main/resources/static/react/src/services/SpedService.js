import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = `${ApiConfig.API_BASE_URL}/fiscal/sped`;

const jsonRequest = async (url, options = {}) => {
    const response = await apiFetch(url, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        }
    });

    const body = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(body.message || body.erro || 'Nao foi possivel gerar o arquivo SPED.');
    }
    return body;
};

export const SpedService = {
    gerar(pedido) {
        return jsonRequest(`${BASE_URL}/efd/gerar`, {
            method: 'POST',
            body: JSON.stringify(pedido)
        });
    },

    gerarPeriodo(pedido) {
        return jsonRequest(`${BASE_URL}/efd/gerar-periodo`, {
            method: 'POST',
            body: JSON.stringify(pedido)
        });
    },

    gerarContribPeriodo(pedido) {
        return jsonRequest(`${BASE_URL}/efd-contribuicoes/gerar-periodo`, {
            method: 'POST',
            body: JSON.stringify(pedido)
        });
    },

    historico() {
        return jsonRequest(BASE_URL + '/historico');
    },

    exemplo() {
        return jsonRequest(`${BASE_URL}/efd/exemplo`);
    }
};

export default SpedService;
