import React, { useState, useEffect, useRef, useCallback } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { InputTextarea } from 'primereact/inputtextarea';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/vendas/devolucoes';

export const Devolucoes = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [itens, setItens] = useState([]);
    const [sel, setSel] = useState(null);
    const [loading, setLoading] = useState(true);
    const [dlg, setDlg] = useState(false);
    const [f, setF] = useState({});
    const [linhas, setLinhas] = useState([{ produto: '', qtd: 1 }]);
    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? j ?? []); };
    const carregar = useCallback(async () => {
        setLoading(true);
        try { setRows(await apiFetch(BASE).then(js)); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha ao carregar', life: 4000 }); }
        finally { setLoading(false); }
    }, []);
    useEffect(() => { carregar(); }, [carregar]);
    const ver = async (d) => { setSel(d); setItens(await apiFetch(BASE + '/' + d.id + '/itens').then(js)); };
    const salvar = async () => {
        const itens = {};
        linhas.forEach((l) => { if (l.produto) itens[String(l.produto)] = Number(l.qtd || 0); });
        const r = await apiFetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ pedidoId: Number(f.pedidoId), motivo: f.motivo, itens }) });
        if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Verifique pedido e quantidades', life: 4500 }); return; }
        setDlg(false); setF({}); setLinhas([{ produto: '', qtd: 1 }]); carregar();
    };
    const decidir = async (id, aprovar) => { await apiFetch(BASE + '/' + id + '/decidir', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ aprovar }) }); carregar(); if (sel && sel.id === id) ver({ ...sel, status: aprovar ? 'APROVADA' : 'REJEITADA' }); };
    const receber = async (id) => { const r = await apiFetch(BASE + '/' + id + '/receber', { method: 'POST' }); if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Recebimento recusado', life: 4000 }); return; } carregar(); setSel(null); };
    const addLinha = () => setLinhas([...linhas, { produto: '', qtd: 1 }]);
    const setLinha = (i, k, v) => setLinhas(linhas.map((l, j) => (j === i ? { ...l, [k]: v } : l)));
    const sev = (s) => (s === 'RECEBIDA' ? 'success' : s === 'REJEITADA' ? 'danger' : s === 'APROVADA' ? 'info' : 'warning');
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='flex justify-content-between align-items-center mb-3 flex-wrap gap-2'>
                <div><h2 className='m-0'>Trocas e Devoluções</h2><span className='bc-muted'>Solicitar, aprovar e receber de volta ao estoque</span></div>
                <Button label='Solicitar devolução' icon='pi pi-plus' onClick={() => { setF({}); setLinhas([{ produto: '', qtd: 1 }]); setDlg(true); }} />
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
                    {sel && (<><h3 className='m-0 mb-2'>Itens da devolução #{sel.id}</h3><DataTable value={itens} paginator rows={8} emptyMessage='Sem itens.' responsiveLayout='scroll'><Column field='produtoId' header='Produto' /><Column field='quantidade' header='Qtd' /><Column field='qtdRecebida' header='Recebida' /></DataTable></>)}
                </div>
            </div>
            <Dialog visible={dlg} onHide={() => setDlg(false)} header='Solicitar devolução' modal style={{ width: 'min(96vw, 560px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-6'><label className='bc-label'>Pedido ID (faturado) *</label><InputText value={f.pedidoId || ''} onChange={(e) => setF({ ...f, pedidoId: e.target.value })} keyfilter='int' /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Motivo *</label><InputText value={f.motivo || ''} onChange={(e) => setF({ ...f, motivo: e.target.value })} /></div>
                    {linhas.map((l, i) => (<div key={i} className='bc-form-col-12 flex gap-2'><InputText value={l.produto} onChange={(e) => setLinha(i, 'produto', e.target.value)} placeholder='Produto ID' keyfilter='int' style={{ flex: 1 }} /><InputNumber value={l.qtd} onValueChange={(e) => setLinha(i, 'qtd', e.value)} minFractionDigits={3} style={{ width: '9rem' }} /><Button icon='pi pi-plus' rounded text onClick={addLinha} /></div>))}
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlg(false)} /><Button label='Solicitar' icon='pi pi-check' onClick={salvar} /></div>
            </Dialog>
        </div>
    );
};
export default Devolucoes;
