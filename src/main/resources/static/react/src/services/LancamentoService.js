import api from './ApiConfig';

export const LancamentoService = {
    // Listar lançamentos paginados com filtros
    listarPaginado: async (page = 0, size = 20, filters = {}) => {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
            sort: filters.sort || 'dataVencimento',
            direction: filters.direction || 'asc',
            ...filters
        });
        
        if (filters.tipo) params.append('tipo', filters.tipo);
        if (filters.documento) params.append('documento', filters.documento);
        if (filters.idPessoa) params.append('idPessoa', filters.idPessoa.toString());
        if (filters.baixado) params.append('baixado', filters.baixado);
        if (filters.dataInicio) params.append('dataInicio', filters.dataInicio);
        if (filters.dataFim) params.append('dataFim', filters.dataFim);
        
        const response = await api.get(`/financeiro/lancamentos/paginado?${params.toString()}`);
        return response.data;
    },

    // Listar lançamentos abertos
    listarAbertos: async () => {
        const response = await api.get('/financeiro/abertos');
        return response.data;
    },

    // Listar lançamentos baixados
    listarBaixados: async () => {
        const response = await api.get('/financeiro/baixados');
        return response.data;
    },

    // Salvar lançamento
    salvar: async (lancamento) => {
        const response = await api.post('/financeiro/lancamentos', lancamento);
        return response.data;
    },

    // Excluir lançamento
    excluir: async (id) => {
        const response = await api.delete(`/financeiro/lancamentos/${id}`);
        return response.data;
    },

    // Baixar lançamento
    baixar: async (id, dataBaixa = null, valorBaixa = null, observacaoBaixa = null) => {
        const params = new URLSearchParams();
        if (dataBaixa) params.append('dataBaixa', dataBaixa);
        if (valorBaixa) params.append('valorBaixa', valorBaixa.toString());
        if (observacaoBaixa) params.append('observacaoBaixa', observacaoBaixa);
        
        const response = await api.post(`/financeiro/${id}/baixar?${params.toString()}`);
        return response.data;
    },
    
    // Estornar baixa de lançamento
    estornar: async (id) => {
        const response = await api.post(`/financeiro/${id}/estornar`);
        return response.data;
    },

    // Obter resumo financeiro
    obterResumo: async (dataInicio = null, dataFim = null) => {
        const params = new URLSearchParams();
        if (dataInicio) params.append('dataInicio', dataInicio);
        if (dataFim) params.append('dataFim', dataFim);
        
        const response = await api.get(`/financeiro/resumo?${params.toString()}`);
        return response.data;
    }
};
