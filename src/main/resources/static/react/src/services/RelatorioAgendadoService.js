import ApiConfig, { apiFetch } from './ApiConfig';

// Service para /api/bi/relatorios-agendados (RelatorioAgendadoController)
const RelatorioAgendadoService = {
    listar: async (empresaId) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/relatorios-agendados?empresaId=${empresaId}`);
        if (!response.ok) throw new Error('Erro ao listar relatórios agendados');
        const data = await response.json();
        return data.data || data;
    },

    listarPendentes: async (empresaId) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/relatorios-agendados/pendentes?empresaId=${empresaId}`);
        if (!response.ok) throw new Error('Erro ao listar relatórios pendentes');
        const data = await response.json();
        return data.data || data;
    },

    criar: async (empresaId, agendamento) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/relatorios-agendados?empresaId=${empresaId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(agendamento)
        });
        if (!response.ok) throw new Error('Erro ao criar agendamento');
        const data = await response.json();
        return data.data || data;
    },

    atualizar: async (empresaId, id, agendamento) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/relatorios-agendados/${id}?empresaId=${empresaId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(agendamento)
        });
        if (!response.ok) throw new Error('Erro ao atualizar agendamento');
        const data = await response.json();
        return data.data || data;
    },

    excluir: async (empresaId, id) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/relatorios-agendados/${id}?empresaId=${empresaId}`, {
            method: 'DELETE'
        });
        if (!response.ok) throw new Error('Erro ao excluir agendamento');
        return response.json();
    },

    executar: async (empresaId, id) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/relatorios-agendados/${id}/executar?empresaId=${empresaId}`, {
            method: 'POST'
        });
        if (!response.ok) throw new Error('Erro ao executar relatório agendado');
        return response.json();
    },

    agendar: async (empresaId, id) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/relatorios-agendados/${id}/agendar?empresaId=${empresaId}`, {
            method: 'POST'
        });
        if (!response.ok) throw new Error('Erro ao ativar agendamento');
        return response.json();
    },

    executarTodos: async (empresaId) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/bi/relatorios-agendados/executar-todos?empresaId=${empresaId}`, {
            method: 'POST'
        });
        if (!response.ok) throw new Error('Erro ao executar todos os relatórios pendentes');
        return response.json();
    }
};

export default RelatorioAgendadoService;
