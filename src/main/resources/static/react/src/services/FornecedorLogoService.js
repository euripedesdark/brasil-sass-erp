import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = ApiConfig.API_BASE_URL;

export const FornecedorLogoService = {
    async uploadLogo(fornecedorId, file) {
        const formData = new FormData();
        formData.append('id', fornecedorId);
        formData.append('logo', file);

        const response = await apiFetch(`${BASE_URL}/cadastro/fornecedores/logo`, {
            method: 'POST',
            body: formData,
        });
        if (!response.ok) {
            throw new Error('Erro ao enviar logo do fornecedor');
        }
        const data = await response.json();
        return data.data || data;
    },

    async getLogo(id) {
        const response = await apiFetch(`${BASE_URL}/cadastro/fornecedores/logo/${id}`, {
            method: 'GET',
        });
        if (!response.ok) {
            throw new Error('Erro ao buscar logo do fornecedor');
        }
        return await response.blob();
    },

    async deleteLogo(fornecedorId) {
        const formData = new FormData();
        formData.append('id', fornecedorId);

        const response = await apiFetch(`${BASE_URL}/cadastro/fornecedores/logo`, {
            method: 'DELETE',
            body: formData,
        });
        if (!response.ok) {
            throw new Error('Erro ao remover logo do fornecedor');
        }
        return true;
    },
};

export default FornecedorLogoService;
