import axios from 'axios';

export const BASE_URL = '';
export const API_BASE_URL = '/api';

const getToken = () => localStorage.getItem('brasil-saas_token');

// Multiempresa: boa parte dos endpoints exige o header X-Empresa-Id, e sem ele
// o backend responde 400 ("Required request header 'X-Empresa-Id'"). Antes so a
// IaService enviava esse header, entao toda tela que usasse `api` ou `apiFetch`
// contra um endpoint com exigencia de empresa quebrou — e o sintoma era um 400
// sem pista do motivo. A empresa vem do /api/auth/me e e persistida pelo
// AuthContext; aqui ela e injetada uma vez, nas duas camadas HTTP.
const getEmpresaId = () => localStorage.getItem('brasil-saas_empresa_id');

export const setEmpresaId = (id) => {
    if (id === null || id === undefined || id === '') {
        localStorage.removeItem('brasil-saas_empresa_id');
        return;
    }
    localStorage.setItem('brasil-saas_empresa_id', String(id));
};

const isApiRequest = (url) => {
    if (!url) return false;
    const value = String(url);
    return value.startsWith('/api/') ||
        value === '/api' ||
        value.startsWith(window.location.origin + '/api/') ||
        value === window.location.origin + '/api';
};

const addEmpresa = (headers) => {
    const empresaId = getEmpresaId();
    if (empresaId && !headers['X-Empresa-Id'] && !headers['x-empresa-id']) {
        headers['X-Empresa-Id'] = empresaId;
    }
    return headers;
};

const addBearer = (config) => {
    const token = getToken();

    if (token && isApiRequest(config.url)) {
        config.headers = config.headers || {};
        addEmpresa(config.headers);

        if (!config.headers.Authorization && !config.headers.authorization) {
            config.headers.Authorization = 'Bearer ' + token;
        }
    }

    return config;
};

const normalizeApiPath = (config) => {
    if (typeof config.url === 'string' && config.url.startsWith('/api/')) {
        config.url = config.url.substring(4);
    } else if (config.url === '/api') {
        config.url = '/';
    }

    return config;
};

export const api = axios.create({
    baseURL: API_BASE_URL,
    headers: { 'Content-Type': 'application/json' }
});

export const getAuthHeader = () => {
    const token = getToken();
    return token ? { Authorization: 'Bearer ' + token } : {};
};

// Um unico interceptor para token e empresa, nas duas instancias (a do projeto
// e a global do axios). A do projeto tinha a propria copia, que so injetava o
// token — por isso as telas que usam `api` perdiam o X-Empresa-Id.
const comTokenEEmpresa = (config) => {
    normalizeApiPath(config);
    return addBearer(config);
};

api.interceptors.request.use(comTokenEEmpresa);
axios.interceptors.request.use(addBearer);

// Sessao expirada.
//
// O backend so aceita JWT; quando o token vence, tudo volta 401. Antes nao
// havia interceptor de resposta: cada tela recebia o 401 e mostrava "nao foi
// possivel carregar" indefinidamente, e o usuario ficava achando que o sistema
// estava quebrado, sem pista de que bastava logar de novo.
//
// A limpeza so vale para 401, nunca 403: 403 aqui significa "sem permissao",
// que e resposta legitima daquela tela.
const expirouSessao = () => {
    if (window.__bcSessaoExpirada) return; // evita redirecionar em rajada
    window.__bcSessaoExpirada = true;
    localStorage.removeItem('brasil-saas_token');
    localStorage.removeItem('brasil-saas_empresa_id');
    window.location.href = '/login';
};

/**
 * Renova o token uma vez e devolve a promessa.
 *
 * O login entrega um refreshToken que ficava guardado no localStorage sem nunca
 * ser usado: a rota POST /api/auth/refresh existia e o interceptor de 401 apenas
 * jogava o usuário para /login. Na prática a sessão morria dura na expiração do
 * token, mesmo com o refresh em mãos.
 *
 * A promessa é guardada fora para que várias respostas 401 simultâneas — o
 * dashboard dispara seis requisições de uma vez — façam UMA renovação, e não
 * seis. As demais esperam a mesma e seguem juntas.
 */
const CHAVE_REFRESH = 'brasil-saas_refresh_token';
let renovando = null;

const renovarToken = () => {
    if (renovando) return renovando;

    const refreshToken = localStorage.getItem(CHAVE_REFRESH);
    if (!refreshToken) return Promise.reject(new Error('sem refresh token'));

    renovando = fetch('/api/auth/refresh', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken })
    })
        .then(async (r) => {
            if (!r.ok) throw new Error('refresh recusado');
            const corpo = await r.json();
            const dados = corpo?.data ?? corpo;
            const novo = dados?.accessToken || dados?.token;
            if (!novo) throw new Error('refresh sem token novo');
            localStorage.setItem('brasil-saas_token', novo);
            if (dados?.refreshToken) {
                localStorage.setItem(CHAVE_REFRESH, dados.refreshToken);
            }
            return novo;
        })
        .finally(() => { renovando = null; });

    return renovando;
};

