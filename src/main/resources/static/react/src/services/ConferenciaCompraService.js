import axios from 'axios';

const BASE = '/api/compras';

export default {
    recebimentos: (pedidoId) => axios.get(`${BASE}/recebimentos`, { params: pedidoId ? { pedidoId } : {} }),
    itensRecebimento: (id) => axios.get(`${BASE}/recebimentos/${id}/itens`),
    conferencias: () => axios.get(`${BASE}/conferencia-faturas`),
    conferir: (payload) => axios.post(`${BASE}/conferencia-faturas`, payload)
};
