const VIA_CEP = 'https://viacep.com.br/ws';

export const CepService = {
    async consultar(cep) {
        const codigo = String(cep || '').replace(/\D/g, '');
        if (codigo.length !== 8) {
            throw new Error('Informe um CEP válido com 8 dígitos.');
        }

        const controller = new AbortController();
        const timeout = setTimeout(() => controller.abort(), 8000);

        try {
            const response = await fetch(VIA_CEP + '/' + codigo + '/json/', {
                headers: { Accept: 'application/json' },
                signal: controller.signal
            });
            if (!response.ok) {
                throw new Error('O serviço externo de CEP não respondeu.');
            }
            const data = await response.json();
            if (data.erro) {
                throw new Error('CEP não encontrado na consulta externa.');
            }
            return data;
        } catch (error) {
            if (error?.name === 'AbortError') {
                throw new Error('A consulta externa de CEP excedeu o tempo limite.');
            }
            throw error;
        } finally {
            clearTimeout(timeout);
        }
    }
};

export default CepService;
