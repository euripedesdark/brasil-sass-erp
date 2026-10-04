import React, { useState, useEffect, useRef } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { InputTextarea } from 'primereact/inputtextarea';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/compras/devolucoes';
export const DevCompra = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [pedidos, setPedidos] = useState([]);
    const [loading, setLoading] = useState(true);
    const [dlg, setDlg] = useState(false);
    const [pedId, setPedId] = useState(null);
    const [motivo, setMotivo] = useState('');
    const [itensPed, setItensPed] = useState([]);
    const [qtds, setQtds] = useState({});
    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? []); };
    const carregar = async () => {
        setLoading(true);
        try {
            setRows(await apiFetch(BASE).then(js));
            const ps = await apiFetch('/api/compras/pedidos').then(js);
            setPedidos(ps.filter((p) => p.status === 'RECEBIDO' || p.status === 'PARCIAL'));
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', life: 3000 }); }
        finally { setLoading(false); }
    };
    useEffect(() => { carregar(); }, []);
    const escolherPedido = async (id) => {
        setPedId(id); setQtds({}); setItensPed([]);
        if (!id) return;
        try {
            const r = await apiFetch('/api/compras/pedidos/' + id);
            const j = await r.json().catch(() => null);
            const det = j?.data ?? j;
            setItensPed(det?.itens ?? []);
        } catch (e) { }
    };
    const solicitar = async () => {
        const itensReq = itensPed.filter((it) => Number(qtds[it.id] ?? 0) > 0).map((it) => ({ produtoId: it.produtoId, quantidade: Number(qtds[it.id]) }));
        if (!pedId || !motivo.trim() || !itensReq.length) return;
        try { await apiFetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ pedidoId: pedId, motivo, itens: itensReq }) }); setDlg(false); setMotivo(''); carregar(); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', life: 4000 }); }
    };
    const acao = async (id, op) => {
        try { await apiFetch(BASE + '/' + id + '/' + op, { method: 'POST' }); carregar(); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', life: 4000 }); }
    };
    const sev = (s) => (s === 'DEVOLVIDA' ? 'success' : s === 'CANCELADA' ? 'danger' : 'warning');
    const botoes = (r) => (
        <span className='flex gap-1'>
            {r.status === 'SOLICITADA' && <><Button icon='pi pi-undo' tooltip='Devolver (baixa estoque)' size='small' onClick={() => acao(r.id, 'devolver')} /><Button icon='pi pi-times' tooltip='Cancelar' size='small' severity='secondary' onClick={() => acao(r.id, 'cancelar')} /></>}
        </span>
    );
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='flex justify-content-between align-items-center mb-3'>
                <div><h2 className='m-0'>Devolucoes de compra</h2><span className='bc-muted'>Devolucao ao fornecedor com baixa de estoque</span></div>
                <Button label='Solicitar' icon='pi pi-plus' onClick={() => setDlg(true)} />
            </div>
            <DataTable value={rows} loading={loading} paginator rows={15} emptyMessage='Nenhuma devolucao.' responsiveLayout='scroll'>
                <Column field='id' header='#' style={{ width: '4rem' }} />
                <Column field='pedidoId' header='Pedido' style={{ width: '6rem' }} />
                <Column field='motivo' header='Motivo' />
                <Column field='status' header='Status' body={(r) => <Tag value={r.status} severity={sev(r.status)} />} />
                <Column header='Acoes' body={botoes} style={{ width: '7rem' }} />
            </DataTable>
            <Dialog header='Solicitar devolucao' visible={dlg} onHide={() => setDlg(false)} style={{ width: '34rem' }}>
                <div className='flex flex-column gap-3'>
                    <span><label className='bc-label'>Pedido recebido</label><Dropdown value={pedId} options={pedidos.map((p) => ({ label: '#' + p.id + ' - ' + (p.status || ''), value: p.id }))} onChange={(e) => escolherPedido(e.value)} placeholder='Escolha' filter className='w-full' /></span>
                    {itensPed.map((it) => (
                        <div key={it.id} className='flex justify-content-between align-items-center gap-2'>
                            <small>#{(it.produtoId ?? '-')} recebida {(it.quantidadeRecebida ?? 0)}</small>
                            <InputNumber value={qtds[it.id] ?? 0} onValueChange={(e) => setQtds((q) => ({ ...q, [it.id]: e.value ?? 0 }))} min={0} maxFractionDigits={3} placeholder='Qtd devolver' />
                        </div>
                    ))}
                    <span><label className='bc-label'>Motivo</label><InputTextarea value={motivo} onChange={(e) => setMotivo(e.target.value)} rows={2} className='w-full' /></span>
                    <Button label='Salvar' icon='pi pi-check' onClick={solicitar} disabled={!pedId || !motivo.trim()} />
                </div>
            </Dialog>
        </div>
    );
};
export default DevCompra;
