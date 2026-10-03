import { apiFetch } from './ApiConfig';

const BASE = '/api/rh/funcionarios';

/**
 * Foto do colaborador.
 *
 * Mesma regra da imagem de produto: os bytes ficam no MongoDB e o Postgres
 * guarda só a referência (fotoUrl, tipo e tamanho). A leitura é por GET com o
 * id e devolve os bytes crus — não existe URL pública, então quem chama busca o
 * blob e monta um objectURL.
 *
 * As três rotas são /{id}/foto, com o id no caminho, igual a /{id}/imagens do
 * produto. Antes o id vinha na query (?id=) e isso quebrava o reuso do
 * componente compartilhado de imagem.
 */
export const FuncionarioFotoService = {
    async uploadFoto(funcionarioId, file) {
        const formData = new FormData();
        formData.append('foto', file);

        const response = await apiFetch(`${BASE}/${funcionarioId}/foto`, {
            method: 'POST',
            body: formData
        });
        if (!response.ok) {
            throw new Error('Não foi possível enviar a foto');
        }
        const data = await response.json();
        return data.data || data;
    },

    async getFoto(id) {
        const response = await apiFetch(`${BASE}/${id}/foto`, { method: 'GET' });
        if (!response.ok) {
            throw new Error('Não foi possível buscar a foto');
        }
        return await response.blob();
    },

    async deleteFoto(funcionarioId) {
        const response = await apiFetch(`${BASE}/${funcionarioId}/foto`, { method: 'DELETE' });
        if (!response.ok) {
            throw new Error('Não foi possível remover a foto');
        }
        return true;
    }
};

export default FuncionarioFotoService;
