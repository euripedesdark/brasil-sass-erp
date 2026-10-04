import React, { useState, useEffect, useRef } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Toast } from 'primereact/toast';

export const OEE = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    const [de, setDe] = useState(() => { const d = new Date(); d.setDate(d.getDate() - 30); return d; });
    const [ate, setAte] = useState(new Date());
    const iso = (d) => (d ? d.toISOString().slice(0, 10) : '');
    const carregar = async () => {
        setLoading(true);
        try {
            const r = await apiFetch('/api/producao/oee?de=' + iso(de) + '&ate=' + iso(ate));
            const j = await r.json().catch(() => []);
            setRows(Array.isArray(j) ? j : (j?.data ?? []));
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', life: 3000 }); }
        finally { setLoading(false); }
    };
    useEffect(() => { carregar(); }, []);
    const media = (k) => (rows.length ? (rows.reduce((s, r) => s + Number(r[k] || 0), 0) / rows.length).toFixed(1) : '-');
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='mb-3'><h2 className='m-0'>Eficiência Operacional</h2><span className='bc-muted'>Disponibilidade × qualidade por dia (sem performance: apontamento não vincula operação)</span></div>
            <div className='flex gap-2 mb-3 flex-wrap align-items-end'>
                <span><label className='bc-label'>De</label><Calendar value={de} onChange={(e) => setDe(e.value)} dateFormat='dd/mm/yy' showIcon /></span>
                <span><label className='bc-label'>Até</label><Calendar value={ate} onChange={(e) => setAte(e.value)} dateFormat='dd/mm/yy' showIcon /></span>
                <Button label='Atualizar' icon='pi pi-refresh' onClick={carregar} />
            </div>
            <div className='grid mb-3'>
                <div className='bc-form-col-4 col-12 md:col-4'><Card><small>Disponibilidade média %</small><div className='text-2xl font-bold'>{media('disponibilidade')}</div></Card></div>
                <div className='bc-form-col-4 col-12 md:col-4'><Card><small>Qualidade média %</small><div className='text-2xl font-bold'>{media('qualidade')}</div></Card></div>
                <div className='bc-form-col-4 col-12 md:col-4'><Card><small>Dias com apontamento</small><div className='text-2xl font-bold'>{rows.length}</div></Card></div>
            </div>
            <DataTable value={rows} loading={loading} paginator rows={15} emptyMessage='Sem apontamentos no período.' responsiveLayout='scroll'>
                <Column field='dia' header='Dia' style={{ width: '8rem' }} />
                <Column field='horas' header='Horas' />
                <Column field='disponibilidade' header='Disp. %' />
                <Column field='produzida' header='Peças' />
                <Column field='refugo' header='Refugo' />
                <Column field='qualidade' header='Qual. %' />
                <Column field='pecasHora' header='Peças/h' />
            </DataTable>
        </div>
    );
};
export default OEE;
