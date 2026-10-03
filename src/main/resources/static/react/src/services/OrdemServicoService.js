import axios from 'axios';

const API_URL = '/api/servicos/os';

class OrdemServicoService {
    listarTodos(page = 0, size = 20, sort = 'dataAbertura') {
        return axios.get(`${API_URL}?page=${page}&size=${size}&sort=${sort}`);
    }

    listarPorStatus(status, page = 0, size = 20) {
        return axios.get(`${API_URL}/status/${status}?page=${page}&size=${size}`);
    }

    buscarPorId(id) {
        return axios.get(`${API_URL}/${id}`);
    }

    salvar(os) {
        if (os.id) {
            return axios.put(`${API_URL}/${os.id}`, os);
        }
        return axios.post(API_URL, os);
    }

    atualizarStatus(id, status) {
        return axios.put(`${API_URL}/${id}/status?status=${status}`);
    }

    deletar(id) {
        return axios.delete(`${API_URL}/${id}`);
    }

    getOSAtrasadas() {
        return axios.get(`${API_URL}/atrasadas`);
    }

    getTotalOSConcluidasPorPeriodo(inicio, fim) {
        return axios.get(`${API_URL}/total-concluidas?inicio=${inicio}&fim=${fim}`);
    }

    getTempoMedioExecucao() {
        return axios.get(`${API_URL}/tempo-medio`);
    }
}

export default new OrdemServicoService();
