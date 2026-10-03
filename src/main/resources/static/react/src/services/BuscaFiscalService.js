import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = `${ApiConfig.API_BASE_URL}/fiscal/busca`;

export const BuscaFiscalService = {
    async buscar(termo, tabela = '') {
        const params = new URLSearchParams({ q: termo, limite: '20' });
        if (tabela) params.set('tabela', tabela);
        const response = await apiFetch(`${BASE_URL}?${params.toString()}`);
        if (!response.ok) {
            throw new Error('Nao foi possivel consultar o catalogo fiscal.');
        }
        return response.json();
    }
};

export default BuscaFiscalService;
