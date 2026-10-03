import ApiConfig, { apiFetch } from './ApiConfig';

// Service para /api/ia/prompt-templates (PromptTemplateController)
const PromptTemplateService = {
    listar: async (empresaId) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/ia/prompt-templates?empresaId=${empresaId}`);
        if (!response.ok) throw new Error('Erro ao listar templates de prompt');
        const data = await response.json();
        return data.data || data;
    },

    listarPorCategoria: async (empresaId, categoria) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/ia/prompt-templates/category/${encodeURIComponent(categoria)}?empresaId=${empresaId}`);
        if (!response.ok) throw new Error('Erro ao listar templates por categoria');
        const data = await response.json();
        return data.data || data;
    },

    buscarPorId: async (empresaId, id) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/ia/prompt-templates/${id}?empresaId=${empresaId}`);
        if (!response.ok) throw new Error('Erro ao buscar template de prompt');
        const data = await response.json();
        return data.data || data;
    },

    salvar: async (template) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/ia/prompt-templates`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(template)
        });
        if (!response.ok) throw new Error('Erro ao salvar template de prompt');
        const data = await response.json();
        return data.data || data;
    },

    excluir: async (empresaId, id) => {
        const response = await apiFetch(`${ApiConfig.BASE_URL}/api/ia/prompt-templates/${id}?empresaId=${empresaId}`, {
            method: 'DELETE'
        });
        if (!response.ok) throw new Error('Erro ao excluir template de prompt');
        return response.json();
    }
};

export default PromptTemplateService;
