import api from './ApiConfig';

// Caminho real: CentroCustoController fica em /api/financeiro/centros-custo.
// Antes este service apontava para /api/centrocusto, que nao existe — as sete
// chamadas davam 404 e as telas CentroCusto.jsx e Transportadora.jsx, que
// dependem dele, nao carregavam nada.
//
// Tambem nao existem os sub-paths /paginado, /search nem GET /{id}: o
// controller expoe apenas GET, POST, PUT /{id} e DELETE /{id}. As telas
// tratam lista simples (CentroCusto.jsx faz `data.content || data`), entao a
// listagem segue sem paginacao de servidor.
const BASE = '/api/financeiro/centros-custo';

export const CentroCustoService = {
    getAll: async () => {
        const response = await api.get(BASE);
        return response.data;
    },

    getById: async (id) => {
        // Nao ha GET /{id} no controller; a lista traz todos os campos.
        const response = await api.get(BASE);
        const lista = response.data?.content || response.data || [];
        return Array.isArray(lista) ? lista.find(c => String(c.id) === String(id)) : null;
    },

    save: async (centroCusto) => {
        const response = await api.post(BASE, centroCusto);
        return response.data;
    },

    update: async (id, centroCusto) => {
        const response = await api.put(`${BASE}/${id}`, centroCusto);
        return response.data;
    },

    delete: async (id) => {
        const response = await api.delete(`${BASE}/${id}`);
        return response.data;
    },

    search: async (term) => {
        // Nao ha /search no controller; o filtro passa a ser do lado da tela.
        const response = await api.get(BASE);
        const lista = response.data?.content || response.data || [];
        if (!term) return lista;
        const alvo = String(term).toLowerCase();
        return Array.isArray(lista)
            ? lista.filter(c => [c.nome, c.descricao, c.codigo]
                  .filter(Boolean)
                  .some(v => String(v).toLowerCase().includes(alvo)))
            : lista;
    }
};

export default CentroCustoService;
