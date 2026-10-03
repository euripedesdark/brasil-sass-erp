import axios from 'axios';

const API_URL = '/api/cadastro/produtos';

class ProdutoService {
    listarTodos(page = 0, size = 20, sort = 'nome') {
        return axios.get(`${API_URL}?page=${page}&size=${size}&sort=${sort}`);
    }

    buscarPorNome(nome, page = 0, size = 20) {
        return axios.get(`${API_URL}?nome=${encodeURIComponent(nome)}&page=${page}&size=${size}`);
    }

    buscarPorId(id) {
        return axios.get(`${API_URL}/${id}`);
    }

    salvar(produto) {
        if (produto.id) {
            return axios.put(`${API_URL}/${produto.id}`, produto);
        }
        return axios.post(API_URL, produto);
    }

    deletar(id) {
        return axios.delete(`${API_URL}/${id}`);
    }

    getProdutosEstoqueBaixo() {
        return axios.get(`${API_URL}/estoque-baixo`);
    }
}

export default new ProdutoService();
