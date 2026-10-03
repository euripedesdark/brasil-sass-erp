import { api } from './ApiConfig';

export const RecentService = {
    async getUpdates(empresaId = null, limite = 20) {
        const response = await api.get('/core/recent/updates', {
            params: {
                empresaId,
                limite
            }
        });
        return response.data;
    }
};

export default RecentService;
