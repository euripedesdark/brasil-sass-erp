import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = `${ApiConfig.BASE_URL}/api/producao/romaneios`;

const RomaneioProducaoService = {
    listar: async (empresaId) => {
        const response = await apiFetch(`${BASE_URL}`, {
            headers: ApiConfig.getAuthHeader()
        });
        if (!response.ok) {
            throw new Error(`Erro ao listar romaneios: ${response.statusText}`);
        }
        return response.json();
    },

    criar: async (empresaId, romaneio) => {
        const response = await apiFetch(`${BASE_URL}`, {
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
    },

    /** path: conferir | liberar | cancelar */
    acao: async (id, path) => {
        const response = await apiFetch(`${BASE_URL}/${id}/${path}`, {
            method: 'POST',
            headers: ApiConfig.getAuthHeader()
        });
        if (!response.ok) {
            let msg = response.statusText;
            try {
                const j = await response.json();
                msg = j?.message || j?.errors?.[0]?.message || msg;
            } catch { /* */ }
            throw new Error(msg);
        }
        return response.json();
    }
};

export default RomaneioProducaoService;
