import React, { useState, useEffect, useRef } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';

const BASE = '/api/rh/esocial';
const EVENTOS = ['S-2200','S-1200','S-1210','S-2299','S-3000'];

export const Esocial = () => {
    const toast = useRef(null);
    const [funcs, setFuncs] = useState([]);
    const [funcId, setFuncId] = useState(null);
    const [tipo, setTipo] = useState('S-2200');
    const [status, setStatus] = useState([]);
    const [loading, setLoading] = useState(false);
    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? j ?? []); };
    useEffect(() => { apiFetch('/api/rh/funcionarios').then(js).then((l) => setFuncs(l)).catch(() => {}); }, []);
    const ver = async (id) => {
        const fid = id ?? funcId;
        if (!fid) return;
        setLoading(true);
        try { setStatus(await apiFetch(BASE + '/eventos/status?funcionarioId=' + fid).then(js)); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha ao consultar', life: 4000 }); }
        finally { setLoading(false); }
    };
    const enviar = async () => {
        if (!funcId) return;
        const r = await apiFetch(BASE + '/eventos', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ tipoEvento: tipo, funcionarioId: funcId }) });
        const j = await r.json().catch(() => ({}));
        const rec = j?.recibo ?? j?.data?.recibo ?? JSON.stringify(j);
        toast.current?.show({ severity: r.ok ? 'success' : 'error', summary: r.ok ? 'Enviado' : 'Erro', detail: String(rec).slice(0, 200), life: 6000 });
        if (r.ok) ver(funcId);
    };
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='mb-3'><h2 className='m-0'>eSocial</h2><span className='bc-muted'>Eventos via microserviço (S-2200 admissão, S-1200 remuneração, S-1210 pagamento, S-2299 desligamento)</span></div>
            <div className='flex gap-2 mb-3 flex-wrap align-items-end'>
                <span><label className='bc-label'>Funcionário</label><Dropdown value={funcId} options={funcs.map((f) => ({ label: f.nome || f.id, value: f.id }))} onChange={(e) => setFuncId(e.value)} placeholder='Selecione' filter style={{ minWidth: '16rem' }} /></span>
                <span><label className='bc-label'>Evento</label><Dropdown value={tipo} options={EVENTOS.map((e) => ({ label: e, value: e }))} onChange={(e) => setTipo(e.value)} /></span>
                <Button label='Consultar' icon='pi pi-search' outlined onClick={() => ver()} />
                <Button label='Enviar evento' icon='pi pi-send' severity='success' onClick={enviar} />
            </div>
            {status && status.length > 0 && (<Card title='Status no eSocial'><pre style={{ whiteSpace: 'pre-wrap' }}>{JSON.stringify(status, null, 2)}</pre></Card>)}
        </div>
    );
};
export default Esocial;
