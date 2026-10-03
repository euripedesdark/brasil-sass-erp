import ApiConfig, { apiFetch } from './ApiConfig';

const BASE_URL = ApiConfig.API_BASE_URL;

export const SuperAdminService = {
    async uploadBackground(file) {
        const formData = new FormData();
        formData.append('file', file);

        const response = await apiFetch(`${BASE_URL}/superadmin/assets/background`, {
            method: 'POST',
            body: formData,
        });
        if (!response.ok) {
            throw new Error('Erro ao enviar background');
        }
        const data = await response.json();
        return data;
    },

    async uploadLogin(file) {
        const formData = new FormData();
        formData.append('file', file);

        const response = await apiFetch(`${BASE_URL}/superadmin/assets/login`, {
            method: 'POST',
            body: formData,
        });
        if (!response.ok) {
            throw new Error('Erro ao enviar tela de login');
        }
        const data = await response.json();
        return data;
    },

    async getSystemAsset(tipo) {
        const response = await apiFetch(`${BASE_URL}/superadmin/assets/system/${tipo}`, {
            method: 'GET',
        });
        if (!response.ok) {
            throw new Error('Erro ao buscar asset do sistema');
        }
        return await response.blob();
    },

    async getUserAsset(empresaId, tipoEntidade, entidadeId) {
        const response = await apiFetch(`${BASE_URL}/superadmin/assets/user/${empresaId}/${tipoEntidade}/${entidadeId}`, {
            method: 'GET',
        });
        if (!response.ok) {
            throw new Error('Erro ao buscar asset do usuario');
        }
        return await response.blob();
    },

    async executeQuery(sql) {
        const response = await apiFetch(`${BASE_URL}/superadmin/sql/query?sql=${encodeURIComponent(sql)}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Erro ao executar query SQL');
        }
        const data = await response.json();
        return data;
    },

    async updateCell(request) {
        const response = await apiFetch(`${BASE_URL}/superadmin/sql/update-cell`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(request),
        });
        if (!response.ok) {
            throw new Error('Erro ao atualizar celula');
        }
        const data = await response.json();
        return data;
    },

    async updatePermission(request) {
        const response = await apiFetch(`${BASE_URL}/superadmin/usuarios/permissao`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(request),
        });
        if (!response.ok) {
            throw new Error('Erro ao atualizar permissao');
        }
        const data = await response.json();
        return data;
    },

    // ---------------- usuarios: criar, alterar, remover ----------------
    // Estas rotas nao existiam no backend, e o botao "Novo Usuario" da tela de
    // Usuarios nao tinha onClick. O resultado era: clica, nao acontece nada, e
    // o usuario nunca e' gravado — logo nao aparece na listagem e nao entra.

    async listarUsuarios() {
        const response = await apiFetch(`${BASE_URL}/superadmin/usuarios`);
        if (!response.ok) {
            throw new Error(await mensagemDoErro(response, 'Erro ao carregar usuarios'));
        }
        return await response.json();
    },

    async criarUsuario(dados) {
        const response = await apiFetch(`${BASE_URL}/superadmin/usuarios`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dados),
        });
        if (!response.ok) {
            throw new Error(await mensagemDoErro(response, 'Erro ao criar usuario'));
        }
        return await response.json();
    },

    async atualizarUsuario(id, dados) {
        const response = await apiFetch(`${BASE_URL}/superadmin/usuarios/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dados),
        });
        if (!response.ok) {
            throw new Error(await mensagemDoErro(response, 'Erro ao salvar usuario'));
        }
        return await response.json();
    },

    async removerUsuario(id) {
        const response = await apiFetch(`${BASE_URL}/superadmin/usuarios/${id}`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
        });
        if (!response.ok) {
            throw new Error(await mensagemDoErro(response, 'Erro ao remover usuario'));
        }
        return await response.json();
    },

    async trocarSenhaUsuario(id, novaSenha) {
        const response = await apiFetch(`${BASE_URL}/superadmin/usuarios/${id}/senha`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ novaSenha }),
        });
        if (!response.ok) {
            throw new Error(await mensagemDoErro(response, 'Erro ao trocar a senha'));
        }
        return await response.json();
    },

    /**
     * Os nomes de perfil que existem na empresa em que o usuario trabalha.
     * Vem do banco, em vez da lista fixa que a tela usava — a lista fixa
     * chutava os ids (1 = ADMIN) e mandava o id errado, que o backend
     * recusava sem a tela mostrar o motivo.
     */
    async perfisDisponiveis() {
        const response = await apiFetch(`${BASE_URL}/superadmin/usuarios/perfis-disponiveis`);
        if (!response.ok) {
            throw new Error(await mensagemDoErro(response, 'Erro ao carregar perfis'));
        }
        return await response.json();
    },
};

/**
 * O backend responde o erro em JSON ({erro, mensagem, detail}) ou em texto.
 * Sem isto a tela mostraria "[object Object]" quando o cadastro falhasse — que
 * e como um erro de senha curta ou de perfil inexistente virava silencio.
 */
async function mensagemDoErro(response, padrao) {
    try {
        const texto = await response.text();
        if (!texto) return padrao;
        try {
            const d = JSON.parse(texto);
            return d.mensagem || d.erro || d.error || d.detail || padrao;
        } catch {
            return texto.length > 200 ? texto.slice(0, 200) : texto;
        }
    } catch {
        return padrao;
    }
}

export default SuperAdminService;
