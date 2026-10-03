import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = ApiConfig.API_BASE_URL;

export const LogosService = {
    async uploadLogoEmpresa(file) {
        const formData = new FormData();
        formData.append('logo', file);

        const response = await apiFetch(`${BASE_URL}/core/logos/empresa`, {
            method: 'POST',
            body: formData,
        });
        if (!response.ok) {
            throw new Error('Erro ao enviar logo da empresa');
        }
        const data = await response.json();
        return data.data || data;
    },

    async getLogoEmpresa(id) {
        const response = await apiFetch(`${BASE_URL}/core/logos/empresa/${id}`, {
            method: 'GET',
        });
        if (!response.ok) {
            throw new Error('Erro ao buscar logo da empresa');
        }
        return await response.blob();
    },
};

export default LogosService;
