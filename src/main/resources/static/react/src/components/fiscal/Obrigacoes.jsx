import React, { useState, useEffect, useRef, useCallback } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/fiscal/obrigacoes';

export const Obrigacoes = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    const [comp, setComp] = useState(() => { const d = new Date(); return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0'); });
    const [dlg, setDlg] = useState(false);
    const [dlgEnt, setDlgEnt] = useState(false);
    const [f, setF] = useState({});
    const [sel, setSel] = useState(null);
    const [proto, setProto] = useState('');
    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? []); };
    const carregar = useCallback(async () => {
        setLoading(true);
        try { setRows(await apiFetch(BASE + '/agenda?competencia=' + comp).then(js)); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', life: 3000 }); }
        finally { setLoading(false); }
    }, [comp]);
    useEffect(() => { carregar(); }, [carregar]);
    const instalar = async () => { await apiFetch(BASE + '/instalar-modelo', { method: 'POST' }); carregar(); };
    const salvar = async () => {
        if (!f.nome?.trim()) return;
        await apiFetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ nome: f.nome, orgao: f.orgao, periodicidade: 'MENSAL', diaVencimento: Number(f.dia || 20), descricao: f.descricao }) });
        setDlg(false); setF({}); carregar();
    };
    const abrirEnt = (r) => { setSel(r); setProto(''); setDlgEnt(true); };
    const entregar = async () => {
        await apiFetch(BASE + '/' + sel.obrigacaoId + '/entregar', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ competencia: comp, protocolo: proto || null }) });
        setDlgEnt(false); carregar();
    };
    const sev = (s) => (s === 'ENTREGUE' ? 'success' : s === 'ATRASADA' ? 'danger' : 'warning');
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='flex justify-content-between align-items-center mb-3 flex-wrap gap-2'>
                <div><h2 className='m-0'>Obrigações</h2><span className='bc-muted'>Agenda mensal com protocolo</span></div>
                <div className='flex gap-2'><InputText value={comp} onChange={(e) => setComp(e.target.value)} placeholder='AAAA-MM' maxLength={7} style={{ width: '8rem' }} /><Button label='Modelo padrão' icon='pi pi-download' outlined onClick={instalar} /><Button label='Nova' icon='pi pi-plus' onClick={() => { setF({}); setDlg(true); }} /></div>
            </div>
            <DataTable value={rows} loading={loading} paginator rows={12} emptyMessage='Nada na competência. Instale o modelo.' responsiveLayout='scroll'>
                <Column field='nome' header='Obrigação' />
                <Column field='orgao' header='Órgão' style={{ width: '8rem' }} />
                <Column field='vencimento' header='Vence em' style={{ width: '8rem' }} />
                <Column header='Status' body={(r) => <Tag value={r.status} severity={sev(r.status)} />} style={{ width: '9rem' }} />
                <Column field='protocolo' header='Protocolo' />
                <Column header='' body={(r) => (r.status !== 'ENTREGUE' ? (<Button label='Entregue' size='small' severity='success' onClick={() => abrirEnt(r)} />) : null)} style={{ width: '8rem' }} />
            </DataTable>
            <Dialog visible={dlg} onHide={() => setDlg(false)} header='Nova obrigação' modal style={{ width: 'min(96vw, 480px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-12'><label className='bc-label'>Nome *</label><InputText value={f.nome || ''} onChange={(e) => setF({ ...f, nome: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Órgão</label><InputText value={f.orgao || ''} onChange={(e) => setF({ ...f, orgao: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Dia venc.</label><InputText value={f.dia || ''} onChange={(e) => setF({ ...f, dia: e.target.value })} keyfilter='int' /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlg(false)} /><Button label='Salvar' icon='pi pi-check' onClick={salvar} /></div>
            </Dialog>
            <Dialog visible={dlgEnt} onHide={() => setDlgEnt(false)} header='Dar baixa' modal style={{ width: 'min(96vw, 420px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-12'><label className='bc-label'>Protocolo (opcional)</label><InputText value={proto} onChange={(e) => setProto(e.target.value)} /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlgEnt(false)} /><Button label='Confirmar entrega' icon='pi pi-check' severity='success' onClick={entregar} /></div>
            </Dialog>
        </div>
    );
};
export default Obrigacoes;
