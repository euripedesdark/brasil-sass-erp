import axios from 'axios';

const API_URL = '/api/rh/funcionarios';

class FuncionarioService {
    // A empresa vem do token. Mandar na query nao só era redundante como
    // permitia pedir a lista de outra empresa.
    listar() {
        return axios.get(API_URL);
    }

    buscarPorId(id) {
        return axios.get(`${API_URL}/${id}`);
    }

    criar(funcionario) {
        return axios.post(API_URL, funcionario);
    }

    atualizar(id, funcionario) {
        return axios.put(`${API_URL}/${id}`, funcionario);
    }

    /** Desativa o colaborador (soft delete), preservando o histórico de apontamentos. */
    excluir(id) {
        return axios.delete(`${API_URL}/${id}`);
    }
}

export const FuncionarioService = new FuncionarioService();

export default FuncionarioService;
