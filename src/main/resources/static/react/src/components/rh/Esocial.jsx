import React, { useState, useEffect, useRef, useCallback } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/rh/esocial-fila';
const TIPOS = ['S-2200','S-1200','S-1210','S-2299','S-3000'];

export const Esocial = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    const [dlg, setDlg] = useState(false);
    const [funcs, setFuncs] = useState([]);
    const [f, setF] = useState({ tipo: 'S-2200' });
    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? []); };
    const carregar = useCallback(async () => {
        setLoading(true);
        try { setRows(await apiFetch(BASE).then(js)); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', life: 3000 }); }
        finally { setLoading(false); }
    }, []);
    useEffect(() => { carregar(); apiFetch('/api/rh/funcionarios').then(js).then((l) => setFuncs(l)).catch(() => {}); }, [carregar]);
    const salvar = async () => {
        const r = await apiFetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ tipo: f.tipo, funcionarioId: f.funcionarioId ? Number(f.funcionarioId) : null }) });
        if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', life: 3000 }); return; }
        setDlg(false); setF({ tipo: 'S-2200' }); carregar();
    };
    const transmitir = async (id) => {
        const r = await apiFetch(BASE + '/' + id + '/transmitir', { method: 'POST' });
        const j = await r.json().catch(() => ({}));
        toast.current?.show({ severity: j.status === 'ENVIADO' ? 'success' : 'warn', summary: j.status || 'resposta', detail: (j.erro || j.recibo || '').slice(0, 200), life: 6000 });
        carregar();
    };
    const excluir = async (id) => { await apiFetch(BASE + '/' + id, { method: 'DELETE' }); carregar(); };
    const sev = (s) => (s === 'ENVIADO' ? 'success' : s === 'FALHA' ? 'danger' : 'warning');
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='mb-3 flex justify-content-between align-items-center flex-wrap gap-2'>
                <div><h2 className='m-0'>eSocial — fila de eventos</h2><span className='bc-muted'>Registra, transmite ao microserviço e guarda recibo/protocolo</span></div>
                <Button label='Novo evento' icon='pi pi-plus' onClick={() => setDlg(true)} />
            </div>
            <DataTable value={rows} loading={loading} paginator rows={12} emptyMessage='Fila vazia.' responsiveLayout='scroll' dataKey='id'>
                <Column field='tipo' header='Evento' style={{ width: '8rem' }} />
                <Column field='funcionarioId' header='Func.' style={{ width: '6rem' }} />
                <Column header='Status' body={(r) => <Tag value={r.status} severity={sev(r.status)} />} style={{ width: '8rem' }} />
                <Column field='recibo' header='Recibo' />
                <Column field='erro' header='Erro' />
                <Column header='' body={(r) => (<div className='flex gap-1'>{(r.status === 'PENDENTE' || r.status === 'FALHA') && (<Button label='Transmitir' size='small' icon='pi pi-send' onClick={() => transmitir(r.id)} />)}{r.status !== 'ENVIADO' && (<Button icon='pi pi-trash' rounded text severity='danger' onClick={() => excluir(r.id)} />)}</div>)} style={{ width: '12rem' }} />
            </DataTable>
            <Dialog visible={dlg} onHide={() => setDlg(false)} header='Novo evento' modal style={{ width: 'min(96vw, 440px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-6'><label className='bc-label'>Evento</label><Dropdown value={f.tipo} options={TIPOS.map((t) => ({ label: t, value: t }))} onChange={(e) => setF({ ...f, tipo: e.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Funcionário</label><Dropdown value={f.funcionarioId} options={funcs.map((x) => ({ label: x.nome || x.id, value: x.id }))} onChange={(e) => setF({ ...f, funcionarioId: e.value })} filter placeholder='Selecione' showClear /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlg(false)} /><Button label='Registrar' icon='pi pi-check' onClick={salvar} /></div>
            </Dialog>
        </div>
    );
};
export default Esocial;
