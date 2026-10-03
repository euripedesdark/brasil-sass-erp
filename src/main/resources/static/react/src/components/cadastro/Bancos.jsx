import React, { useState, useRef } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Checkbox } from 'primereact/checkbox';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

export const Bancos = () => {
    const toast = useRef(null);
    const [busca, setBusca] = useState('');
    const [soPix, setSoPix] = useState(false);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(false);
    const buscar = async () => {
        setLoading(true);
        try {
            let url = '/api/core/bancos';
            const qs = [];
            if (busca.trim()) qs.push('busca=' + encodeURIComponent(busca.trim()));
            if (soPix) qs.push('apenasPix=true');
            if (qs.length) url += '?' + qs.join('&');
            const r = await apiFetch(url);
            const j = await r.json().catch(() => []);
            setRows(Array.isArray(j) ? j : (j?.data ?? j?.lista ?? []));
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha na busca', life: 4000 }); }
        finally { setLoading(false); }
    };
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='mb-3'><h2 className='m-0'>Bancos</h2><span className='bc-muted'>Catálogo COMPE/ISPB (513 instituições)</span></div>
            <div className='flex gap-2 mb-3 flex-wrap align-items-center'>
                <InputText value={busca} onChange={(e) => setBusca(e.target.value)} placeholder='Nome ou código...' style={{ flex: 1, minWidth: '16rem' }} onKeyDown={(e) => e.key === 'Enter' && buscar()} />
                <span className='flex align-items-center gap-2'><Checkbox checked={soPix} onChange={(e) => setSoPix(e.checked)} inputId='pix' /><label htmlFor='pix'>Só PIX</label></span>
                <Button label='Buscar' icon='pi pi-search' onClick={buscar} loading={loading} />
            </div>
            <DataTable value={rows} loading={loading} paginator rows={15} emptyMessage='Busque por nome ou código.' responsiveLayout='scroll'>
                <Column field='compe' header='COMPE' style={{ width: '7rem' }} />
                <Column field='nomeCurto' header='Nome' />
                <Column field='nome' header='Razão' />
                <Column field='tipo' header='Tipo' style={{ width: '9rem' }} />
                <Column header='PIX' body={(r) => <Tag value={r.aceitaPix ? 'Sim' : 'Não'} severity={r.aceitaPix ? 'success' : 'secondary'} />} style={{ width: '6rem' }} />
            </DataTable>
        </div>
    );
};
export default Bancos;
