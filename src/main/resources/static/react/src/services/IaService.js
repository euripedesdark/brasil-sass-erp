import ApiConfig, { apiFetch } from './ApiConfig';

const empresaHeaders = (empresaId, json = false) => ({
    ...(json ? { 'Content-Type': 'application/json' } : {}),
    'X-Empresa-Id': String(empresaId)
});

const IaService = {
    listarSessoes: async (empresaId) =>
        apiFetch(`${ApiConfig.BASE_URL}/api/ia/sessoes`, {
            headers: empresaHeaders(empresaId)
        }),

    criarSessao: async (empresaId, sessao) =>
        apiFetch(`${ApiConfig.BASE_URL}/api/ia/sessoes`, {
            method: 'POST',
            headers: empresaHeaders(empresaId, true),
            body: JSON.stringify(sessao)
        }),

    atualizarSessao: async (empresaId, id, sessao) =>
        apiFetch(`${ApiConfig.BASE_URL}/api/ia/sessoes/${id}`, {
            method: 'PUT',
            headers: empresaHeaders(empresaId, true),
            body: JSON.stringify(sessao)
        }),

    excluirSessao: async (empresaId, id) =>
        apiFetch(`${ApiConfig.BASE_URL}/api/ia/sessoes/${id}`, {
            method: 'DELETE',
            headers: empresaHeaders(empresaId)
        }),

    listarMensagens: async (empresaId, sessaoId) =>
        apiFetch(`${ApiConfig.BASE_URL}/api/ia/mensagens/sessao/${sessaoId}/ordenado`, {
            headers: empresaHeaders(empresaId)
        }),

    enviarMensagem: async (empresaId, request) =>
        apiFetch(`${ApiConfig.BASE_URL}/api/ia/chat`, {
            method: 'POST',
            headers: empresaHeaders(empresaId, true),
            body: JSON.stringify(request)
        }),

    chatSimples: async (empresaId, usuarioId, mensagem) =>
        apiFetch(`${ApiConfig.BASE_URL}/api/ia/chat/simples?usuarioId=${usuarioId}&mensagem=${encodeURIComponent(mensagem)}`, {
            method: 'POST',
            headers: empresaHeaders(empresaId)
        }),

    classificarTexto: async (empresaId, tipo, texto) =>
        apiFetch(`${ApiConfig.BASE_URL}/api/ia/classificacoes/classificar-texto?tipo=${encodeURIComponent(tipo)}&texto=${encodeURIComponent(texto)}`, {
            method: 'POST',
            headers: empresaHeaders(empresaId)
        }),

    analisar: async (empresaId, request) =>
        apiFetch(`${ApiConfig.BASE_URL}/api/ia/analises`, {
            method: 'POST',
            headers: empresaHeaders(empresaId, true),
            body: JSON.stringify(request)
        }),

    preverVendas: async (empresaId, produtoId, dias) =>
        apiFetch(`${ApiConfig.BASE_URL}/api/ia/analises/prever-vendas?produtoId=${produtoId}&dias=${dias}`, { headers: empresaHeaders(empresaId) }),
    preverEstoque: async (empresaId, produtoId, dias) =>
        apiFetch(`${ApiConfig.BASE_URL}/api/ia/analises/prever-estoque?produtoId=${produtoId}&dias=${dias}`, { headers: empresaHeaders(empresaId) }),
    preverFinanceiro: async (empresaId, dias) =>
        apiFetch(`${ApiConfig.BASE_URL}/api/ia/analises/prever-financeiro?dias=${dias}`, { headers: empresaHeaders(empresaId) }),
    getConfig: async (empresaId) =>
        apiFetch(`${ApiConfig.BASE_URL}/api/ia/config?empresaId=${empresaId}`),
    getRemainingTokens: async (empresaId) =>
        apiFetch(`${ApiConfig.BASE_URL}/api/ia/config/remaining-tokens?empresaId=${empresaId}`),
    testConnection: async (empresaId) =>
        apiFetch(`${ApiConfig.BASE_URL}/api/ia/config/test-connection?empresaId=${empresaId}`, { method: 'POST' }),
    gerarEmbedding: async (empresaId, texto) =>
        apiFetch(`${ApiConfig.BASE_URL}/api/ia/embeddings/gerar?texto=${encodeURIComponent(texto)}`, {
            method: 'POST',
            headers: empresaHeaders(empresaId)
        })
};

export default IaService;