const tratarErro = (erro) => {
    const status = erro?.response?.status;
    const original = erro?.config;

    // 401 numa tentativa já renovada: o refresh token não vale mais
    if (status === 401 && original && !original.__renovado && localStorage.getItem(CHAVE_REFRESH)) {
        original.__renovado = true;
        return renovarToken()
            .then(() => axios(original))
            .catch(() => {
                expirouSessao();
                return Promise.reject(erro);
            });
    }

    if (status === 401) {
        expirouSessao();
    }
    return Promise.reject(erro);
};

api.interceptors.response.use((r) => r, tratarErro);
axios.interceptors.response.use((r) => r, tratarErro);

/**
 * Cliente HTTP único para chamadas que ainda usam `fetch`.
 *
 * O backend (SecurityConfig + JwtAuthenticationFilter) so aceita o token JWT
 * no header `Authorization`; nao ha sessao nem cookie. O `fetch` nativo nao
 * passa pelos interceptors do axios, entao toda chamada feita com ele voltava
 * 401. Este wrapper injeta o header e mantem a mesma assinatura do `fetch`,
 * devolvendo a `Response` original (`ok`, `json()`, `blob()`, `status`...).
 *
 * URLs absolutas, `blob:` e `data:` sao repassadas intactas para o fetch puro,
 * porque nao pertencem a API.
 */
const isExternal = (url) => /^(?:[a-z][a-z0-9+.-]*:)?\/\//i.test(url)
    || url.startsWith('blob:')
    || url.startsWith('data:');

const hasAuthHeader = (headers) => {
    if (!headers) return false;
    if (typeof headers.has === 'function') return headers.has('Authorization');
    return Object.keys(headers).some((k) => k.toLowerCase() === 'authorization');
};

export const apiFetch = (input, init = {}) => {
    if (typeof input !== 'string' || isExternal(input)) {
        return fetch(input, init);
    }

    const token = getToken();
    if (!token) {
        return fetch(input, init);
    }

    const options = { ...init };
    if (!hasAuthHeader(options.headers)) {
        options.headers = {
            ...(options.headers || {}),
            Authorization: 'Bearer ' + token
        };
    }
    // mesma empresa que o axios injeta, para as duas camadas se comportarem igual
    options.headers = addEmpresa({ ...(options.headers || {}) });

    return fetch(input, options).then((resposta) => {
        // 401 com refresh token em mãos: renova uma vez e repete a chamada.
        // Sem isto, a sessao morre na expiracao do token mesmo com o refresh
        // guardado — que e o que acontecia, ja que o interceptor do axios so
        // cobria as chamadas feitas com axios.
        if (resposta.status !== 401 || options.__renovado || !localStorage.getItem(CHAVE_REFRESH)) {
            return resposta;
        }
        return renovarToken()
            .then(() => apiFetch(input, { ...init, __renovado: true }))
            .catch(() => {
                expirouSessao();
                return resposta;
            });
    });
};

/**
 * Normaliza a resposta do backend para lista + total.
 *
 * O projeto convive com dois formatos e ninguem tinha um lugar unico para
 * lidar com isso:
 *
 *   - PageResponse cru:      { content: [...], totalElements: n, ... }
 *   - ApiResponse envelope:  { success, code, data: <PageResponse | [...]> }
 *
 * Quando o componente passava o objeto inteiro para o `value` do DataTable do
 * PrimeReact (que espera array), o `data.map` do componente estourava com
 * TypeError. Sem ErrorBoundary, isso desmontava a arvore inteira: a tela nao
 * "nao abria", o aplicativo inteiro caia — o que parece um bug de rota.
 *
 * Aqui a regra fica em um lugar so, e nenhuma tela precisa saber qual dos dois
 * formatos o controller daquele recurso usa.
 */
export const desembrulharLista = (resposta) => {
    // AxiosResponse cru: pega o corpo
    let corpo = resposta;
    if (corpo && typeof corpo === 'object' && 'data' in corpo && 'status' in corpo) {
        corpo = corpo.data;
    }
    // envelope { success, code, data }
    if (corpo && typeof corpo === 'object' && !Array.isArray(corpo) && 'data' in corpo
        && ('success' in corpo || 'code' in corpo)) {
        corpo = corpo.data;
    }
    // PageResponse
    if (corpo && typeof corpo === 'object' && Array.isArray(corpo.content)) {
        return {
            lista: corpo.content,
            total: corpo.totalElements ?? corpo.content.length,
            pagina: corpo.number ?? 0,
            tamanho: corpo.size ?? corpo.content.length
        };
    }
    if (Array.isArray(corpo)) {
        return { lista: corpo, total: corpo.length, pagina: 0, tamanho: corpo.length };
    }
    return { lista: [], total: 0, pagina: 0, tamanho: 0 };
};

const ApiConfig = { BASE_URL, API_BASE_URL, getAuthHeader, apiFetch, setEmpresaId, desembrulharLista };
export default ApiConfig;
