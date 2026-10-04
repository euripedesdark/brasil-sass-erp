import React, { useState, useEffect, useRef } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/rh/ferias';
export const Ferias = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [funcs, setFuncs] = useState([]);
    const [folhas, setFolhas] = useState([]);
    const [loading, setLoading] = useState(true);
    const [dlg, setDlg] = useState(false);
    const [dlgGozar, setDlgGozar] = useState(false);
    const [sel, setSel] = useState(null);
    const [fFunc, setFFunc] = useState(null);
    const [fIni, setFIni] = useState(null);
    const [fDias, setFDias] = useState(30);
    const [fFolha, setFFolha] = useState(null);
    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? []); };
    const carregar = async () => {
        setLoading(true);
        try {
            setRows(await apiFetch(BASE).then(js));
            setFuncs(await apiFetch('/api/rh/funcionarios').then(js));
            setFolhas(await apiFetch('/api/rh/folhas').then(js));
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', life: 3000 }); }
        finally { setLoading(false); }
    };
    useEffect(() => { carregar(); }, []);
    const nomeFunc = (id) => (funcs.find((f) => f.id === id)?.pessoa?.nome || funcs.find((f) => f.id === id)?.nome || ('#' + id));
    const programar = async () => {
        if (!fFunc || !fIni) return;
        const ini = fIni.toISOString().slice(0, 10);
        try { await apiFetch(BASE + '?funcionarioId=' + fFunc + '&inicio=' + ini + '&dias=' + (fDias || 30), { method: 'POST' }); setDlg(false); carregar(); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', life: 4000 }); }
    };
    const gozar = async () => {
        if (!sel || !fFolha) return;
        try { await apiFetch(BASE + '/' + sel.id + '/gozar?folhaId=' + fFolha, { method: 'POST' }); setDlgGozar(false); carregar(); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', life: 4000 }); }
    };
    const cancelar = async (r) => {
        try { await apiFetch(BASE + '/' + r.id + '/cancelar', { method: 'POST' }); carregar(); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', life: 3000 }); }
    };
    const sev = (s) => (s === 'GOZADA' ? 'success' : s === 'CANCELADA' ? 'danger' : 'warning');
    const botoes = (r) => (
        <span className='flex gap-1'>
            {r.status === 'PROGRAMADA' && <><Button icon='pi pi-check' tooltip='Gozar (gera itens na folha)' size='small' onClick={() => { setSel(r); setFFolha(null); setDlgGozar(true); }} /><Button icon='pi pi-times' tooltip='Cancelar' size='small' severity='secondary' onClick={() => cancelar(r)} /></>}
        </span>
    );
    const abertas = folhas.filter((f) => f.status === 'ABERTA');
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='flex justify-content-between align-items-center mb-3'>
                <div><h2 className='m-0'>Ferias</h2><span className='bc-muted'>Programacao de 10 a 30 dias; ao gozar, gera ferias + 1/3 na folha aberta</span></div>
                <Button label='Programar' icon='pi pi-plus' onClick={() => setDlg(true)} />
            </div>
            <DataTable value={rows} loading={loading} paginator rows={15} emptyMessage='Nenhuma ferias programada.' responsiveLayout='scroll'>
                <Column header='Funcionario' body={(r) => nomeFunc(r.funcionarioId)} />
                <Column field='dataInicio' header='Inicio' style={{ width: '8rem' }} />
                <Column field='dataFim' header='Fim' style={{ width: '8rem' }} />
                <Column field='dias' header='Dias' style={{ width: '5rem' }} />
                <Column field='valorFerias' header='Ferias' />
                <Column field='valorTerco' header='1/3' />
                <Column field='status' header='Status' body={(r) => <Tag value={r.status} severity={sev(r.status)} />} />
                <Column header='Acoes' body={botoes} style={{ width: '7rem' }} />
            </DataTable>
            <Dialog header='Programar ferias' visible={dlg} onHide={() => setDlg(false)} style={{ width: '26rem' }}>
                <div className='flex flex-column gap-3'>
                    <span><label className='bc-label'>Funcionario</label><Dropdown value={fFunc} options={funcs.map((f) => ({ label: f.pessoa?.nome || f.nome || ('#' + f.id), value: f.id }))} onChange={(e) => setFFunc(e.value)} placeholder='Escolha' filter className='w-full' /></span>
                    <span><label className='bc-label'>Inicio</label><Calendar value={fIni} onChange={(e) => setFIni(e.value)} dateFormat='dd/mm/yy' showIcon className='w-full' /></span>
                    <span><label className='bc-label'>Dias (10 a 30)</label><InputNumber value={fDias} onValueChange={(e) => setFDias(e.value ?? 30)} min={10} max={30} className='w-full' /></span>
                    <Button label='Salvar' icon='pi pi-check' onClick={programar} disabled={!fFunc || !fIni} />
                </div>
            </Dialog>
            <Dialog header='Gozar ferias' visible={dlgGozar} onHide={() => setDlgGozar(false)} style={{ width: '26rem' }}>
                <div className='flex flex-column gap-3'>
                    <span><label className='bc-label'>Folha aberta (recebe ferias + 1/3)</label><Dropdown value={fFolha} options={abertas.map((f) => ({ label: f.competencia + ' (' + (f.valorTotal ?? 0) + ')', value: f.id }))} onChange={(e) => setFFolha(e.value)} placeholder='Escolha a folha' className='w-full' /></span>
                    <Button label='Confirmar gozo' icon='pi pi-check' onClick={gozar} disabled={!fFolha} />
                </div>
            </Dialog>
        </div>
    );
};
export default Ferias;
