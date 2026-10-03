import React, { useState, useRef } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { InputText } from 'primereact/inputtext';
import { Message } from 'primereact/message';
import { Toast } from 'primereact/toast';

export const CteMdfe = () => {
    const toast = useRef(null);
    const [cte, setCte] = useState(null);
    const [mdfe, setMdfe] = useState(null);
    const [recibo, setRecibo] = useState('');
    const [ret, setRet] = useState(null);
    const [erro, setErro] = useState('');
    const js = async (r) => r.json().catch(() => ({}));
    const carregar = async () => {
        setErro('');
        try {
            const [c, m] = await Promise.all([apiFetch('/api/fiscal/cte/status').then(js), apiFetch('/api/fiscal/mdfe/status').then(js)]);
            setCte(c); setMdfe(m);
        } catch (e) { setErro('Falha ao consultar os serviços.'); }
    };
    const consultar = async () => {
        if (!recibo.trim()) return;
        const r = await apiFetch('/api/fiscal/mdfe/recibo?numero=' + encodeURIComponent(recibo.trim()));
        if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Recibo não encontrado', life: 3500 }); return; }
        setRet(await r.json().catch(() => ({})));
    };
    const kv = (o) => !o || typeof o !== 'object' ? String(o ?? '-') : Object.entries(o).map(([k, v]) => (<div key={k}><strong>{k}:</strong> {typeof v === 'object' ? JSON.stringify(v) : String(v)}</div>));
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='flex justify-content-between align-items-center mb-3 flex-wrap gap-2'>
                <div><h2 className='m-0'>CT-e / MDF-e</h2><span className='bc-muted'>Status dos serviços e consulta de recibo</span></div>
                <Button label='Consultar serviços' icon='pi pi-refresh' onClick={carregar} />
            </div>
            {erro && (<Message severity='error' text={erro} className='w-full mb-3' />)}
            <div className='grid'>
                <div className='bc-form-col-6 col-12 md:col-6'><Card title='CT-e — status do serviço'>{cte ? kv(cte) : <span className='bc-muted'>Clique em consultar.</span>}</Card></div>
                <div className='bc-form-col-6 col-12 md:col-6'><Card title='MDF-e — status do serviço'>{mdfe ? kv(mdfe) : <span className='bc-muted'>Clique em consultar.</span>}</Card></div>
            </div>
            <Card title='MDF-e — consultar recibo' className='mt-3'>
                <div className='flex gap-2 flex-wrap'><InputText value={recibo} onChange={(e) => setRecibo(e.target.value)} placeholder='Número do recibo' style={{ flex: 1 }} /><Button label='Consultar' icon='pi pi-search' onClick={consultar} /></div>
                {ret && (<div className='mt-3'>{kv(ret)}</div>)}
            </Card>
        </div>
    );
};
export default CteMdfe;
