import React, { useState, useEffect, useRef, useCallback } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { localeAtivo } from '../shared/LocaleData.js';

const BASE = '/api/vendas/devolucoes';
const PEDIDOS = '/api/vendas/pedidos';
const ATIVAS = ['SOLICITADA', 'APROVADA', 'RECEBIDA'];
const num = (v) => Number(v ?? 0);
const fmt = (v) => num(v).toLocaleString(localeAtivo(), { maximumFractionDigits: 3 });

export const Devolucoes = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [itens, setItens] = useState([]);
    const [sel, setSel] = useState(null);
    const [loading, setLoading] = useState(true);
    const [dlg, setDlg] = useState(false);
    const [f, setF] = useState({});
    const [linhas, setLinhas] = useState([]);
    const [buscando, setBuscando] = useState(false);
    const [enviando, setEnviando] = useState(false);

    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? j ?? []); };
    const msg = async (r, padrao) => { const j = await r.json().catch(() => null); return j?.message || j?.errors?.[0]?.message || padrao; };
    const erro = (detail) => toast.current?.show({ severity: 'error', summary: 'Erro', detail, life: 4500 });
    const aviso = (detail) => toast.current?.show({ severity: 'warn', summary: 'Atenção', detail, life: 4000 });

    const carregar = useCallback(async () => {
        setLoading(true);
        try { setRows(await apiFetch(BASE).then(js)); }
        catch (e) { erro('Falha ao carregar devoluções'); }
        finally { setLoading(false); }
    }, []);
    useEffect(() => { carregar(); }, [carregar]);

    const ver = async (d) => {
        setSel(d);
        try { setItens(await apiFetch(BASE + '/' + d.id + '/itens').then(js)); } catch (e) { setItens([]); erro('Falha ao carregar itens'); }
    };

    // Mostra o que ainda pode ser devolvido: vendido menos o que ja esta em devolucoes ativas do pedido.
    const buscarPedido = async () => {
        const id = Number(f.pedidoId);
        if (!id) { aviso('Informe o número do pedido'); return; }
        setBuscando(true);
        setLinhas([]);
        try {
            const r = await apiFetch(PEDIDOS + '/' + id);
            if (!r.ok) throw new Error(await msg(r, 'Pedido não encontrado'));
            const j = await r.json();
            const pedido = j?.data ?? j;
            if (pedido.status !== 'FATURADO') throw new Error('Somente pedido faturado aceita devolução (status atual: ' + pedido.status + ')');
            const vendidas = new Map();
            (pedido.itens || []).filter((i) => i.produtoId).forEach((i) => {
                const atual = vendidas.get(i.produtoId) || { produtoId: i.produtoId, descricao: i.descricao, vendida: 0 };
                atual.vendida += num(i.quantidade);
                vendidas.set(i.produtoId, atual);
            });
            const devolvido = new Map();
            for (const d of rows.filter((x) => x.pedidoId === id && ATIVAS.includes(x.status))) {
                const lista = await apiFetch(BASE + '/' + d.id + '/itens').then(js);
                lista.forEach((i) => devolvido.set(i.produtoId, (devolvido.get(i.produtoId) || 0) + num(i.quantidade)));
            }
            const lista = [...vendidas.values()].map((v) => {
                const devolvida = devolvido.get(v.produtoId) || 0;
                return { ...v, devolvida, disponivel: Math.max(0, v.vendida - devolvida), qtd: null };
            });
            if (lista.length === 0) throw new Error('O pedido não tem itens de produto para devolver');
            setLinhas(lista);
        } catch (e) { erro(e.message); }
        finally { setBuscando(false); }
    };

    const setQtd = (produtoId, v) => setLinhas((ls) => ls.map((l) => (l.produtoId === produtoId ? { ...l, qtd: v } : l)));

    const solicitar = async () => {
        const motivo = (f.motivo || '').trim();
        const escolhidos = linhas.filter((l) => num(l.qtd) > 0);
        if (!f.pedidoId || linhas.length === 0) { aviso('Busque o pedido faturado primeiro'); return; }
        if (!motivo) { aviso('Informe o motivo'); return; }
        if (escolhidos.length === 0) { aviso('Informe a quantidade de ao menos um item'); return; }
        const acima = escolhidos.find((l) => num(l.qtd) > l.disponivel);
        if (acima) { aviso('Produto ' + acima.produtoId + ': máximo devolvível é ' + fmt(acima.disponivel)); return; }
        const corpo = {};
        escolhidos.forEach((l) => { corpo[String(l.produtoId)] = num(l.qtd); });
        setEnviando(true);
        try {
            const r = await apiFetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ pedidoId: Number(f.pedidoId), motivo, itens: corpo }) });
            if (!r.ok) throw new Error(await msg(r, 'Verifique pedido e quantidades'));
            toast.current?.show({ severity: 'success', summary: 'Devolução solicitada', life: 2500 });
            setDlg(false); setF({}); setLinhas([]); carregar();
        } catch (e) { erro(e.message); }
        finally { setEnviando(false); }
    };

    const decidir = async (id, aprovar) => {
        try {
            const r = await apiFetch(BASE + '/' + id + '/decidir', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ aprovar }) });
            if (!r.ok) throw new Error(await msg(r, 'Decisão recusada'));
            toast.current?.show({ severity: 'success', summary: aprovar ? 'Devolução aprovada' : 'Devolução rejeitada', life: 2500 });
            await carregar();
            if (sel && sel.id === id) setSel({ ...sel, status: aprovar ? 'APROVADA' : 'REJEITADA' });
        } catch (e) { erro(e.message); }
    };

    const receber = async (id) => {
        if (!window.confirm('Receber a devolução #' + id + '? Os itens voltam ao saldo de estoque e isso não pode ser repetido.')) return;
        try {
            const r = await apiFetch(BASE + '/' + id + '/receber', { method: 'POST' });
            if (!r.ok) throw new Error(await msg(r, 'Recebimento recusado'));
            toast.current?.show({ severity: 'success', summary: 'Devolução recebida em estoque', life: 3000 });
            setSel(null); carregar();
        } catch (e) { erro(e.message); }
    };

    const sev = (s) => (s === 'RECEBIDA' ? 'success' : s === 'REJEITADA' ? 'danger' : s === 'APROVADA' ? 'info' : 'warning');
    const abrir = () => { setF({}); setLinhas([]); setDlg(true); };

    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='flex justify-content-between align-items-center mb-3 flex-wrap gap-2'>
                <div><h2 className='m-0'>Trocas e Devoluções</h2><span className='bc-muted'>Solicitar, aprovar e receber de volta ao estoque</span></div>
                <Button label='Solicitar devolução' icon='pi pi-plus' onClick={abrir} />
            </div>
            <div className='grid'>
                <div className='bc-form-col-6 col-12 md:col-6'>
                    <DataTable value={rows} loading={loading} paginator rows={10} emptyMessage='Nenhuma devolução.' responsiveLayout='scroll' dataKey='id' selectionMode='single' selection={sel} onSelectionChange={(e) => e.value && ver(e.value)}>
                        <Column field='pedidoId' header='Pedido' style={{ width: '6rem' }} />
                        <Column field='motivo' header='Motivo' />
                        <Column header='Status' body={(r) => <Tag value={r.status} severity={sev(r.status)} />} style={{ width: '9rem' }} />
                        <Column header='' body={(r) => (<div className='flex gap-1'>
                            {r.status === 'SOLICITADA' && (<><Button icon='pi pi-check' rounded text severity='success' tooltip='Aprovar' onClick={() => decidir(r.id, true)} /><Button icon='pi pi-times' rounded text severity='danger' tooltip='Rejeitar' onClick={() => decidir(r.id, false)} /></>)}
                            {r.status === 'APROVADA' && (<Button label='Receber' size='small' severity='help' onClick={() => receber(r.id)} />)}
                        </div>)} style={{ width: '9rem' }} />
                    </DataTable>
                </div>
                <div className='bc-form-col-6 col-12 md:col-6'>
                    {!sel && (<div className='p-3 border-round surface-card bc-muted'>Selecione uma devolução.</div>)}
                    {sel && (<><h3 className='m-0 mb-2'>Itens da devolução #{sel.id} <Tag value={sel.status} severity={sev(sel.status)} /></h3><p className='bc-muted mt-0'>Pedido {sel.pedidoId} — {sel.motivo}</p><DataTable value={itens} paginator rows={8} emptyMessage='Sem itens.' responsiveLayout='scroll'><Column field='produtoId' header='Produto' /><Column header='Qtd' body={(r) => fmt(r.quantidade)} /><Column header='Recebida' body={(r) => fmt(r.qtdRecebida)} /></DataTable></>)}
                </div>
            </div>
            <Dialog visible={dlg} onHide={() => setDlg(false)} header='Solicitar devolução' modal style={{ width: 'min(96vw, 640px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-8 col-8'><label className='bc-label'>Pedido faturado (ID) *</label><InputText value={f.pedidoId || ''} onChange={(e) => { setF({ ...f, pedidoId: e.target.value }); setLinhas([]); }} keyfilter='int' onKeyDown={(e) => e.key === 'Enter' && buscarPedido()} /></div>
                    <div className='bc-form-col-4 col-4 flex align-items-end'><Button label='Buscar' icon='pi pi-search' outlined loading={buscando} onClick={buscarPedido} className='w-full' /></div>
                    <div className='bc-form-col-12 col-12'><label className='bc-label'>Motivo * <span className='bc-muted'>({(f.motivo || '').length}/100)</span></label><InputText value={f.motivo || ''} maxLength={100} onChange={(e) => setF({ ...f, motivo: e.target.value })} /></div>
                </div>
                {linhas.length > 0 && (
                    <DataTable value={linhas} dataKey='produtoId' responsiveLayout='scroll' className='mt-3' size='small'>
                        <Column header='Produto' body={(l) => (<span>{l.produtoId}{l.descricao ? ' — ' + l.descricao : ''}</span>)} />
                        <Column header='Vendida' body={(l) => fmt(l.vendida)} style={{ width: '5.5rem' }} />
                        <Column header='Já devolvida' body={(l) => fmt(l.devolvida)} style={{ width: '6.5rem' }} />
                        <Column header='Disponível' body={(l) => (<Tag value={fmt(l.disponivel)} severity={l.disponivel > 0 ? 'success' : 'danger'} />)} style={{ width: '6rem' }} />
                        <Column header='Devolver' body={(l) => (<InputNumber value={l.qtd} onValueChange={(e) => setQtd(l.produtoId, e.value)} min={0} max={l.disponivel} maxFractionDigits={3} disabled={l.disponivel <= 0} inputStyle={{ width: '6rem' }} />)} style={{ width: '8rem' }} />
                    </DataTable>
                )}
                <div className='flex justify-content-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlg(false)} /><Button label='Solicitar' icon='pi pi-check' loading={enviando} disabled={linhas.length === 0} onClick={solicitar} /></div>
            </Dialog>
        </div>
    );
};
export default Devolucoes;
