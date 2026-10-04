import React, { useState, useEffect, useRef } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Toast } from 'primereact/toast';
import { Button } from 'primereact/button';

export const IndicadoresAtivos = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    const carregar = () => {
        setLoading(true);
        apiFetch('/api/ativos/indicadores').then((r) => r.json().catch(() => [])).then((j) => setRows(Array.isArray(j) ? j : (j?.data ?? []))).catch(() => toast.current?.show({ severity: 'error', summary: 'Erro', life: 3000 })).finally(() => setLoading(false));
    };
    useEffect(() => { carregar(); }, []);
    const gerar = async () => {
        try {
            const r = await apiFetch('/api/ativos/indicadores/gerar-preventivas', { method: 'POST' });
            const j = await r.json().catch(() => []);
            const n = Array.isArray(j) ? j.length : 0;
            toast.current?.show({ severity: n ? 'success' : 'info', summary: n ? n + ' preventiva(s) criada(s)' : 'Nada a gerar', life: 5000 });
            carregar();
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', life: 3000 }); }
    };
    const num = (v) => (v == null || Number(v) < 0 ? '—' : Number(v));
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='flex justify-content-between align-items-center mb-3'><div><h2 className='m-0'>Indicadores de Ativos</h2><span className='bc-muted'>MTBF/MTTR em dias corridos, proxima preventiva pela MTBF</span></div><Button label='Gerar preventivas' icon='pi pi-calendar-plus' onClick={gerar} /></div>
            <DataTable value={rows} loading={loading} paginator rows={12} emptyMessage='Sem ativos.' responsiveLayout='scroll'>
                <Column field='codigo' header='Ativo' style={{ width: '7rem' }} />
                <Column field='descricao' header='Descrição' />
                <Column field='corretivas' header='Corretivas' style={{ width: '6rem' }} />
                <Column field='preventivas' header='Preventivas' style={{ width: '7rem' }} />
                <Column header='MTBF (dias)' body={(r) => num(r.mtbfDias)} style={{ width: '7rem' }} />
                <Column header='MTTR (dias)' body={(r) => num(r.mttrDias)} style={{ width: '7rem' }} />
                <Column field='proximaPreventiva' header='Prox. preventiva' body={(r) => r.proximaPreventiva || '-'} style={{ width: '9rem' }} />
                <Column field='diasSemFalha' header='Sem falha há' style={{ width: '7rem' }} />
                <Column field='custo' header='Custo' body={(r) => Number(r.custo ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })} style={{ width: '9rem' }} />
            </DataTable>
        </div>
    );
};
export default IndicadoresAtivos;
