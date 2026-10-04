import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const fmt = (v) => Number(v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
const hojeISO = () => new Date().toISOString().slice(0, 10);

export const Cobranca = () => {
    const toast = useRef(null);
    const navigate = useNavigate();
    const [rows, setRows] = useState([]);
    const [sel, setSel] = useState([]);
    const [contas, setContas] = useState([]);
    const [tipos, setTipos] = useState([]);
    const [loading, setLoading] = useState(true);
    const [dlgBaixa, setDlgBaixa] = useState(false);
    const [alvo, setAlvo] = useState(null);
    const [fBx, setFBx] = useState({});
    const [dlgRem, setDlgRem] = useState(false);
    const [bank, setBank] = useState('itau');
    const [contaId, setContaId] = useState(null);
    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? j?.lista ?? []); };
    const carregar = useCallback(async () => {
        setLoading(true);
        try {
            const [t, c, tp] = await Promise.all([apiFetch('/api/financeiro/titulos').then(js), apiFetch('/api/financeiro/contas-bancarias').then(js).catch(() => []), apiFetch('/api/financeiro/tipos-pagamento').then(js).catch(() => [])]);
            setRows(t.filter((x) => x.status === 'ABERTO' || x.status === 'PARCIAL'));
            setContas(c); setTipos(tp);
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha ao carregar carteira', life: 4000 }); }
        finally { setLoading(false); }
    }, []);
    useEffect(() => { carregar(); }, [carregar]);
    const diasAtraso = (v) => v ? Math.floor((Date.now() - new Date(v + 'T00:00:00').getTime()) / 86400000) : 0;
    const vencidos = rows.filter((r) => diasAtraso(r.dataVencimento) > 0);
    const totVenc = vencidos.reduce((s, r) => s + Number(r.valorSaldo || 0), 0);
    const totAberto = rows.reduce((s, r) => s + Number(r.valorSaldo || 0), 0);
    const abrirBaixa = (r) => { setAlvo(r); setFBx({ valor: Number(r.valorSaldo || 0), data: new Date(), conta: null, tipo: null }); setDlgBaixa(true); };
    const confirmarBaixa = async () => {
        const r = await apiFetch('/api/financeiro/titulos/' + alvo.id + '/baixar', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ valorBaixa: Number(fBx.valor || 0), dataBaixa: fBx.data ? fBx.data.toISOString().slice(0, 10) : hojeISO(), contaBancariaId: fBx.conta, tipoPagamentoId: fBx.tipo }) });
        if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Baixa recusada', life: 4000 }); return; }
        setDlgBaixa(false); carregar();
    };
    const emitirBoleto = async (r) => {
        const resp = await apiFetch('/api/financeiro/boletos/emitir?bank=' + bank + '&tituloId=' + r.id, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ valor: Number(r.valorSaldo || 0), vencimento: r.dataVencimento, descricao: r.descricao }) });
        if (!resp.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Boleto recusado (confira banco/dados)', life: 5000 }); return; }
        toast.current?.show({ severity: 'success', summary: 'Boleto emitido', life: 3000 });
    };
    const gerarRemessa = async () => {
        if (!sel.length || !contaId) { toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Selecione títulos e conta', life: 3500 }); return; }
        const itens = sel.map((r) => ({ tituloId: r.id, valor: Number(r.valorSaldo || 0), vencimento: r.dataVencimento }));
        const r = await apiFetch('/api/financeiro/boletos/remessas?bank=' + bank + '&contaBancariaId=' + contaId, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(itens) });
        if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Remessa recusada', life: 4000 }); return; }
        toast.current?.show({ severity: 'success', summary: 'Remessa gerada', life: 3000 });
        setSel([]); setDlgRem(false);
    };
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='mb-3'><h2 className='m-0'>Cobrança</h2><span className='bc-muted'>Carteira em aberto, baixa, boleto e remessa</span></div>
            <div className='grid mb-3'>
                <div className='bc-form-col-4 col-12 md:col-4'><Card><small>Vencidos</small><div className='text-2xl font-bold' style={{ color: 'var(--red-500)' }}>{vencidos.length} · {fmt(totVenc)}</div></Card></div>
                <div className='bc-form-col-4 col-12 md:col-4'><Card><small>Em aberto</small><div className='text-2xl font-bold'>{rows.length} · {fmt(totAberto)}</div></Card></div>
                <div className='bc-form-col-4 col-12 md:col-4'><Card><small>Ações em lote</small><div className='flex gap-2 mt-2'><Dropdown value={contaId} options={contas.map((c) => ({ label: (c.banco || '') + ' ' + (c.agencia || '') + '/' + (c.conta || ''), value: c.id }))} onChange={(e) => setContaId(e.value)} placeholder='Conta' /><InputText value={bank} onChange={(e) => setBank(e.target.value)} placeholder='Banco' style={{ width: '7rem' }} /><Button label='Remessa' icon='pi pi-send' outlined onClick={() => setDlgRem(true)} /></div></Card></div>
            </div>
            <DataTable value={rows} loading={loading} paginator rows={12} emptyMessage='Carteira zerada.' responsiveLayout='scroll' dataKey='id' selectionMode='checkbox' selection={sel} onSelectionChange={(e) => setSel(e.value)}> 
                <Column selectionMode='multiple' style={{ width: '3rem' }} />
                <Column field='descricao' header='Título' />
                <Column field='dataVencimento' header='Venc.' style={{ width: '7rem' }} />
                <Column header='Atraso' body={(r) => { const d = diasAtraso(r.dataVencimento); return d > 0 ? (<Tag value={d + 'd'} severity='danger' />) : (<Tag value='em dia' severity='success' />); }} style={{ width: '6rem' }} />
                <Column header='Saldo' body={(r) => fmt(r.valorSaldo)} style={{ width: '9rem' }} />
                <Column header='' body={(r) => (<div className='flex gap-1'>
                    <Button label='Baixar' size='small' onClick={() => abrirBaixa(r)} />
                    <Button label='Boleto' size='small' outlined onClick={() => emitirBoleto(r)} />
                    <Button label='Reneg.' size='small' text onClick={() => navigate('/financeiro/renegociacao')} />
                </div>)} style={{ width: '14rem' }} />
            </DataTable>
            <Dialog visible={dlgRem} onHide={() => setDlgRem(false)} header='Gerar remessa' modal>
                <p>Enviar {sel.length} título(s) em remessa CNAB ({bank})?</p>
                <div className='flex justify-end gap-2'><Button label='Cancelar' text severity='secondary' onClick={() => setDlgRem(false)} /><Button label='Gerar' icon='pi pi-check' onClick={gerarRemessa} /></div>
            </Dialog>
            <Dialog visible={dlgBaixa} onHide={() => setDlgBaixa(false)} header='Baixar título' modal style={{ width: 'min(96vw, 480px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-6'><label className='bc-label'>Valor *</label><InputNumber value={fBx.valor} onValueChange={(e) => setFBx({ ...fBx, valor: e.value })} mode='currency' currency='BRL' locale='pt-BR' /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Data</label><Calendar value={fBx.data} onChange={(e) => setFBx({ ...fBx, data: e.value })} dateFormat='dd/mm/yy' showIcon /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Conta</label><Dropdown value={fBx.conta} options={contas.map((c) => ({ label: (c.banco || '') + ' ' + (c.agencia || '') + '/' + (c.conta || ''), value: c.id }))} onChange={(e) => setFBx({ ...fBx, conta: e.value })} placeholder='Conta' /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Tipo pgto</label><Dropdown value={fBx.tipo} options={tipos.map((t) => ({ label: t.descricao || t.nome || t.id, value: t.id }))} onChange={(e) => setFBx({ ...fBx, tipo: e.value })} placeholder='Tipo' /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlgBaixa(false)} /><Button label='Baixar' icon='pi pi-check' severity='success' onClick={confirmarBaixa} /></div>
            </Dialog>
        </div>
    );
};
export default Cobranca;
