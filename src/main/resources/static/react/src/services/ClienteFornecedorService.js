import { api } from './ApiConfig';

// Caminhos reais no backend: PessoaController fica em /api/cadastro/pessoas e
// FornecedorController em /api/cadastro/fornecedores. Antes este service
// apontava para /api/pessoa, que nao existe.
//
// Os dois controllers respondem no formato paginado do Spring
// ({content: [...], ...}), e nao ha /paginado nem /search: a listagem e sempre
// GET na raiz. Por isso a pagina e o tamanho viram apenas o que o backend
// aceita via query, e o filtro de busca e feito aqui.
//
// Tambem faltava listarFornecedores, que e o metodo que Transportadora.jsx
// chama para montar o seletor de pessoa. Sem ele a tela quebrava com
// "is not a function" — erro engolido pelo catch, que deixava o seletor
// silenciosamente vazio.
const PESSOAS = '/api/cadastro/pessoas';
const FORNECEDORES = '/api/cadastro/fornecedores';

const conteudo = (resposta) => resposta?.data?.content ?? resposta?.data ?? [];

const filtrar = (lista, termo) => {
    if (!termo) return lista;
    const alvo = String(termo).toLowerCase();
    return lista.filter(item => {
        const pessoa = item?.pessoa ?? item;
        return [pessoa?.nome, pessoa?.documento, pessoa?.email, item?.codigo]
            .filter(Boolean)
            .some(v => String(v).toLowerCase().includes(alvo));
    });
};

export const ClienteFornecedorService = {
    // --- pessoas (cadastro unico de clientes, fornecedores e colaboradores) ---
    getAll: async () => {
        const response = await api.get(PESSOAS);
        return response.data;
    },

    listarPessoas: async (termo) => {
        const response = await api.get(PESSOAS);
        return filtrar(conteudo(response), termo);
    },

    getById: async (id) => {
        const response = await api.get(`${PESSOAS}/${id}`);
        return response.data;
    },

    save: async (pessoa) => {
        const response = await api.post(PESSOAS, pessoa);
        return response.data;
    },

    update: async (id, pessoa) => {
        const response = await api.put(`${PESSOAS}/${id}`, pessoa);
        return response.data;
    },

    delete: async (id) => {
        const response = await api.delete(`${PESSOAS}/${id}`);
        return response.data;
    },

    search: async (termo) => {
        const response = await api.get(PESSOAS);
        return filtrar(conteudo(response), termo);
    },

    // --- fornecedores ---
    // assinatura mantida como (page, size) porque e assim que
    // Transportadora.jsx chama; os dois valores sao ignorados pelo backend.
    listarFornecedores: async () => {
        const response = await api.get(FORNECEDORES);
        return response.data;
    },

    getFornecedorById: async (id) => {
        const response = await api.get(`${FORNECEDORES}/${id}`);
        return response.data;
    },

    salvarFornecedor: async (fornecedor) => {
        const response = await api.post(FORNECEDORES, fornecedor);
        return response.data;
    },

    atualizarFornecedor: async (id, fornecedor) => {
        const response = await api.put(`${FORNECEDORES}/${id}`, fornecedor);
        return response.data;
    },

    excluirFornecedor: async (id) => {
        const response = await api.delete(`${FORNECEDORES}/${id}`);
        return response.data;
    }
};

export default ClienteFornecedorService;
