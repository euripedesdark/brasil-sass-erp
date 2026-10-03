import axios from 'axios';

const API_URL = '/api/cadastro/clientes';

class ClienteService {
    listar(page = 0, size = 20, filtros = {}) {
        return axios.get(API_URL, { params: { page, size, ...filtros } }).then((r) => r.data);
    }

    buscarPorId(id) {
        return axios.get(`${API_URL}/${id}`);
    }

    criar(cliente) {
        return axios.post(API_URL, cliente);
    }

    atualizar(id, cliente) {
        return axios.put(`${API_URL}/${id}`, cliente);
    }

    excluir(id) {
        return axios.delete(`${API_URL}/${id}`);
    }

    getLimiteCredito(id) {
        return axios.get(`${API_URL}/${id}/limite-credito`);
    }
}

// Nomeado: Cliente.jsx e ClienteLogo.jsx importam por nome.
const clienteService = new ClienteService();

export { clienteService as ClienteService };
export default clienteService;
