import React, { useState, useEffect, useRef, useCallback } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { InputNumber } from 'primereact/inputnumber';
import { InputTextarea } from 'primereact/inputtextarea';
import { TabView, TabPanel } from 'primereact/tabview';
import { Toast } from 'primereact/toast';
import { formatoData } from '../shared/LocaleData.js';

const BASE = '/api/financeiro/renegociacoes';
const fmt = (v) => Number(v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export const Renegociacao = () => {
    const toast = useRef(null);
    const [tits, setTits] = useState([]);
    const [hist, setHist] = useState([]);
    const [loading, setLoading] = useState(true);
    const [dlg, setDlg] = useState(false);
    const [sel, setSel] = useState(null);
    const [novoVenc, setNovoVenc] = useState(null);
    const [acrescimo, setAcrescimo] = useState(0);
    const [obs, setObs] = useState('');
    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? j ?? []); };
    const carregar = useCallback(async () => {
        setLoading(true);
        try {
            const [t, h] = await Promise.all([apiFetch('/api/financeiro/titulos').then(js), apiFetch(BASE).then(js)]);
            setTits(t.filter((x) => x.status === 'ABERTO' || x.status === 'PARCIAL'));
            setHist(h);
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha ao carregar', life: 4000 }); }
        finally { setLoading(false); }
    }, []);
    useEffect(() => { carregar(); }, [carregar]);
    const abrir = (t) => { setSel(t); setNovoVenc(null); setAcrescimo(0); setObs(''); setDlg(true); };
    const confirmar = async () => {
        if (!novoVenc) { toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Informe o novo vencimento', life: 3000 }); return; }
        const r = await apiFetch(BASE + '/renegociar', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ tituloId: sel.id, novoVencimento: novoVenc.toISOString().slice(0, 10), acrescimo: Number(acrescimo || 0), observacao: obs }) });
        if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Não foi possível renegociar', life: 4000 }); return; }
        toast.current?.show({ severity: 'success', summary: 'Renegociado', life: 3000 });
        setDlg(false); carregar();
    };
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='mb-3'><h2 className='m-0'>Renegociação</h2><span className='bc-muted'>Gera novo título (saldo + acréscimo) e baixa o original como RENEGOCIADO</span></div>
            <TabView>
                <TabPanel header='Títulos em aberto'>
                    <DataTable value={tits} loading={loading} paginator rows={10} emptyMessage='Nada em aberto.' responsiveLayout='scroll' dataKey='id'>
                        <Column field='descricao' header='Descrição' />
                        <Column field='dataVencimento' header='Vencimento' style={{ width: '8rem' }} />
                        <Column header='Saldo' body={(r) => fmt(r.valorSaldo)} style={{ width: '9rem' }} />
                        <Column header='' body={(r) => (<Button label='Renegociar' size='small' outlined onClick={() => abrir(r)} />)} style={{ width: '9rem' }} />
                    </DataTable>
                </TabPanel>
                <TabPanel header='Histórico'>
                    <DataTable value={hist} loading={loading} paginator rows={10} emptyMessage='Sem renegociações.' responsiveLayout='scroll' dataKey='id'>
                        <Column field='tituloOriginalId' header='Título original' />
                        <Column field='novoTituloId' header='Novo título' />
                        <Column field='dataRenegociacao' header='Data' />
                        <Column header='Acréscimo' body={(r) => fmt(r.valorAcrescimo)} />
                    </DataTable>
                </TabPanel>
            </TabView>
            <Dialog visible={dlg} onHide={() => setDlg(false)} header='Renegociar título' modal style={{ width: 'min(96vw, 480px)' }}>
                {sel && (<p><strong>{sel.descricao}</strong> — saldo {fmt(sel.valorSaldo)}</p>)}
                <div className='grid p-fluid'>
                    <div className='bc-form-col-6'><label className='bc-label'>Novo vencimento *</label><Calendar value={novoVenc} onChange={(e) => setNovoVenc(e.value)} dateFormat={formatoData()} showIcon /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Acréscimo</label><InputNumber value={acrescimo} onValueChange={(e) => setAcrescimo(e.value)} mode='currency' currency='BRL' locale='pt-BR' /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Observação</label><InputTextarea rows={2} value={obs} onChange={(e) => setObs(e.target.value)} /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlg(false)} /><Button label='Confirmar' icon='pi pi-check' severity='success' onClick={confirmar} /></div>
            </Dialog>
        </div>
    );
};
export default Renegociacao;
