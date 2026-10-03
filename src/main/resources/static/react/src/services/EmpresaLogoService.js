import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = ApiConfig.API_BASE_URL;

export const EmpresaLogoService = {
    async uploadLogo(file) {
        const formData = new FormData();
        formData.append('logo', file);

        const response = await apiFetch(`${BASE_URL}/core/empresas/logo`, {
            method: 'POST',
            body: formData,
        });
        if (!response.ok) {
            throw new Error('Erro ao enviar logo da empresa');
        }
        const data = await response.json();
        return data.data || data;
    },

    async getLogo(id) {
        const response = await apiFetch(`${BASE_URL}/core/empresas/logo/${id}`, {
            method: 'GET',
        });
        if (!response.ok) {
            throw new Error('Erro ao buscar logo da empresa');
        }
        return await response.blob();
    },

    async deleteLogo() {
        const response = await apiFetch(`${BASE_URL}/core/empresas/logo`, {
            method: 'DELETE',
        });
        if (!response.ok) {
            throw new Error('Erro ao remover logo da empresa');
        }
        return true;
    },
};

export default EmpresaLogoService;
