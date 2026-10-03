import ApiConfig, { apiFetch } from './ApiConfig';

const PRODUCAO_API_URL = `${ApiConfig.BASE_URL}/api/producao`;

// Tipos de Producao
const TIPOS_PRODUCAO = [
    { label: 'Normal', value: 'NORMAL' },
    { label: 'Reprocesso', value: 'REPROCESSO' },
    { label: 'Amostra', value: 'AMOSTRA' },
    { label: 'Manutenção', value: 'MANUTENCAO' }
];

// Status de Producao
const STATUS_PRODUCAO = [
    { label: 'Aberto', value: 'ABERTO' },
    { label: 'Em Processo', value: 'EM_PROCESSO' },
    { label: 'Pausado', value: 'PAUSADO' },
    { label: 'Finalizado', value: 'FINALIZADO' },
    { label: 'Cancelado', value: 'CANCELADO' }
];

// Unidades de Medida para Producao
const UNIDADES_MEDIDA = [
    { label: 'Quilograma (KG)', value: 'KG' },
    { label: 'Grama (G)', value: 'G' },
    { label: 'Tonelada (T)', value: 'T' },
    { label: 'Litro (L)', value: 'L' },
    { label: 'Mililitro (ML)', value: 'ML' },
    { label: 'Unidade (UN)', value: 'UN' },
    { label: 'Caixa (CX)', value: 'CX' },
    { label: 'Metro (M)', value: 'M' },
    { label: 'Centímetro (CM)', value: 'CM' },
    { label: 'Milímetro (MM)', value: 'MM' }
];

/**
 * Servico de API para o modulo de Producao
 */
