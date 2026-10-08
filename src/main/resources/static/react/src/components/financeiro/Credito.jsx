import React, { useEffect, useRef, useState } from 'react';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { apiFetch } from '../../services/ApiConfig';

const fmt = (v) => Number(v || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

const Credito = () => {
    const toast = useRef(null);
    const [clientes, setClientes] = useState([]);
    const [cliId, setCliId] = useState(null);
    const [res, setRes] = useState(null);
    const [loading, setLoading] = useState(false);
    const [novoLimite, setNovoLimite] = useState(null);
    const [salvando, setSalvando] = useState(false);

    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? j?.lista ?? j?.content ?? []); };
    useEffect(() => { apiFetch('/api/cadastro/clientes').then(js).then((l) => setClientes(l)).catch(() => {}); }, []);

    const analisar = async () => {
        if (!cliId) return;
        setLoading(true);
        try {
            const r = await apiFetch('/api/financeiro/credito/analise?clienteId=' + cliId);
            if (!r.ok) throw new Error();
            const data = await r.json();
            setRes(data);
            setNovoLimite(Number(data.limite ?? 0));
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha na análise', life: 4000 }); }
        finally { setLoading(false); }
    };

    const salvarLimite = async () => {
        if (!cliId || novoLimite == null || novoLimite < 0) {
            toast.current?.show({ severity: 'warn', summary: 'Informe um limite válido', life: 3000 });
            return;
        }
        setSalvando(true);
        try {
            const r = await apiFetch('/api/financeiro/credito/limite', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ clienteId: cliId, limite: novoLimite })
            });
            if (!r.ok) {
                const j = await r.json().catch(() => null);
                throw new Error(j?.message || 'Falha ao gravar limite');
            }
            const data = await r.json();
            setRes(data);
            setNovoLimite(Number(data.limite ?? 0));
            toast.current?.show({ severity: 'success', summary: 'Limite atualizado', life: 3000 });
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 });
        } finally { setSalvando(false); }
    };

    const sev = !res ? 'info' : res.situacao === 'OK' ? 'success' : res.situacao === 'INADIMPLENTE' ? 'warning' : 'danger';
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='mb-3'><h2 className='m-0'>Crédito do Cliente</h2><span className='bc-muted'>Análise e ajuste de limite</span></div>
            <div className='flex gap-2 mb-3 flex-wrap align-items-end'>
                <span><label className='bc-label'>Cliente</label>
                    <Dropdown value={cliId} options={clientes.map((c) => ({ label: (c.nome || c.pessoaNome || ('#' + c.id)), value: c.id }))}
                        onChange={(e) => { setCliId(e.value); setRes(null); }} placeholder='Selecione' filter style={{ minWidth: '18rem' }} /></span>
                <Button label='Analisar' icon='pi pi-search' onClick={analisar} loading={loading} />
            </div>
            {res && (<>
                <div className='mb-3'><Tag value={res.situacao} severity={sev} style={{ fontSize: '1.1rem' }} /></div>
                <div className='grid'>
                    {[['Limite', res.limite], ['Em aberto', res.emAberto], ['Vencido', res.vencido], ['Em pedidos', res.emPedidos], ['Comprometido', res.comprometido], ['Disponível', res.disponivel]].map(([l, v]) => (
                        <div key={l} className='bc-form-col-4 col-12 md:col-4'><Card><small>{l}</small><div className='text-xl font-bold'>{fmt(v)}</div></Card></div>
                    ))}
                </div>
                <Card className='mt-3' title='Ajustar limite de crédito'>
                    <div className='flex gap-2 flex-wrap align-items-end'>
                        <span><label className='bc-label'>Novo limite (R$)</label>
                            <InputNumber value={novoLimite} onValueChange={(e) => setNovoLimite(e.value)} mode='currency' currency='BRL' locale='pt-BR' min={0} /></span>
                        <Button label='Salvar limite' icon='pi pi-check' severity='success' onClick={salvarLimite} loading={salvando} />
                    </div>
                </Card>
            </>)}
        </div>
    );
};
export default Credito;
