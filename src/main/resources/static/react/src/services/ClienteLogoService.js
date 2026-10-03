import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = ApiConfig.API_BASE_URL;

export const ClienteLogoService = {
    async uploadLogo(clienteId, file) {
        const formData = new FormData();
        formData.append('id', clienteId);
        formData.append('logo', file);

        const response = await apiFetch(`${BASE_URL}/cadastro/clientes/logo`, {
            method: 'POST',
            body: formData,
        });
        if (!response.ok) {
            throw new Error('Erro ao enviar logo do cliente');
        }
        const data = await response.json();
        return data.data || data;
    },

    async getLogo(id) {
        const response = await apiFetch(`${BASE_URL}/cadastro/clientes/logo/${id}`, {
            method: 'GET',
        });
        if (!response.ok) {
            throw new Error('Erro ao buscar logo do cliente');
        }
        return await response.blob();
    },

    async deleteLogo(clienteId) {
        const formData = new FormData();
        formData.append('id', clienteId);

        const response = await apiFetch(`${BASE_URL}/cadastro/clientes/logo`, {
            method: 'DELETE',
            body: formData,
        });
        if (!response.ok) {
            throw new Error('Erro ao remover logo do cliente');
        }
        return true;
    },
};

export default ClienteLogoService;
