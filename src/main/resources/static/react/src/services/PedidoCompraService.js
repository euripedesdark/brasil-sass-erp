import axios from 'axios';
import ApiConfig from './ApiConfig';

const API_URL = `${ApiConfig.BASE_URL || ''}/api/compras/pedidos`;
const RECEBIMENTOS_URL = `${ApiConfig.BASE_URL || ''}/api/compras/recebimentos`;

class PedidoCompraService {
    listarPorEmpresa(empresaId) { return axios.get(API_URL, { params: { empresaId } }); }
    buscarPorId(id) { return axios.get(`${API_URL}/${id}`); }
    criar(pedido) { return axios.post(API_URL, pedido); }
    receber(id) { return axios.post(`${API_URL}/${id}/receber`); }
    receberParcial(id, quantidades) { return axios.post(`${API_URL}/${id}/receber-parcial`, quantidades); }
    cancelar(id) { return axios.post(`${API_URL}/${id}/cancelar`); }
    listarRecebimentos(pedidoId) { return axios.get(RECEBIMENTOS_URL, { params: pedidoId ? { pedidoId } : {} }); }
    listarItensRecebimento(recebimentoId) { return axios.get(`${RECEBIMENTOS_URL}/${recebimentoId}/itens`); }
}
export default new PedidoCompraService();
