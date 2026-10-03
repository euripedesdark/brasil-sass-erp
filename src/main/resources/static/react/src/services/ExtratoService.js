import axios from 'axios';
import ApiConfig from './ApiConfig';

const API_URL = `${ApiConfig.BASE_URL || ''}/api/financeiro/extratos`;

class ExtratoService {
    porConta(contaId) {
        return axios.get(`${API_URL}/conta/${contaId}`, { withCredentials: true });
    }

    pendentesConciliacao() {
        return axios.get(`${API_URL}/pendentes-conciliacao`, { withCredentials: true });
    }

    /** ExtratoRequest: contaBancariaId, dataMovimento, descricao, valor, tipo */
    criar(body) {
        return axios.post(API_URL, body, { withCredentials: true });
    }
}

export default new ExtratoService();
