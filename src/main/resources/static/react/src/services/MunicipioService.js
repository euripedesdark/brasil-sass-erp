import axios from 'axios';

const API_BASE_URL = '/api/municipios';

// Configuração base do Axios para todas as requisições
const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json'
    }
});

export default api;

export const MunicipioService = {
    async buscar(codigo, nome, page = 0, size = 20) {
        const params = new URLSearchParams();
        params.append('page', page);
        params.append('size', size);
        
        if (codigo && codigo.trim()) {
            params.append('codigo', codigo.trim());
        }
        if (nome && nome.trim()) {
            params.append('nome', nome.trim());
        }
        
        const response = await axios.get(`${API_BASE_URL}/buscar?${params.toString()}`);
        return response.data;
    },

    async listar(page = 0, size = 20) {
        const response = await axios.get(`${API_BASE_URL}?page=${page}&size=${size}`);
        return response.data;
    },

    async buscarPorId(id) {
        const response = await axios.get(`${API_BASE_URL}/${id}`);
        return response.data;
    },

    async salvar(municipio) {
        const response = await axios.post(API_BASE_URL, municipio);
        return response.data;
    },

    async atualizar(id, municipio) {
        const response = await axios.put(`${API_BASE_URL}/${id}`, municipio);
        return response.data;
    },

    async excluir(id) {
        await axios.delete(`${API_BASE_URL}/${id}`);
    }
};
