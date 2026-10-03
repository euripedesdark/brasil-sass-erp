import axios from 'axios';
import ApiConfig from './ApiConfig';

const API_URL = '/api/cadastro/servicos';

class ServicoService {
    listarTodos(page = 0, size = 20, sort = 'nome') {
        return axios.get(`${API_URL}?page=${page}&size=${size}&sort=${sort}`);
    }

    listarPorFiltros(nome, codigo, ativo, page = 0, size = 20) {
        const params = new URLSearchParams();
        if (nome) params.append('nome', nome);
        if (codigo) params.append('codigo', codigo);
        if (ativo !== undefined) params.append('ativo', ativo);
        params.append('page', page);
        params.append('size', size);
        return axios.get(`${API_URL}?${params.toString()}`);
    }

    buscarPorId(id) {
        return axios.get(`${API_URL}/${id}`);
    }

    salvar(servico) {
        if (servico.id) {
            return axios.put(`${API_URL}/${servico.id}`, servico);
        }
        return axios.post(API_URL, servico);
    }

    excluir(id) {
        return axios.delete(`${API_URL}/${id}`);
    }

    getServicosAtivos(empresaId) {
        return axios.get(`${API_URL}/ativos?empresaId=${empresaId}`);
    }

    getServicosPorCategoria(categoria, empresaId) {
        return axios.get(`${API_URL}/categoria/${categoria}?empresaId=${empresaId}`);
    }

    getServicosPorValorMinimo(valorMinimo) {
        return axios.get(`${API_URL}/valor-minimo?valor=${valorMinimo}`);
    }
}

export default new ServicoService();
