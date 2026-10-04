import React, { useState, useEffect, useRef, useCallback } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/producao/mps';
const fmt = (v) => Number(v ?? 0).toLocaleString('pt-BR', { minimumFractionDigits: 3 });

export const MPS = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    const [periodo, setPeriodo] = useState(() => { const d = new Date(); return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0'); });
    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? j ?? []); };
    const carregar = useCallback(async () => {
        setLoading(true);
        try { setRows(await apiFetch(BASE + '?periodo=' + periodo).then(js)); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha ao carregar MPS', life: 4000 }); }
        finally { setLoading(false); }
    }, [periodo]);
    useEffect(() => { carregar(); }, [carregar]);
    const gerar = async () => {
        const r = await apiFetch(BASE + '/gerar?periodo=' + periodo, { method: 'POST' });
        if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Use AAAA-MM', life: 3500 }); return; }
        carregar();
    };
    const confirmar = async (id) => { await apiFetch(BASE + '/' + id + '/confirmar', { method: 'POST' }); carregar(); };
    const excluir = async (id) => { await apiFetch(BASE + '/' + id, { method: 'DELETE' }); carregar(); };
    const aoMrp = async (row) => {
        const r = await apiFetch('/api/producao/mrp/gerar-sugestoes', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ produtoId: row.produtoId, quantidade: Number(row.qtdPlanejada || 0) }) });
        if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'MRP recusou', life: 3500 }); return; }
        toast.current?.show({ severity: 'success', summary: 'MRP', detail: 'Sugestões geradas', life: 3000 });
    };
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='flex justify-content-between align-items-center mb-3 flex-wrap gap-2'>
                <div><h2 className='m-0'>MPS</h2><span className='bc-muted'>Plano mestre: demanda dos pedidos menos estoque</span></div>
                <div className='flex gap-2'><InputText value={periodo} onChange={(e) => setPeriodo(e.target.value)} placeholder='AAAA-MM' maxLength={7} /><Button label='Gerar' icon='pi pi-calculator' onClick={gerar} /></div>
            </div>
            <DataTable value={rows} loading={loading} paginator rows={12} emptyMessage='Gere o plano do período.' responsiveLayout='scroll' dataKey='id'>
                <Column field='produtoId' header='Produto' />
                <Column header='Demanda' body={(r) => fmt(r.qtdDemandada)} />
                <Column header='Estoque' body={(r) => fmt(r.qtdEstoque)} />
                <Column header='Planejada' body={(r) => fmt(r.qtdPlanejada)} />
                <Column header='Status' body={(r) => <Tag value={r.status} severity={r.status === 'CONFIRMADO' ? 'success' : 'warning'} />} style={{ width: '9rem' }} />
                <Column header='' body={(r) => (<div className='flex gap-1'>
                    {r.status !== 'CONFIRMADO' && <Button label='Confirmar' size='small' outlined onClick={() => confirmar(r.id)} />}
                    <Button label='MRP' size='small' severity='help' onClick={() => aoMrp(r)} />
                    <Button icon='pi pi-trash' rounded text severity='danger' tooltip='Excluir' onClick={() => excluir(r.id)} />
                </div>)} style={{ width: '13rem' }} />
            </DataTable>
        </div>
    );
};
export default MPS;
