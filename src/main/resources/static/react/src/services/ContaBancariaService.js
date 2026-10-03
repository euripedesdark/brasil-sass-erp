import axios from 'axios';
import ApiConfig from './ApiConfig';

const API_URL = `${ApiConfig.BASE_URL || ''}/api/financeiro/contas-bancarias`;

class ContaBancariaService {
    listar() {
        return axios.get(API_URL, { withCredentials: true });
    }

    criar(body) {
        return axios.post(API_URL, body, { withCredentials: true });
    }

    /** Soft-delete: marca conta como inativa */
    excluir(id) {
        return axios.delete(`${API_URL}/${id}`, { withCredentials: true });
    }
}

export default new ContaBancariaService();
