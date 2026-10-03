import React, { useState, useRef } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { Toast } from 'primereact/toast';

export const BuscaFiscal = () => {
    const toast = useRef(null);
    const [termo, setTermo] = useState('');
    const [tabela, setTabela] = useState(null);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(false);
    const buscar = async () => {
        if (!termo.trim()) return;
        setLoading(true);
        try {
            let url = '/api/fiscal/busca?q=' + encodeURIComponent(termo.trim()) + '&limite=50';
            if (tabela) url += '&tabela=' + tabela;
            const r = await apiFetch(url);
            const j = await r.json().catch(() => []);
            setRows(Array.isArray(j) ? j : (j?.data ?? j?.lista ?? []));
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha na busca', life: 4000 }); }
        finally { setLoading(false); }
    };
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='mb-3'><h2 className='m-0'>Busca Fiscal</h2><span className='bc-muted'>NCM, CEST e CFOP por código, descrição ou palavra-chave</span></div>
            <div className='flex gap-2 mb-3 flex-wrap'>
                <InputText value={termo} onChange={(e) => setTermo(e.target.value)} placeholder='Ex: 8471, camiseta, SPED...' style={{ flex: 1, minWidth: '16rem' }} onKeyDown={(e) => e.key === 'Enter' && buscar()} />
                <Dropdown value={tabela} options={['NCM','CEST','CFOP'].map((t) => ({ label: t, value: t }))} onChange={(e) => setTabela(e.value)} placeholder='Todas' showClear />
                <Button label='Buscar' icon='pi pi-search' onClick={buscar} loading={loading} />
            </div>
            <DataTable value={rows} loading={loading} paginator rows={15} emptyMessage='Digite e busque.' responsiveLayout='scroll'>
                <Column field='codigo' header='Código' style={{ width: '10rem' }} />
                <Column field='descricao' header='Descrição' />
                <Column field='tabela' header='Tabela' style={{ width: '8rem' }} />
            </DataTable>
        </div>
    );
};
export default BuscaFiscal;