const ProducaoService = {
    /**
     * Lista todas as ordens de producao de uma empresa
     * @param {number} empresaId - ID da empresa
     * @param {Object} params - Parametros de filtragem (opcional)
     * @returns {Promise<Array>} - Lista de ordens de producao
     */
    listarOrdens: async (empresaId, params = {}) => {
        try {
            const queryParams = new URLSearchParams({
                empresaId: empresaId,
                ...params
            });
            
            const response = await apiFetch(`${PRODUCAO_API_URL}?${queryParams}`);
            
            if (!response.ok) {
                throw new Error(`Erro ao listar ordens: ${response.statusText}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Erro no ProducaoService.listarOrdens:', error);
            throw error;
        }
    },

    /**
     * Busca uma ordem de producao pelo ID
     * @param {number} id - ID da ordem de producao
     * @param {number} empresaId - ID da empresa
     * @returns {Promise<Object>} - Ordem de producao
     */
    buscarPorId: async (id, empresaId) => {
        try {
            const response = await apiFetch(`${PRODUCAO_API_URL}/${id}?empresaId=${empresaId}`);
            
            if (!response.ok) {
                throw new Error(`Erro ao buscar ordem: ${response.statusText}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Erro no ProducaoService.buscarPorId:', error);
            throw error;
        }
    },

    /**
     * Cria uma nova ordem de producao
     * @param {number} empresaId - ID da empresa
     * @param {Object} ordem - Dados da ordem de producao
     * @returns {Promise<Object>} - Ordem de producao criada
     */
    criarOrdem: async (empresaId, ordem) => {
        try {
            const response = await apiFetch(`${PRODUCAO_API_URL}?empresaId=${empresaId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...ApiConfig.getAuthHeader()
                },
                body: JSON.stringify(ordem)
            });
            
            if (!response.ok) {
                const errorData = await response.text();
                throw new Error(errorData || `Erro ao criar ordem: ${response.statusText}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Erro no ProducaoService.criarOrdem:', error);
            throw error;
        }
    },

    /**
     * Atualiza uma ordem de producao existente
     * @param {number} id - ID da ordem de producao
     * @param {number} empresaId - ID da empresa
     * @param {Object} ordem - Dados atualizados da ordem
     * @returns {Promise<Object>} - Ordem de producao atualizada
     */
    atualizarOrdem: async (id, empresaId, ordem) => {
        try {
            const response = await apiFetch(`${PRODUCAO_API_URL}/${id}?empresaId=${empresaId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    ...ApiConfig.getAuthHeader()
                },
                body: JSON.stringify(ordem)
            });
            
            if (!response.ok) {
                const errorData = await response.text();
                throw new Error(errorData || `Erro ao atualizar ordem: ${response.statusText}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Erro no ProducaoService.atualizarOrdem:', error);
            throw error;
        }
    },

    /**
     * Finaliza uma ordem de producao
     * @param {number} id - ID da ordem de producao
     * @param {number} empresaId - ID da empresa
     * @returns {Promise<Object>} - Ordem de producao finalizada
     */
    finalizarOrdem: async (id, empresaId) => {
        try {
            const response = await apiFetch(`${PRODUCAO_API_URL}/${id}/finalizar?empresaId=${empresaId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...ApiConfig.getAuthHeader()
                }
            });
            
            if (!response.ok) {
                const errorData = await response.text();
                throw new Error(errorData || `Erro ao finalizar ordem: ${response.statusText}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Erro no ProducaoService.finalizarOrdem:', error);
            throw error;
        }
    },

    /**
     * Inicia uma ordem de producao (mudar status para EM_PROCESSO)
     * @param {number} id - ID da ordem de producao
     * @param {number} empresaId - ID da empresa
     * @returns {Promise<Object>} - Ordem de producao atualizada
     */
    iniciarOrdem: async (id, empresaId) => {
        try {
            const response = await apiFetch(`${PRODUCAO_API_URL}/${id}/iniciar?empresaId=${empresaId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...ApiConfig.getAuthHeader()
                }
            });
            
            if (!response.ok) {
                const errorData = await response.text();
                throw new Error(errorData || `Erro ao iniciar ordem: ${response.statusText}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Erro no ProducaoService.iniciarOrdem:', error);
            throw error;
        }
    },

    /**
     * Pausa uma ordem de producao
     * @param {number} id - ID da ordem de producao
     * @param {number} empresaId - ID da empresa
     * @returns {Promise<Object>} - Ordem de producao atualizada
     */
    pausarOrdem: async (id, empresaId) => {
        try {
            const response = await apiFetch(`${PRODUCAO_API_URL}/${id}/pausar?empresaId=${empresaId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...ApiConfig.getAuthHeader()
                }
            });
            
            if (!response.ok) {
                const errorData = await response.text();
                throw new Error(errorData || `Erro ao pausar ordem: ${response.statusText}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Erro no ProducaoService.pausarOrdem:', error);
            throw error;
        }
    },

    /**
     * Cancela uma ordem de producao
     * @param {number} id - ID da ordem de producao
     * @param {number} empresaId - ID da empresa
     * @returns {Promise<Object>} - Ordem de producao cancelada
     */
    cancelarOrdem: async (id, empresaId) => {
        try {
            const response = await apiFetch(`${PRODUCAO_API_URL}/${id}/cancelar?empresaId=${empresaId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...ApiConfig.getAuthHeader()
                }
            });
            
            if (!response.ok) {
                const errorData = await response.text();
                throw new Error(errorData || `Erro ao cancelar ordem: ${response.statusText}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Erro no ProducaoService.cancelarOrdem:', error);
            throw error;
        }
    },

    /**
     * Lista ordens de producao por status
     * @param {number} empresaId - ID da empresa
     * @param {string} status - Status da ordem
     * @returns {Promise<Array>} - Lista de ordens de producao
     */
    listarPorStatus: async (empresaId, status) => {
        try {
            const response = await apiFetch(`${PRODUCAO_API_URL}/por-status?empresaId=${empresaId}&status=${status}`);
            
            if (!response.ok) {
                throw new Error(`Erro ao listar ordens por status: ${response.statusText}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Erro no ProducaoService.listarPorStatus:', error);
            throw error;
        }
    },

    /**
     * Lista ordens de producao por produto final
     * @param {number} empresaId - ID da empresa
     * @param {number} produtoId - ID do produto final
     * @returns {Promise<Array>} - Lista de ordens de producao
     */
    listarPorProdutoFinal: async (empresaId, produtoId) => {
        try {
            const response = await apiFetch(`${PRODUCAO_API_URL}/por-produto?empresaId=${empresaId}&produtoId=${produtoId}`);
            
            if (!response.ok) {
                throw new Error(`Erro ao listar ordens por produto: ${response.statusText}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Erro no ProducaoService.listarPorProdutoFinal:', error);
            throw error;
        }
    },

    /**
     * Lista ordens de producao por periodo
     * @param {number} empresaId - ID da empresa
     * @param {string} dataInicio - Data de inicio (YYYY-MM-DD)
     * @param {string} dataFim - Data de fim (YYYY-MM-DD)
     * @returns {Promise<Array>} - Lista de ordens de producao
     */
    listarPorPeriodo: async (empresaId, dataInicio, dataFim) => {
        try {
            const response = await apiFetch(
                `${PRODUCAO_API_URL}/por-periodo?empresaId=${empresaId}&dataInicio=${dataInicio}&dataFim=${dataFim}`
            );
            
            if (!response.ok) {
                throw new Error(`Erro ao listar ordens por periodo: ${response.statusText}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Erro no ProducaoService.listarPorPeriodo:', error);
            throw error;
        }
    },

    /**
     * Obtem estatisticas de producao
     * @param {number} empresaId - ID da empresa
     * @returns {Promise<Object>} - Estatisticas de producao
     */
    getEstatisticas: async (empresaId) => {
        try {
            const response = await apiFetch(`${PRODUCAO_API_URL}/estatisticas?empresaId=${empresaId}`);
            
            if (!response.ok) {
                throw new Error(`Erro ao obter estatisticas: ${response.statusText}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('Erro no ProducaoService.getEstatisticas:', error);
            throw error;
        }
    },

    // Constants para uso nos componentes
    TIPOS_PRODUCAO,
    STATUS_PRODUCAO,
    UNIDADES_MEDIDA
};

export default ProducaoService;
