import axios from 'axios';
import ApiConfig from './ApiConfig';

const SALDO_URL = `${ApiConfig.BASE_URL || ''}/api/estoque/saldos`;
const MOV_URL = `${ApiConfig.BASE_URL || ''}/api/estoque/movimentacoes`;

class EstoqueService {
    /** Lista todos os saldos da empresa */
    listarSaldos(empresaId) {
        return axios.get(SALDO_URL, { params: { empresaId } });
    }

    /** Saldo de um produto (0 se nao existir) */
    buscarSaldo(empresaId, produtoId) {
        return axios.get(SALDO_URL, { params: { empresaId, produtoId } });
    }

    /** Movimentacoes da empresa ou de um produto */
    listarMovimentacoes(empresaId, produtoId) {
        const params = { empresaId };
        if (produtoId != null && produtoId !== '') params.produtoId = produtoId;
        return axios.get(MOV_URL, { params });
    }
}

export default new EstoqueService();
