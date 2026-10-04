import React, { useState, useEffect, useRef } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dropdown } from 'primereact/dropdown';
import { Dialog } from 'primereact/dialog';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/rh/ponto';

export const Ponto = () => {
    const toast = useRef(null);
    const [funcs, setFuncs] = useState([]);
    const [funcId, setFuncId] = useState(null);
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(false);
    const [ano, setAno] = useState(new Date().getFullYear());
    const [mes, setMes] = useState(new Date().getMonth() + 1);
    const [dlgAj, setDlgAj] = useState(false);
    const [aj, setAj] = useState({ id: null, data: '', e1: '', s1: '', e2: '', s2: '', observacao: '' });
    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? j ?? []); };
    useEffect(() => { apiFetch('/api/rh/funcionarios').then(js).then((l) => setFuncs(l)).catch(() => {}); }, []);
    const ver = async (id) => {
        const fid = id ?? funcId;
        if (!fid) return;
        setLoading(true);
        try { setRows(await apiFetch(BASE + '?funcionarioId=' + fid + '&ano=' + ano + '&mes=' + mes).then(js)); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha ao carregar espelho', life: 4000 }); }
        finally { setLoading(false); }
    };
    const bater = async () => {
        if (!funcId) { toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Selecione o funcionário', life: 3000 }); return; }
        const r = await apiFetch(BASE + '/bater?funcionarioId=' + funcId, { method: 'POST' });
        if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Dia já fechado ou funcionário inativo', life: 4000 }); return; }
        ver(funcId);
    };
    const [dlgFolha, setDlgFolha] = useState(false);
    const [folhas, setFolhas] = useState([]);
    const [folhaId, setFolhaId] = useState(null);
    const abrirEnvio = async () => {
        if (!funcId) return;
        const r = await apiFetch('/api/rh/folhas').then(js).catch(() => []);
        setFolhas(r.filter((f) => f.status === 'ABERTA'));
        setFolhaId(null);
        setDlgFolha(true);
    };
    const enviarFolha = async () => {
        if (!folhaId) return;
        const r = await apiFetch('/api/rh/folhas/' + folhaId + '/importar-ponto', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ funcionarioId: funcId, ano, mes }) });
        if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Importação recusada', life: 4000 }); return; }
        toast.current?.show({ severity: 'success', summary: 'Horas lançadas na folha', life: 3000 });
        setDlgFolha(false);
    };
    const falta = async () => {
        if (!funcId) return;
        await apiFetch(BASE + '/falta?funcionarioId=' + funcId, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({}) });
        ver(funcId);
    };
    const hhmm = (v) => (v ? String(v).slice(0, 5) : '');
    const abrirAjuste = (r) => {
        setAj({ id: r.id, data: r.data, e1: hhmm(r.e1), s1: hhmm(r.s1), e2: hhmm(r.e2), s2: hhmm(r.s2), observacao: '' });
        setDlgAj(true);
    };
    const salvarAjuste = async () => {
        const ok = (v) => v === '' || /^([01]\d|2[0-3]):[0-5]\d$/.test(v);
        if (![aj.e1, aj.s1, aj.e2, aj.s2].every(ok)) {
            toast.current?.show({ severity: 'warn', summary: 'Horário inválido', detail: 'Use o formato HH:mm (ex.: 08:00)', life: 3500 });
            return;
        }
        if (!aj.observacao.trim()) {
            toast.current?.show({ severity: 'warn', summary: 'Justificativa', detail: 'Informe o motivo do ajuste', life: 3500 });
            return;
        }
        const nulo = (v) => (v === '' ? null : v);
        const r = await apiFetch(BASE + '/' + aj.id + '/ajustar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ e1: nulo(aj.e1), s1: nulo(aj.s1), e2: nulo(aj.e2), s2: nulo(aj.s2), observacao: aj.observacao.trim() })
        });
        if (!r.ok) {
            const j = await r.json().catch(() => null);
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: j?.message || j?.errors?.[0]?.message || 'Ajuste recusado (dia fechado na folha?)', life: 4500 });
            return;
        }
        toast.current?.show({ severity: 'success', summary: 'Ponto ajustado', life: 2500 });
        setDlgAj(false);
        ver(funcId);
    };
    const totHoras = rows.filter((r) => !r.falta).reduce((s, r) => s + Number(r.horasTrabalhadas || 0), 0);
    const faltas = rows.filter((r) => r.falta).length;
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='mb-3'><h2 className='m-0'>Ponto</h2><span className='bc-muted'>Batidas e espelho mensal</span></div>
            <div className='flex gap-2 mb-3 flex-wrap align-items-end'>
                <span><label className='bc-label'>Funcionário</label><Dropdown value={funcId} options={funcs.map((f) => ({ label: (f.nome || f.id), value: f.id }))} onChange={(e) => { setFuncId(e.value); }} placeholder='Selecione' filter style={{ minWidth: '16rem' }} /></span>
                <span><label className='bc-label'>Ano</label><InputNumber value={ano} onValueChange={(e) => setAno(e.value)} useGrouping={false} /></span>
                <span><label className='bc-label'>Mês</label><InputNumber value={mes} onValueChange={(e) => setMes(e.value)} min={1} max={12} useGrouping={false} /></span>
                <Button label='Ver espelho' icon='pi pi-search' onClick={() => ver()} />
                <Button label='Bater ponto' icon='pi pi-clock' severity='success' onClick={bater} />
                <Button label='Falta' icon='pi pi-ban' severity='danger' outlined onClick={falta} />
            </div>
                <Button label='Enviar p/ folha' icon='pi pi-send' severity='help' outlined onClick={abrirEnvio} />
            <div className='grid mb-3'>
                <div className='bc-form-col-6 col-12 md:col-6'><Card><small>Horas no mês</small><div className='text-2xl font-bold'>{totHoras.toFixed(2)}h</div></Card></div>
                <div className='bc-form-col-6 col-12 md:col-6'><Card><small>Faltas</small><div className='text-2xl font-bold'>{faltas}</div></Card></div>
            </div>
            <Dialog visible={dlgFolha} onHide={() => setDlgFolha(false)} header='Enviar horas para folha' modal style={{ width: 'min(96vw, 440px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-12'><label className='bc-label'>Folha aberta</label><Dropdown value={folhaId} options={folhas.map((f) => ({ label: (f.competencia || f.id) + ' (' + (f.status || '') + ')', value: f.id }))} onChange={(e) => setFolhaId(e.value)} placeholder='Selecione' /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlgFolha(false)} /><Button label='Enviar' icon='pi pi-check' onClick={enviarFolha} /></div>
            </Dialog>
            <Dialog visible={dlgAj} onHide={() => setDlgAj(false)} header={'Ajustar ponto — ' + (aj.data || '')} modal style={{ width: 'min(96vw, 460px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-6'><label className='bc-label'>Entrada 1</label><InputText placeholder='HH:mm' maxLength={5} value={aj.e1} onChange={(e) => setAj({ ...aj, e1: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Saída 1</label><InputText placeholder='HH:mm' maxLength={5} value={aj.s1} onChange={(e) => setAj({ ...aj, s1: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Entrada 2</label><InputText placeholder='HH:mm' maxLength={5} value={aj.e2} onChange={(e) => setAj({ ...aj, e2: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Saída 2</label><InputText placeholder='HH:mm' maxLength={5} value={aj.s2} onChange={(e) => setAj({ ...aj, s2: e.target.value })} /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Justificativa *</label><InputText value={aj.observacao} onChange={(e) => setAj({ ...aj, observacao: e.target.value })} /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'>
                    <Button label='Cancelar' text severity='secondary' onClick={() => setDlgAj(false)} />
                    <Button label='Salvar ajuste' icon='pi pi-check' onClick={salvarAjuste} />
                </div>
            </Dialog>
            <DataTable value={rows} loading={loading} paginator rows={15} emptyMessage='Sem registros.' responsiveLayout='scroll' dataKey='id'>
                <Column field='data' header='Data' style={{ width: '8rem' }} />
                <Column field='e1' header='E1' /><Column field='s1' header='S1' /><Column field='e2' header='E2' /><Column field='s2' header='S2' />
                <Column field='horasTrabalhadas' header='Horas' style={{ width: '6rem' }} />
                <Column header='Falta' body={(r) => (r.falta ? <Tag value='Falta' severity='danger' /> : null)} style={{ width: '6rem' }} />
                <Column header='' body={(r) => (<Button icon='pi pi-pencil' rounded text tooltip='Ajustar batidas' onClick={() => abrirAjuste(r)} />)} style={{ width: '4rem' }} />
            </DataTable>
        </div>
    );
};
export default Ponto;
