import ApiConfig, { apiFetch } from './ApiConfig';

// Service para /api/bi/kpis (KpiController)
const KpiService = {
    listar: async (empresaId) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/kpis?empresaId=${empresaId}`);
        if (!response.ok) throw new Error('Erro ao listar KPIs');
        const data = await response.json();
        return data.data || data;
    },

    listarPorTipo: async (empresaId, tipo) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/kpis/type/${encodeURIComponent(tipo)}?empresaId=${empresaId}`);
        if (!response.ok) throw new Error('Erro ao listar KPIs por tipo');
        const data = await response.json();
        return data.data || data;
    },

    buscarPorId: async (empresaId, id) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/kpis/${id}?empresaId=${empresaId}`);
        if (!response.ok) throw new Error('Erro ao buscar KPI');
        const data = await response.json();
        return data.data || data;
    },

    criar: async (empresaId, kpi) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/kpis?empresaId=${empresaId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(kpi)
        });
        if (!response.ok) throw new Error('Erro ao criar KPI');
        const data = await response.json();
        return data.data || data;
    },

    atualizar: async (empresaId, id, kpi) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/kpis/${id}?empresaId=${empresaId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(kpi)
        });
        if (!response.ok) throw new Error('Erro ao atualizar KPI');
        const data = await response.json();
        return data.data || data;
    },

    excluir: async (empresaId, id) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/kpis/${id}?empresaId=${empresaId}`, {
            method: 'DELETE'
        });
        if (!response.ok) throw new Error('Erro ao excluir KPI');
        return response.json();
    },

    refresh: async (empresaId) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/kpis/refresh?empresaId=${empresaId}`, {
            method: 'POST'
        });
        if (!response.ok) throw new Error('Erro ao atualizar valores dos KPIs');
        return response.json();
    },

    calcular: async (empresaId, id) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/kpis/${id}/calculate?empresaId=${empresaId}`);
        if (!response.ok) throw new Error('Erro ao calcular KPI');
        const data = await response.json();
        return data.data || data;
    }
};

export default KpiService;
