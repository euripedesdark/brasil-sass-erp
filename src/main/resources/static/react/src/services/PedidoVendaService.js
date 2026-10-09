import axios from 'axios';
import ApiConfig from './ApiConfig';

const API_URL = `${ApiConfig.BASE_URL || ''}/api/vendas/pedidos`;

class PedidoVendaService {
    listarPorEmpresa(empresaId) {
        return axios.get(API_URL, { params: { empresaId } });
    }

    buscarPorId(id) {
        return axios.get(`${API_URL}/${id}`);
    }

    criar(pedido) {
        return axios.post(API_URL, pedido);
    }

    confirmar(id) {
        return axios.post(`${API_URL}/${id}/confirmar`);
    }

    atp(id) {
        return axios.get(`${API_URL}/${id}/atp`);
    }

    credito(clienteId) {
        return axios.get(API_URL + '/clientes/' + clienteId + '/credito');
    }

    faturar(id, forcar) {
        return axios.post(`${API_URL}/${id}/faturar` + (forcar ? '?forcar=true' : ''));
    }

    preverTributacao(body, { ufDestino, consumidorFinal = true, contribuinte = false } = {}) {
        const qs = new URLSearchParams();
        if (ufDestino) qs.set('ufDestino', ufDestino);
        qs.set('consumidorFinal', String(!!consumidorFinal));
        qs.set('contribuinte', String(!!contribuinte));
        return axios.post(`${API_URL}/tributacao/prever?${qs.toString()}`, body);
    }

    posvenda(id, body) {
        return axios.post(`${API_URL}/${id}/posvenda`, body);
    }
    cancelar(id) {
        return axios.post(`${API_URL}/${id}/cancelar`);
    }
}

export default new PedidoVendaService();
