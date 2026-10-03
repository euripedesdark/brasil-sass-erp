import axios from 'axios';

const BASE = '/api/producao/estruturas';

export default {
    listar: (produtoPaiId) => axios.get(`${BASE}/${produtoPaiId}`),
    criar: (estrutura) => axios.post(BASE, estrutura),
    excluir: (id) => axios.delete(`${BASE}/${id}`)
};
