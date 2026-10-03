import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = ApiConfig.API_BASE_URL;

export const CestService = {
    // Paginado. Sao 1.043 CEST e o endpoint devolve 50 por omissao: buscar a
    // lista inteira trazia 50 linhas e a tela jurava que eram 50 — as outras
    // 993 ficavam inalcancaveis. O filtro vai no servidor pelo mesmo motivo.
    async listar(page = 0, rows = 50, busca = '') {
        const params = new URLSearchParams({ page, size: rows });
        if (busca && busca.trim()) params.append('busca', busca.trim());
        const response = await apiFetch(`${BASE_URL}/fiscal/cest?${params}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao listar CEST');
        }
        const data = await response.json();
        return data.data || data;
    },

    async buscarPorCodigo(codigo) {
        const response = await apiFetch(`${BASE_URL}/fiscal/cest/${codigo}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao buscar CEST');
        }
        const data = await response.json();
        return data.data || data;
    },
};

export default CestService;
