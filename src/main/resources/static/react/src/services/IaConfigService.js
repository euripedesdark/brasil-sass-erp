import ApiConfig, { apiFetch } from './ApiConfig';

// Service para /api/ia/config (AIConfigController)
const IaConfigService = {
    obter: async (empresaId) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/ia/config?empresaId=${empresaId}`);
        if (!response.ok) throw new Error('Erro ao obter configuração de IA');
        const data = await response.json();
        return data.data || data;
    },

    salvar: async (empresaId, config) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/ia/config?empresaId=${empresaId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(config)
        });
        if (!response.ok) throw new Error('Erro ao salvar configuração de IA');
        const data = await response.json();
        return data.data || data;
    },

    testarConexao: async (empresaId) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/ia/config/test-connection?empresaId=${empresaId}`, {
            method: 'POST'
        });
        if (!response.ok) throw new Error('Erro ao testar conexão com a API de IA');
        const data = await response.json();
        return data.data || data;
    },

    resetarUso: async (empresaId) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/ia/config/reset-usage?empresaId=${empresaId}`, {
            method: 'POST'
        });
        if (!response.ok) throw new Error('Erro ao resetar uso de tokens');
        return response.json();
    },

    tokensRestantes: async (empresaId) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/ia/config/remaining-tokens?empresaId=${empresaId}`);
        if (!response.ok) throw new Error('Erro ao consultar tokens restantes');
        const data = await response.json();
        return data.data ?? data;
    }
};

export default IaConfigService;
