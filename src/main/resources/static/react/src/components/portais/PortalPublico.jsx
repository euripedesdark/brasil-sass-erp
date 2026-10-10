import React, { useState, useRef } from 'react';
import { localeAtivo } from '../shared/LocaleData.js';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { InputText } from 'primereact/inputtext';
import { Message } from 'primereact/message';
import { Toast } from 'primereact/toast';

const fmt = (v) => Number(v ?? 0).toLocaleString(localeAtivo(), { style: 'currency', currency: 'BRL' });

export const PortalPublico = () => {
    const toast = useRef(null);
    const [token, setToken] = useState(new URLSearchParams(window.location.search).get('token') || '');
    const [conta, setConta] = useState(null);
    const [erro, setErro] = useState('');
    const entrar = async () => {
        setErro(''); setConta(null);
        if (!token.trim()) { setErro('Informe o token de acesso.'); return; }
        try {
            const r = await fetch('/api/portais/publico/minha-conta?token=' + encodeURIComponent(token.trim()));
            if (r.status === 401) { setErro('Token expirado ou revogado.'); return; }
            if (!r.ok) { setErro('Falha ao carregar. Tente de novo.'); return; }
            setConta(await r.json());
        } catch (e) { setErro('Falha de comunicação.'); }
    };
    return (
        <div className='p-4' style={{ maxWidth: '880px', margin: '2rem auto' }}>
            <Toast ref={toast} />
            <Card title='Portal de Autoatendimento' subTitle='Brasil SaaS ERP'>
                {!conta && (<div className='flex gap-2'><InputText value={token} onChange={(e) => setToken(e.target.value)} placeholder='Cole seu token de acesso' style={{ flex: 1 }} /><Button label='Entrar' icon='pi pi-arrow-right' onClick={entrar} /></div>)}
                {erro && (<Message severity='error' text={erro} className='w-full mt-3' />)}
                {conta && (<div className='mt-3'>
                    <h3 className='m-0'>{conta.nome}</h3>
                    <p className='bc-muted'>{conta.tipo} · {conta.documento}</p>
                    {(conta.titulos || []).length > 0 && (<><h4>Títulos em aberto</h4><DataTable value={conta.titulos} responsiveLayout='scroll'><Column field='descricao' header='Descrição' /><Column field='vencimento' header='Vencimento' /><Column header='Saldo' body={(r) => fmt(r.saldo)} /></DataTable></>)}
                    {(conta.pedidos || []).length > 0 && (<><h4 className='mt-4'>Pedidos</h4><DataTable value={conta.pedidos} responsiveLayout='scroll'><Column field='id' header='#' /><Column field='status' header='Status' /></DataTable></>)}
                    {(conta.titulos || []).length === 0 && (conta.pedidos || []).length === 0 && (<p className='bc-muted'>Nada pendente. Obrigado!</p>)}
                </div>)}
            </Card>
        </div>
    );
};
export default PortalPublico;
