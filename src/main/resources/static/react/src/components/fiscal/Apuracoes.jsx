import React, { useState, useEffect, useRef } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/fiscal/apuracoes';
export const Apuracoes = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    const [impostos, setImpostos] = useState([]);
    const [dlg, setDlg] = useState(false);
    const [impId, setImpId] = useState(null);
    const [comp, setComp] = useState(() => { const d = new Date(); return String(d.getMonth() + 1).padStart(2, '0') + '/' + d.getFullYear(); });
    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? j); };
    const carregar = async () => {
        setLoading(true);
        try {
            const a = await apiFetch(BASE).then(js);
            setRows(Array.isArray(a) ? a : []);
            const im = await apiFetch('/api/fiscal/impostos').then(js);
            setImpostos(Array.isArray(im) ? im : []);
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', life: 3000 }); }
        finally { setLoading(false); }
    };
    useEffect(() => { carregar(); }, []);
    const nomeImp = (id) => (impostos.find((i) => i.id === id)?.sigla || ('#' + id));
    const acao = async (id, op) => {
        try { await apiFetch(BASE + '/' + id + '/' + op, { method: 'POST' }); carregar(); }
        catch (e) { const j = await e.json?.().catch(() => null); toast.current?.show({ severity: 'error', summary: 'Erro', detail: j?.message || 'Falhou', life: 4000 }); }
    };
    const calcular = async () => {
        if (!impId || !comp) return;
        try { await apiFetch(BASE + '/calcular?impostoId=' + impId + '&competencia=' + encodeURIComponent(comp), { method: 'POST' }); setDlg(false); carregar(); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', life: 4000 }); }
    };
    const sev = (s) => (s === 'TRANSMITIDA' ? 'success' : s === 'ENCERRADA' ? 'info' : 'warning');
    const botoes = (r) => (
        <span className='flex gap-1'>
            {r.status === 'ABERTA' && <Button icon='pi pi-lock' tooltip='Encerrar' size='small' onClick={() => acao(r.id, 'encerrar')} />}
            {r.status === 'ENCERRADA' && <><Button icon='pi pi-undo' tooltip='Reabrir' size='small' severity='secondary' onClick={() => acao(r.id, 'reabrir')} /><Button icon='pi pi-send' tooltip='Transmitir' size='small' onClick={() => acao(r.id, 'transmitir')} /></>}
        </span>
    );
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='flex justify-content-between align-items-center mb-3'>
                <div><h2 className='m-0'>Apuracao de impostos</h2><span className='bc-muted'>Devido x credito x pagar por competencia (ICMS/IPI de NFe; PIS/COFINS/IRPJ/CSLL pela aliquota; ISS de NFSe)</span></div>
                <Button label='Apurar' icon='pi pi-calculator' onClick={() => setDlg(true)} />
            </div>
            <DataTable value={rows} loading={loading} paginator rows={15} emptyMessage='Nenhuma apuracao. Clique em Apurar.' responsiveLayout='scroll'>
                <Column field='competencia' header='Competencia' style={{ width: '8rem' }} />
                <Column header='Imposto' body={(r) => nomeImp(r.impostoId)} />
                <Column field='baseCalculo' header='Base' />
                <Column field='valorDevido' header='Devido' />
                <Column field='valorCredito' header='Credito' />
                <Column field='valorPagar' header='A pagar' />
                <Column field='status' header='Status' body={(r) => <Tag value={r.status} severity={sev(r.status)} />} />
                <Column header='Acoes' body={botoes} style={{ width: '8rem' }} />
            </DataTable>
            <Dialog header='Apurar competencia' visible={dlg} onHide={() => setDlg(false)} style={{ width: '26rem' }}>
                <div className='flex flex-column gap-3'>
                    <span><label className='bc-label'>Imposto</label><Dropdown value={impId} options={impostos.map((i) => ({ label: i.sigla + ' - ' + (i.nome || ''), value: i.id }))} onChange={(e) => setImpId(e.value)} placeholder='Escolha' filter className='w-full' /></span>
                    <span><label className='bc-label'>Competencia (MM/AAAA)</label><InputText value={comp} onChange={(e) => setComp(e.target.value)} placeholder='MM/AAAA' className='w-full' /></span>
                    <Button label='Calcular' icon='pi pi-check' onClick={calcular} disabled={!impId || !comp} />
                </div>
            </Dialog>
        </div>
    );
};
export default Apuracoes;
