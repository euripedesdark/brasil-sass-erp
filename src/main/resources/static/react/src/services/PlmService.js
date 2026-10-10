import axios from 'axios';

const BASE = '/api/plm';

export default {
    mudancas: () => axios.get(BASE + '/mudancas'),
    criarMudanca: (payload) => axios.post(BASE + '/mudancas', payload),
    enviarAprovacao: (id, aprovadorId) => axios.post(BASE + '/mudancas/' + id + '/enviar-aprovacao', { aprovadorId }),
    decidir: (id, aprovar, observacao) => axios.post(BASE + '/mudancas/' + id + '/decidir', { aprovar, observacao }),
    implementar: (id) => axios.post(BASE + '/mudancas/' + id + '/implementar', {}),
    efeitos: (id) => axios.get(BASE + '/mudancas/' + id + '/efeitos'),
    adicionarEfeito: (payload) => axios.post(BASE + '/efeitos', payload),
    revisoes: (produtoId) => axios.get(BASE + '/revisoes', { params: { produtoId } }),
    criarRevisao: (payload) => axios.post(BASE + '/revisoes', payload),
    vigorarRevisao: (id) => axios.post(BASE + '/revisoes/' + id + '/vigorar', {}),
};
