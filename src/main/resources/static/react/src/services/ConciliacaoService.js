import axios from 'axios';
import ApiConfig from './ApiConfig';

const API_URL = `${ApiConfig.BASE_URL || ''}/api/financeiro/conciliacoes`;

class ConciliacaoService {
    listar() { return axios.get(API_URL, { withCredentials: true }); }
    criar(body) { return axios.post(API_URL, body, { withCredentials: true }); }
    itens(id) { return axios.get(`${API_URL}/${id}/itens`, { withCredentials: true }); }
    pendentes(id) { return axios.get(`${API_URL}/${id}/pendentes`, { withCredentials: true }); }
    baixas(id) { return axios.get(`${API_URL}/${id}/baixas`, { withCredentials: true }); }
    vincular(id, body) { return axios.post(`${API_URL}/${id}/vincular`, body, { withCredentials: true }); }
    conciliarAutomatico(id) { return axios.post(API_URL + '/' + id + '/conciliar-automatico', {}, { withCredentials: true }); }
        fechar(id) { return axios.post(`${API_URL}/${id}/fechar`, {}, { withCredentials: true }); }
}

export default new ConciliacaoService();
