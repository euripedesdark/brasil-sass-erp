import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = ApiConfig.API_BASE_URL;

export const SefazConsultaService = {
    async status(uf) {
        const response = await apiFetch(`${BASE_URL}/fiscal/sefaz/status?uf=${uf}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao consultar status SEFAZ');
        }
        const data = await response.json();
        return data;
    },

    async consultar(uf, chave) {
        const response = await apiFetch(`${BASE_URL}/fiscal/sefaz/consultar?uf=${uf}&chave=${chave}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao consultar NF-e');
        }
        const data = await response.json();
        return data;
    },

    async distribuicao(ufAutor, cnpj, ultNsu = '', chave = '') {
        let url = `${BASE_URL}/fiscal/sefaz/distribuicao?ufAutor=${ufAutor}&cnpj=${cnpj}`;
        if (ultNsu) url += `&ultNsu=${ultNsu}`;
        if (chave) url += `&chave=${chave}`;

        const response = await apiFetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao consultar distribuicao');
        }
        const data = await response.json();
        return data;
    },
};

export default SefazConsultaService;
