import React, { useState, useEffect, useRef } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
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
    const falta = async () => {
        if (!funcId) return;
        await apiFetch(BASE + '/falta?funcionarioId=' + funcId, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({}) });
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
            <div className='grid mb-3'>
                <div className='bc-form-col-6 col-12 md:col-6'><Card><small>Horas no mês</small><div className='text-2xl font-bold'>{totHoras.toFixed(2)}h</div></Card></div>
                <div className='bc-form-col-6 col-12 md:col-6'><Card><small>Faltas</small><div className='text-2xl font-bold'>{faltas}</div></Card></div>
            </div>
            <DataTable value={rows} loading={loading} paginator rows={15} emptyMessage='Sem registros.' responsiveLayout='scroll' dataKey='id'>
                <Column field='data' header='Data' style={{ width: '8rem' }} />
                <Column field='e1' header='E1' /><Column field='s1' header='S1' /><Column field='e2' header='E2' /><Column field='s2' header='S2' />
                <Column field='horasTrabalhadas' header='Horas' style={{ width: '6rem' }} />
                <Column header='Falta' body={(r) => (r.falta ? <Tag value='Falta' severity='danger' /> : null)} style={{ width: '6rem' }} />
            </DataTable>
        </div>
    );
};
export default Ponto;
