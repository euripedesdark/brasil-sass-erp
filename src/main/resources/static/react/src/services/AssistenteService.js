import ApiConfig, { apiFetch } from './ApiConfig';

const empresaHeaders = (empresaId, json = false) => ({
    ...(json ? { 'Content-Type': 'application/json' } : {}),
    'X-Empresa-Id': String(empresaId)
});

/**
 * Serviço do Assistente ERP.
 *
 * Transport only. A confiança, o motivo e a origem chegam calculados do backend:
 * a tela não computa nada, e também não extrai código de dentro de um texto.
 */
export const AssistenteService = {
    perguntar: async (empresaId, pergunta) => {
        const resposta = await apiFetch(`${ApiConfig.BASE_URL}/api/ia/assistente`, {
            method: 'POST',
            headers: empresaHeaders(empresaId, true),
            body: JSON.stringify({ pergunta })
        });
        if (!resposta.ok) {
            throw new Error('Não foi possível consultar o assistente');
        }
        return resposta.json();
    },

    auditoria: async (empresaId) => {
        const resposta = await apiFetch(`${ApiConfig.BASE_URL}/api/ia/assistente/auditoria`, {
            headers: empresaHeaders(empresaId)
        });
        return resposta.ok ? resposta.json() : [];
    }
};
