import axios from 'axios';

const BASE = '/api/compras';

export default {
    recebimentos: (pedidoId) => axios.get(`${BASE}/recebimentos`, { params: pedidoId ? { pedidoId } : {} }),
    itensRecebimento: (id) => axios.get(`${BASE}/recebimentos/${id}/itens`),
    conferencias: () => axios.get(`${BASE}/conferencia-faturas`),
    itensConferencia: (id) => axios.get(`${BASE}/conferencia-faturas/${id}/itens`),
    conferir: (payload) => axios.post(`${BASE}/conferencia-faturas`, payload),
    aprovarExcepcional: (id, motivo) => axios.post(`${BASE}/conferencia-faturas/` + id + `/aprovacao-excepcional`, { motivo })
};
