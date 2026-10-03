import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = ApiConfig.API_BASE_URL;

export const EntradaNotaService = {
    async listar(page = 0, size = 20) {
        const response = await apiFetch(`${BASE_URL}/fiscal/entradas?page=${page}&size=${size}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao listar entradas de notas');
        }
        const data = await response.json();
        return data;
    },

    /**
     * Le o XML e devolve o plano, sem gravar nada.
     *
     * O `Content-Type` NAO vai no cabecalho. O browser precisa montar o
     * `Content-Type: multipart/form-data; boundary=...` sozinho, porque e ele
     * que tem a fronteira. Mandar `application/json` aqui da o erro
     * "the request was rejected because its content type is not
     * multipart/form-data" — o 415 que aparece quando se envia arquivo do
     * jeito que se envia JSON.
     */
    async analisarImportacao(arquivo, pessoaId, criarProdutos) {
        const formData = new FormData();
        formData.append('arquivo', arquivo);
        if (pessoaId) formData.append('pessoaId', pessoaId);
        formData.append('criarProdutos', criarProdutos ? 'true' : 'false');

        const response = await apiFetch(`${BASE_URL}/fiscal/entradas/importar/analisar`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            // A mensagem do backend vem em `errors[0].message` e diz o que
            // esta errado de verdade ("e um PDF, e nao um XML"). Repassar o
            // JSON cru para o usuario ler nao serve para nada.
            const corpo = await response.json().catch(() => null);
            const mensagem = corpo?.errors?.[0]?.message
                || 'Nao foi possivel ler o arquivo enviado';
            throw new Error(mensagem);
        }
        return response.json();
    },

    /** Grava a nota, cria o que foi marcado e da entrada no estoque. */
    async confirmarImportacao(arquivo, pessoaId, criarProdutos) {
        const formData = new FormData();
        formData.append('arquivo', arquivo);
        if (pessoaId) formData.append('pessoaId', pessoaId);
        formData.append('criarProdutos', criarProdutos ? 'true' : 'false');

        const response = await apiFetch(`${BASE_URL}/fiscal/entradas/importar/confirmar`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const corpo = await response.json().catch(() => null);
            throw new Error(corpo?.errors?.[0]?.message
                || 'Nao foi possivel confirmar a entrada');
        }
        return response.json();
    },

    async buscarPorChave(chave) {
        const response = await apiFetch(`${BASE_URL}/fiscal/entradas/chave/${chave}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao buscar entrada por chave');
        }
        const data = await response.json();
        return data.data || data;
    },

    async registrar(entrada) {
        const response = await apiFetch(`${BASE_URL}/fiscal/entradas`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(entrada),
        });
        if (!response.ok) {
            throw new Error('Erro ao registrar entrada');
        }
        const data = await response.json();
        return data.data || data;
    },

    async manifestar(id, manifestacao) {
        const response = await apiFetch(`${BASE_URL}/fiscal/entradas/${id}/manifestacao`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(manifestacao),
        });
        if (!response.ok) {
            throw new Error('Erro ao manifestar entrada');
        }
        const data = await response.json();
        return data.data || data;
    },
};

export default EntradaNotaService;
