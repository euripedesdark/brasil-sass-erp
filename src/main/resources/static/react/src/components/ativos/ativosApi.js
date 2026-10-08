import {apiFetch} from '../../services/ApiConfig';

export const brl = v => Number(v || 0).toLocaleString('pt-BR', {style: 'currency', currency: 'BRL'});

/** Chama a API e devolve o JSON; em erro lanca Error com a mensagem do backend. */
export async function api(url, method = 'GET', body) {
    const opts = {method};
    if (body !== undefined) {
        opts.headers = {'Content-Type': 'application/json'};
        opts.body = JSON.stringify(body);
    }
    const r = await apiFetch(url, opts);
    if (!r.ok) {
        let msg = `Erro ${r.status}`;
        try { const j = await r.json(); msg = j.message || j.detail || j.error || msg; } catch { /* sem corpo */ }
        throw new Error(msg);
    }
    if (r.status === 204) return null;
    const txt = await r.text();
    return txt ? JSON.parse(txt) : null;
}

/** Lista opcional de outro modulo: sem permissao vira lista vazia. */
export async function opcional(url) {
    try { const d = await api(url); return Array.isArray(d) ? d : (d?.content || []); } catch { return []; }
}

export const isoDate = d => d ? new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 10) : null;
export const periodoAtual = () => new Date().toISOString().slice(0, 7);
