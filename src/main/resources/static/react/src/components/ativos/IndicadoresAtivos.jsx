import React, { useState, useEffect, useRef } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Toast } from 'primereact/toast';

export const IndicadoresAtivos = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    useEffect(() => {
        apiFetch('/api/ativos/indicadores').then((r) => r.json().catch(() => [])).then((j) => setRows(Array.isArray(j) ? j : (j?.data ?? []))).catch(() => toast.current?.show({ severity: 'error', summary: 'Erro', life: 3000 })).finally(() => setLoading(false));
    }, []);
    const num = (v) => (v == null || Number(v) < 0 ? '—' : Number(v));
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='mb-3'><h2 className='m-0'>Indicadores de Ativos</h2><span className='bc-muted'>MTBF/MTTR em dias corridos, custo e dias sem falha</span></div>
            <DataTable value={rows} loading={loading} paginator rows={12} emptyMessage='Sem ativos.' responsiveLayout='scroll'>
                <Column field='codigo' header='Ativo' style={{ width: '7rem' }} />
                <Column field='descricao' header='Descrição' />
                <Column field='corretivas' header='Corretivas' style={{ width: '6rem' }} />
                <Column field='preventivas' header='Preventivas' style={{ width: '7rem' }} />
                <Column header='MTBF (dias)' body={(r) => num(r.mtbfDias)} style={{ width: '7rem' }} />
                <Column header='MTTR (dias)' body={(r) => num(r.mttrDias)} style={{ width: '7rem' }} />
                <Column field='diasSemFalha' header='Sem falha há' style={{ width: '7rem' }} />
                <Column field='custo' header='Custo' body={(r) => Number(r.custo ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })} style={{ width: '9rem' }} />
            </DataTable>
        </div>
    );
};
export default IndicadoresAtivos;
