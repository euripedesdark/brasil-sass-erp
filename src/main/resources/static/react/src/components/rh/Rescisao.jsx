import React, { useState, useEffect, useRef } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Card } from 'primereact/card';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';

const fmt = (v) => Number(v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export const Rescisao = () => {
    const toast = useRef(null);
    const [funcs, setFuncs] = useState([]);
    const [funcId, setFuncId] = useState(null);
    const [data, setData] = useState(new Date());
    const [motivo, setMotivo] = useState('SEM_JUSTA_CAUSA');
    const [res, setRes] = useState(null);
    const [loading, setLoading] = useState(false);
    useEffect(() => { apiFetch('/api/rh/funcionarios').then((r) => r.json().catch(() => [])).then((j) => setFuncs(Array.isArray(j) ? j : (j?.data ?? []))).catch(() => {}); }, []);
    const calcular = async () => {
        if (!funcId) return;
        setLoading(true);
        try {
            const iso = data ? data.toISOString().slice(0, 10) : '';
            const r = await apiFetch('/api/rh/rescisao/calcular?funcionarioId=' + funcId + '&desligamento=' + iso + '&motivo=' + motivo);
            if (!r.ok) throw new Error();
            setRes(await r.json());
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', life: 3000 }); }
        finally { setLoading(false); }
    };
    const linhas = res ? [['Saldo de salario', res.saldoSalario], ['13o proporcional', res.decimoTerceiro], ['Ferias proporcionais', res.ferias], ['1/3 de ferias', res.tercoFerias], ['Aviso previo', res.avisoPrevio], ['Multa 40% FGTS', res.multa40]] : [];
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='mb-3'><h2 className='m-0'>Rescisao</h2><span className='bc-muted'>Verbas rescisorias estimadas</span></div>
            <div className='flex gap-2 mb-3 flex-wrap align-items-end'>
                <span><label className='bc-label'>Funcionario</label><Dropdown value={funcId} options={funcs.map((f) => ({ label: f.nome || f.id, value: f.id }))} onChange={(e) => setFuncId(e.value)} placeholder='Selecione' filter style={{ minWidth: '16rem' }} /></span>
                <span><label className='bc-label'>Desligamento</label><Calendar value={data} onChange={(e) => setData(e.value)} dateFormat='dd/mm/yy' showIcon /></span>
                <span><label className='bc-label'>Motivo</label><Dropdown value={motivo} options={['SEM_JUSTA_CAUSA','COM_JUSTA_CAUSA','PEDIDO'].map((m) => ({ label: m, value: m }))} onChange={(e) => setMotivo(e.value)} /></span>
                <Button label='Calcular' icon='pi pi-calculator' onClick={calcular} loading={loading} />
            </div>
            {res && (<Card><p>Meses trabalhados: <strong>{res.mesesTrabalhados}</strong></p>{linhas.map(([l, v]) => (<p key={l}>{l}: <strong>{fmt(v)}</strong></p>))}<h3>Total: {fmt(res.total)}</h3></Card>)}
        </div>
    );
};
export default Rescisao;
