import React, { useState, useEffect, useRef, useCallback } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/financeiro/emprestimos';
const fmt = (v) => Number(v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export const Emprestimos = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    const [dlg, setDlg] = useState(false);
    const [f, setF] = useState({});
    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? j ?? []); };
    const carregar = useCallback(async () => {
        setLoading(true);
        try { setRows(await apiFetch(BASE).then(js)); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha ao carregar', life: 4000 }); }
        finally { setLoading(false); }
    }, []);
    useEffect(() => { carregar(); }, [carregar]);
    const salvar = async () => {
        if (!f.instituicao?.trim() || !f.dataContratacao || !f.valorTotal) { toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Instituição, data e valor obrigatórios', life: 3000 }); return; }
        await apiFetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ instituicao: f.instituicao, valorTotal: Number(f.valorTotal), taxaJuros: Number(f.taxaJuros || 0), dataContratacao: f.dataContratacao.toISOString().slice(0, 10) }) });
        setDlg(false); setF({}); carregar();
    };
    const quitar = async (id) => { await apiFetch(BASE + '/' + id + '/quitar', { method: 'POST' }); carregar(); };
    const excluir = async (id) => { await apiFetch(BASE + '/' + id, { method: 'DELETE' }); carregar(); };
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='flex justify-content-between align-items-center mb-3 flex-wrap gap-2'>
                <div><h2 className='m-0'>Empréstimos</h2><span className='bc-muted'>Contratação, juros e quitação</span></div>
                <Button label='Novo empréstimo' icon='pi pi-plus' onClick={() => { setF({}); setDlg(true); }} />
            </div>
            <DataTable value={rows} loading={loading} paginator rows={10} emptyMessage='Nenhum empréstimo.' responsiveLayout='scroll' dataKey='id'>
                <Column field='instituicao' header='Instituição' />
                <Column header='Valor' body={(r) => fmt(r.valorTotal)} />
                <Column header='Juros %' body={(r) => Number(r.taxaJuros ?? 0) + '%'} style={{ width: '7rem' }} />
                <Column field='dataContratacao' header='Contratação' style={{ width: '8rem' }} />
                <Column header='Status' body={(r) => <Tag value={r.status} severity={r.status === 'QUITADO' ? 'success' : 'warning'} />} style={{ width: '7rem' }} />
                <Column header='' body={(r) => (<div className='flex gap-1'>
                    {r.status !== 'QUITADO' && <Button label='Quitar' size='small' severity='success' onClick={() => quitar(r.id)} />}
                    <Button icon='pi pi-trash' rounded text severity='danger' tooltip='Excluir' onClick={() => excluir(r.id)} />
                </div>)} style={{ width: '10rem' }} />
            </DataTable>
            <Dialog visible={dlg} onHide={() => setDlg(false)} header='Novo empréstimo' modal style={{ width: 'min(96vw, 500px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-12'><label className='bc-label'>Instituição *</label><InputText value={f.instituicao || ''} onChange={(e) => setF({ ...f, instituicao: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Valor *</label><InputNumber value={f.valorTotal} onValueChange={(e) => setF({ ...f, valorTotal: e.value })} mode='currency' currency='BRL' locale='pt-BR' /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Juros % a.m.</label><InputNumber value={f.taxaJuros} onValueChange={(e) => setF({ ...f, taxaJuros: e.value })} suffix=' %' minFractionDigits={2} /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Contratação *</label><Calendar value={f.dataContratacao} onChange={(e) => setF({ ...f, dataContratacao: e.target.value })} dateFormat='dd/mm/yy' showIcon /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlg(false)} /><Button label='Salvar' icon='pi pi-check' onClick={salvar} /></div>
            </Dialog>
        </div>
    );
};
export default Emprestimos;
