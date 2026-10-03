import axios from 'axios';

const API_URL = '/api/cadastro/fornecedores';

const FornecedorService = {
    listar(page = 0, size = 20) {
        return axios.get(API_URL, { params: { page, size } }).then((r) => r.data);
    },
    buscarPorId(id) {
        return axios.get(`${API_URL}/${id}`);
    },
    criar(fornecedor) {
        return axios.post(API_URL, fornecedor);
    },
    atualizar(id, fornecedor) {
        return axios.put(`${API_URL}/${id}`, fornecedor);
    },
    excluir(id) {
        return axios.delete(`${API_URL}/${id}`);
    }
};

export { FornecedorService };
export default FornecedorService;
