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
            const body = await response.json().catch(() => null);
            throw new Error(body?.mensagem || body?.erro || 'Erro ao consultar status SEFAZ');
        }
        const data = await response.json();
        return data;
    },

    async consultar(uf, chave) {
        const chaveNormalizada = String(chave || '').replace(/\D/g, '');
        if (chaveNormalizada.length !== 44) {
            throw new Error('A chave de acesso da NF-e deve possuir 44 dígitos.');
        }
        const response = await apiFetch(`${BASE_URL}/fiscal/sefaz/consultar?uf=${uf}&chave=${chaveNormalizada}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            const body = await response.json().catch(() => null);
            throw new Error(body?.mensagem || body?.erro || 'Erro ao consultar NF-e');
        }
        const data = await response.json();
        return data;
    },

    async distribuicao(ufAutor, cnpj, ultNsu = '', chave = '') {
        const cnpjNormalizado = String(cnpj || '').replace(/\D/g, '');
        if (cnpjNormalizado.length !== 14) {
            throw new Error('O CNPJ do destinatário deve possuir 14 dígitos.');
        }
        let url = `${BASE_URL}/fiscal/sefaz/distribuicao?ufAutor=${ufAutor}&cnpj=${cnpjNormalizado}`;
        if (ultNsu) url += `&ultNsu=${ultNsu}`;
        if (chave) url += `&chave=${chave}`;

        const response = await apiFetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            const body = await response.json().catch(() => null);
            throw new Error(body?.mensagem || body?.erro || 'Erro ao consultar distribuição');
        }
        const data = await response.json();
        return data;
    },
};

export default SefazConsultaService;
