import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = ApiConfig.API_BASE_URL;

export const FolhaPagamentoService = {
    async listar() {
        const response = await apiFetch(`${BASE_URL}/rh/folhas`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao listar folhas de pagamento');
        }
        const data = await response.json();
        return data.data || data;
    },

    async gerarDecimo(ano, parcela) {
        const response = await apiFetch(BASE_URL + '/rh/folhas/gerar-decimo?ano=' + ano + '&parcela=' + parcela, { method: 'POST' });
        if (!response.ok) throw new Error('Erro ao gerar 13o');
        const data = await response.json();
        return data.data || data;
    },

    async buscarPorId(id) {
        const response = await apiFetch(`${BASE_URL}/rh/folhas/${id}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao buscar folha de pagamento');
        }
        const data = await response.json();
        return data.data || data;
    },

    /**
     * Processar a folha gera um título a pagar no Financeiro, com vencimento
     * para dia 5. É o botão que fecha o ciclo do RH com o financeiro, então a
     * confirmação na tela avisa do que vai acontecer.
     */
    async processar(id) {
        const response = await apiFetch(`${BASE_URL}/rh/folhas/${id}/processar`, { method: 'POST' });
        if (!response.ok) {
            const j = await response.json().catch(() => null);
            throw new Error(j?.errors?.[0]?.message || 'Não foi possível processar a folha');
        }
        return true;
    },

    async encargos(id, a) {
        let url = BASE_URL + '/rh/encargos/folha/' + id;
        if (a) url += '?aliqInss=' + (a.inss ?? 20) + '&aliqFgts=' + (a.fgts ?? 8) + '&aliqRat=' + (a.rat ?? 2);
        const response = await apiFetch(url);
        if (!response.ok) throw new Error('Falha nos encargos');
        return response.json();
    },

    async cancelar(id) {
        const response = await apiFetch(`${BASE_URL}/rh/folhas/${id}/cancelar`, { method: 'POST' });
        if (!response.ok) {
            const j = await response.json().catch(() => null);
            throw new Error(j?.errors?.[0]?.message || 'Não foi possível cancelar a folha');
        }
        return true;
    },

    async criar(folha) {
        const response = await apiFetch(`${BASE_URL}/rh/folhas`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(({ empresaId, ...resto }) => resto)(folha),
        });
        if (!response.ok) {
            throw new Error('Erro ao criar folha de pagamento');
        }
        const data = await response.json();
        return data.data || data;
    },

    async atualizar(id, folha) {
        const response = await apiFetch(`${BASE_URL}/rh/folhas/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(({ empresaId, ...resto }) => resto)(folha),
        });
        if (!response.ok) {
            throw new Error('Erro ao atualizar folha de pagamento');
        }
        const data = await response.json();
        return data.data || data;
    },

    async excluir(id) {
        const response = await apiFetch(`${BASE_URL}/rh/folhas/${id}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao excluir folha de pagamento');
        }
        return true;
    },
};

export default FolhaPagamentoService;
