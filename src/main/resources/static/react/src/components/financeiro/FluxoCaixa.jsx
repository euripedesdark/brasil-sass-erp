import React, { useState, useEffect, useRef } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';

const fmt = (v) => Number(v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export const FluxoCaixa = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [dias, setDias] = useState(90);
    const [loading, setLoading] = useState(true);
    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? []); };
    const carregar = async (d) => {
        setLoading(true);
        try { setRows(await apiFetch('/api/financeiro/fluxo-caixa?dias=' + (d ?? dias)).then(js)); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha ao projetar', life: 4000 }); }
        finally { setLoading(false); }
    };
    useEffect(() => { carregar(90); }, []);
    const tot = (k) => rows.reduce((s, r) => s + Number(r[k] || 0), 0);
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='flex justify-content-between align-items-center mb-3 flex-wrap gap-2'>
                <div><h2 className='m-0'>Fluxo de Caixa Projetado</h2><span className='bc-muted'>Entradas × saídas por semana, a partir dos títulos</span></div>
                <div className='flex gap-2 align-items-end'><Dropdown value={dias} options={[30, 60, 90, 180].map((d) => ({ label: d + ' dias', value: d }))} onChange={(e) => { setDias(e.value); carregar(e.value); }} /></div>
            </div>
            <div className='grid mb-3'>
                <div className='bc-form-col-4 col-12 md:col-4'><div className='p-3 border-round surface-card'><small>Entradas</small><div className='text-2xl font-bold' style={{ color: 'var(--green-500)' }}>{fmt(tot('entradas'))}</div></div></div>
                <div className='bc-form-col-4 col-12 md:col-4'><div className='p-3 border-round surface-card'><small>Saídas</small><div className='text-2xl font-bold' style={{ color: 'var(--red-500)' }}>{fmt(tot('saidas'))}</div></div></div>
                <div className='bc-form-col-4 col-12 md:col-4'><div className='p-3 border-round surface-card'><small>Líquido</small><div className='text-2xl font-bold'>{fmt(tot('entradas') - tot('saidas'))}</div></div></div>
            </div>
            <DataTable value={rows} loading={loading} paginator rows={14} emptyMessage='Sem títulos no período.' responsiveLayout='scroll'>
                <Column field='semana' header='Semana' style={{ width: '7rem' }} />
                <Column header='Entradas' body={(r) => fmt(r.entradas)} />
                <Column header='Saídas' body={(r) => fmt(r.saidas)} />
                <Column header='Líquido' body={(r) => fmt(r.liquido)} />
                <Column header='Acumulado' body={(r) => fmt(r.acumulado)} />
            </DataTable>
        </div>
    );
};
export default FluxoCaixa;
