import axios from 'axios';
import ApiConfig from './ApiConfig';

const API_URL = '/api/estoque/movimentacoes';
const SALDO_URL = '/api/estoque/saldos';
const PRODUTOS_URL = '/api/estoque/produtos';

class MovimentacaoEstoqueService {
    listarMovimentacoes(empresaId, produtoId, page = 0, size = 20) {
        return axios.get(`${API_URL}?empresaId=${empresaId}&produtoId=${produtoId}&page=${page}&size=${size}`);
    }

    listarPorEmpresa(empresaId, page = 0, size = 20) {
        return axios.get(`${API_URL}/empresa/${empresaId}?page=${page}&size=${size}`);
    }

    buscarSaldo(empresaId, produtoId) {
        return axios.get(`${SALDO_URL}?empresaId=${empresaId}&produtoId=${produtoId}`);
    }

    listarProdutos(empresaId, page = 0, size = 20) {
        return axios.get(`${PRODUTOS_URL}?empresaId=${empresaId}&page=${page}&size=${size}`);
    }

    salvarMovimentacao(movimentacao) {
        if (movimentacao.id) {
            return axios.put(`${API_URL}/${movimentacao.id}`, movimentacao);
        }
        return axios.post(API_URL, movimentacao);
    }

    registrarEntrada(movimentacao) {
        return axios.post(`${API_URL}/entrada`, movimentacao);
    }

    registrarSaida(movimentacao) {
        return axios.post(`${API_URL}/saida`, movimentacao);
    }

    registrarTransferencia(movimentacao) {
        return axios.post(`${API_URL}/transferencia`, movimentacao);
    }

    ajustarEstoque(ajuste) {
        return axios.post(`${API_URL}/ajuste`, ajuste);
    }

    getHistoricoProduto(empresaId, produtoId, page = 0, size = 20) {
        return axios.get(`${API_URL}/historico?empresaId=${empresaId}&produtoId=${produtoId}&page=${page}&size=${size}`);
    }

    getEstoqueBaixo(empresaId, limiteMinimo) {
        return axios.get(`${PRODUTOS_URL}/estoque-baixo?empresaId=${empresaId}&limiteMinimo=${limiteMinimo}`);
    }
}

export default new MovimentacaoEstoqueService();
