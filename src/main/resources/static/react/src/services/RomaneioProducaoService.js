import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = `${ApiConfig.BASE_URL}/api/producao/romaneios`;

const RomaneioProducaoService = {
    listar: async (empresaId) => {
        const response = await apiFetch(`${BASE_URL}?empresaId=${empresaId}`, {
            headers: ApiConfig.getAuthHeader()
        });
        if (!response.ok) {
            throw new Error(`Erro ao listar romaneios: ${response.statusText}`);
        }
        return response.json();
    },

    criar: async (empresaId, romaneio) => {
        const response = await apiFetch(`${BASE_URL}?empresaId=${empresaId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...ApiConfig.getAuthHeader()
            },
            body: JSON.stringify(romaneio)
        });
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || `Erro ao criar romaneio: ${response.statusText}`);
        }
        return response.json();
    }
};

export default RomaneioProducaoService;
