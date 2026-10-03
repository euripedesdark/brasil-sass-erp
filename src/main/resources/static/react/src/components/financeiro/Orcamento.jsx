import React, { useState, useEffect, useRef, useCallback } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { TabView, TabPanel } from 'primereact/tabview';
import { Toast } from 'primereact/toast';

const BASE = '/api/financeiro/orcamentos';
const fmt = (v) => Number(v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export const Orcamento = () => {
    const toast = useRef(null);
    const [rows, setRows] = useState([]);
    const [contas, setContas] = useState([]);
    const [loading, setLoading] = useState(true);
    const [ano, setAno] = useState(new Date().getFullYear());
    const [dlg, setDlg] = useState(false);
    const [dlgReal, setDlgReal] = useState(false);
    const [sel, setSel] = useState(null);
    const [f, setF] = useState({});
    const [mes, setMes] = useState(new Date().getMonth() + 1);
    const [valor, setValor] = useState(0);
    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? j ?? []); };
    const carregar = useCallback(async () => {
        setLoading(true);
        try {
            const [a, c] = await Promise.all([apiFetch(BASE + '/acompanhamento?ano=' + ano).then(js), apiFetch('/api/financeiro/plano-contas').then(js).catch(() => [])]);
            setRows(a); setContas(c);
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha ao carregar', life: 4000 }); }
        finally { setLoading(false); }
    }, [ano]);
    useEffect(() => { carregar(); }, [carregar]);
    const contaLabel = (id) => { const c = contas.find((x) => x.id === id); return c ? ((c.codigo || "") + ' - ' + (c.descricao || c.nome || "")) : ('Geral'); };
    const salvar = async () => {
        await apiFetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ano, planoContasId: f.contaId || null, centroCustoId: null, valorOrcado: Number(f.valor || 0) }) });
        setDlg(false); setF({}); carregar();
    };
    const lancar = async () => {
        await apiFetch(BASE + '/' + sel.orcamentoId + '/realizado', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ mes: Number(mes), valor: Number(valor || 0) }) });
        setDlgReal(false); carregar();
    };
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='flex justify-content-between align-items-center mb-3 flex-wrap gap-2'>
                <div><h2 className='m-0'>Orçado × Realizado</h2><span className='bc-muted'>Orçamento anual por conta com desvio mensal</span></div>
                <div className='flex gap-2'><InputNumber value={ano} onValueChange={(e) => setAno(e.value)} useGrouping={false} /><Button label='Novo orçamento' icon='pi pi-plus' onClick={() => { setF({}); setDlg(true); }} /></div>
            </div>
            <DataTable value={rows} loading={loading} paginator rows={12} emptyMessage='Sem orçamentos no ano.' responsiveLayout='scroll'>
                <Column header='Conta' body={(r) => contaLabel(r.contaId)} />
                <Column header='Orçado' body={(r) => fmt(r.orcado)} style={{ width: '10rem' }} />
                <Column header='Realizado' body={(r) => fmt(r.realizado)} style={{ width: '10rem' }} />
                <Column header='Desvio' body={(r) => <span style={{ color: Number(r.desvio) < 0 ? 'var(--red-500)' : 'inherit', fontWeight: 'bold' }}>{fmt(r.desvio)}</span>} style={{ width: '10rem' }} />
                <Column header='%' body={(r) => Number(r.percentual) + '%'} style={{ width: '6rem' }} />
                <Column header='' body={(r) => (<Button label='Realizado' size='small' outlined onClick={() => { setSel(r); setDlgReal(true); }} />)} style={{ width: '8rem' }} />
            </DataTable>
            <Dialog visible={dlg} onHide={() => setDlg(false)} header='Novo orçamento' modal style={{ width: 'min(96vw, 480px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-12'><label className='bc-label'>Conta</label><Dropdown value={f.contaId} options={contas.map((c) => ({ label: (c.codigo || '') + ' - ' + (c.descricao || c.nome || ''), value: c.id }))} onChange={(e) => setF({ ...f, contaId: e.value })} filter placeholder='Geral' showClear /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Valor orçado *</label><InputNumber value={f.valor} onValueChange={(e) => setF({ ...f, valor: e.value })} mode='currency' currency='BRL' locale='pt-BR' /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlg(false)} /><Button label='Salvar' icon='pi pi-check' onClick={salvar} /></div>
            </Dialog>
            <Dialog visible={dlgReal} onHide={() => setDlgReal(false)} header='Lançar realizado' modal style={{ width: 'min(96vw, 420px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-6'><label className='bc-label'>Mês</label><InputNumber value={mes} onValueChange={(e) => setMes(e.value)} min={1} max={12} useGrouping={false} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Valor *</label><InputNumber value={valor} onValueChange={(e) => setValor(e.value)} mode='currency' currency='BRL' locale='pt-BR' /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlgReal(false)} /><Button label='Lançar' icon='pi pi-check' onClick={lancar} /></div>
            </Dialog>
        </div>
    );
};
export default Orcamento;
