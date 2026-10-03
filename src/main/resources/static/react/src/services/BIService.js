import ApiConfig, { apiFetch } from './ApiConfig';

const BIService = {
    // Dashboard endpoints
    listarDashboards: async (empresaId) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/dashboards?empresaId=${empresaId}`);
        return response;
    },

    criarDashboard: async (dashboard) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/dashboards?empresaId=${dashboard.empresaId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dashboard)
        });
        return response;
    },

    atualizarDashboard: async (id, dashboard) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/dashboards/${id}?empresaId=${dashboard.empresaId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dashboard)
        });
        return response;
    },

    excluirDashboard: async (id, empresaId) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/dashboards/${id}?empresaId=${empresaId}`, {
            method: 'DELETE'
        });
        return response;
    },

    // Indicadores endpoints
    listarIndicadores: async (empresaId, dashboardId = null) => {
        const url = dashboardId 
            ? `${ApiConfig.BASE_URL}/api/bi/indicadores?empresaId=${empresaId}&dashboardId=${dashboardId}`
            : `${ApiConfig.BASE_URL}/api/bi/indicadores?empresaId=${empresaId}`;
        const response = await apiFetch(url);
        return response;
    },

    // Relatórios endpoints
    listarRelatoriosAgendados: async (empresaId) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/relatorios-agendados?empresaId=${empresaId}`);
        return response;
    },

    // Dashboard dados
    getDashboardDados: async (dashboardId, empresaId) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/dashboards/${dashboardId}/dados?empresaId=${empresaId}`);
        return response;
    }
};

export default BIService;
