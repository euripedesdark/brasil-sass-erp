import React, { useState, useRef, useEffect } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { InputText } from 'primereact/inputtext';
import { Message } from 'primereact/message';
import { Toast } from 'primereact/toast';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Dialog } from 'primereact/dialog';

export const CteMdfe = () => {
    const toast = useRef(null);
    const [cte, setCte] = useState(null);
    const [mdfe, setMdfe] = useState(null);
    const [recibo, setRecibo] = useState('');
    const [ret, setRet] = useState(null);
    const [erro, setErro] = useState('');
    const [ctes, setCtes] = useState([]);
    const [mdfes, setMdfes] = useState([]);
    const [carregandoDocs, setCarregandoDocs] = useState(false);
    const [detalhe, setDetalhe] = useState(null);
    const [tipoDetalhe, setTipoDetalhe] = useState('');
    const js = async (r) => r.json().catch(() => ({}));
    const carregar = async () => {
        setErro('');
        try {
            const [c, m] = await Promise.all([apiFetch('/api/fiscal/cte/status').then(js), apiFetch('/api/fiscal/mdfe/status').then(js)]);
            setCte(c); setMdfe(m);
        } catch (e) { setErro('Falha ao consultar os serviços.'); }
    };
    const carregarDocumentos = async () => {
        setCarregandoDocs(true);
        try {
            const [rc, rm] = await Promise.all([apiFetch('/api/fiscal/cte?size=50'), apiFetch('/api/fiscal/mdfe?size=50')]);
            if (rc.ok) setCtes((await rc.json().catch(() => ({}))).content || []);
            if (rm.ok) setMdfes((await rm.json().catch(() => ({}))).content || []);
        } catch (e) { setErro('Falha ao carregar os documentos emitidos.'); }
        finally { setCarregandoDocs(false); }
    };
    useEffect(() => { carregarDocumentos(); }, []);
    const abrirDetalhe = async (tipo, id) => {
        const r = await apiFetch('/api/fiscal/' + tipo + '/' + id);
        if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Documento não encontrado', life: 3500 }); return; }
        setTipoDetalhe(tipo === 'cte' ? 'CT-e' : 'MDF-e');
        setDetalhe(await r.json().catch(() => null));
    };
    const baixarXml = () => {
        if (!detalhe?.xml) return;
        const blob = new Blob([detalhe.xml], { type: 'application/xml' });
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = (detalhe.chaveAcesso || (tipoDetalhe + '-' + detalhe.id)) + '.xml';
        a.click();
        URL.revokeObjectURL(a.href);
    };
    const fmtData = (v) => v ? new Date(v).toLocaleString('pt-BR') : '-';
    const fmtMoeda = (v) => v == null ? '-' : Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
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
                <div><h2 className='m-0'>CT-e / MDF-e</h2><span className='bc-muted'>Status dos serviços, consulta de recibo e documentos gravados</span></div>
                <Button label='Consultar serviços' icon='pi pi-refresh' onClick={carregar} />
            </div>
            {erro && (<Message severity='error' text={erro} className='w-full mb-3' />)}
            <div className='grid'>
                <div className='bc-form-col-6 col-12 md:col-6'><Card title='CT-e — status do serviço'>{cte ? kv(cte) : <span className='bc-muted'>Clique em consultar.</span>}</Card></div>
                <div className='bc-form-col-6 col-12 md:col-6'><Card title='MDF-e — status do serviço'>{mdfe ? kv(mdfe) : <span className='bc-muted'>Clique em consultar.</span>}</Card></div>
            </div>
            <Card title='MDF-e — consultar recibo' className='mt-3'>
                <div className='flex gap-2 flex-wrap'><InputText value={recibo} onChange={(e) => setRecibo(e.target.value)} placeholder='Número do recibo' style={{ flex: 1 }} /><Button label='Consultar' icon='pi pi-search' onClick={consultar} /></div>
                {ret && ret.sucesso === null && (<Message severity='warn' className='w-full mt-3' text={ret.xMotivo || 'Não foi possível confirmar a autorização. Verifique antes de reemitir.'} />)}
                {ret && (<div className='mt-3'>{kv(ret)}</div>)}
            </Card>
            <Card title='CT-e emitidos' className='mt-3'>
                <DataTable value={ctes} loading={carregandoDocs} size='small' paginator rows={10} emptyMessage='Nenhum CT-e gravado.'>
                    <Column field='numero' header='Número' />
                    <Column field='serie' header='Série' />
                    <Column field='status' header='Status' />
                    <Column field='dataEmissao' header='Emissão' body={(r) => fmtData(r.dataEmissao)} />
                    <Column field='valorFrete' header='Frete' body={(r) => fmtMoeda(r.valorFrete)} />
                    <Column field='chaveAcesso' header='Chave de acesso' />
                    <Column header='' body={(r) => (<Button icon='pi pi-eye' rounded text tooltip='Detalhes e XML' onClick={() => abrirDetalhe('cte', r.id)} />)} style={{ width: '4rem' }} />
                </DataTable>
            </Card>
            <Card title='MDF-e emitidos' className='mt-3'>
                <DataTable value={mdfes} loading={carregandoDocs} size='small' paginator rows={10} emptyMessage='Nenhum MDF-e gravado.'>
                    <Column field='numero' header='Número' />
                    <Column field='serie' header='Série' />
                    <Column field='status' header='Status' />
                    <Column field='dataEmissao' header='Emissão' body={(r) => fmtData(r.dataEmissao)} />
                    <Column field='ufInicio' header='UF início' />
                    <Column field='ufFim' header='UF fim' />
                    <Column field='chaveAcesso' header='Chave de acesso' />
                    <Column header='' body={(r) => (<Button icon='pi pi-eye' rounded text tooltip='Detalhes e XML' onClick={() => abrirDetalhe('mdfe', r.id)} />)} style={{ width: '4rem' }} />
                </DataTable>
            </Card>
            <Dialog visible={!!detalhe} onHide={() => setDetalhe(null)} header={tipoDetalhe + (detalhe ? ' nº ' + (detalhe.numero ?? detalhe.id) : '')} modal style={{ width: 'min(96vw, 760px)' }}>
                {detalhe && (<div>
                    {kv({ serie: detalhe.serie, status: detalhe.status, emissao: fmtData(detalhe.dataEmissao), chaveAcesso: detalhe.chaveAcesso })}
                    <h4 className='mt-3 mb-2'>XML</h4>
                    {detalhe.xml
                        ? (<pre style={{ maxHeight: '45vh', overflow: 'auto', whiteSpace: 'pre-wrap', wordBreak: 'break-all' }}>{detalhe.xml}</pre>)
                        : (<span className='bc-muted'>Sem XML gravado para este documento.</span>)}
                </div>)}
                <div className='flex justify-end gap-2 mt-3'>
                    <Button label='Baixar XML' icon='pi pi-download' outlined disabled={!detalhe?.xml} onClick={baixarXml} />
                    <Button label='Fechar' text severity='secondary' onClick={() => setDetalhe(null)} />
                </div>
            </Dialog>
        </div>
    );
};
export default CteMdfe;
