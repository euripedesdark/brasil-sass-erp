import React, { useEffect, useRef, useState } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { InputText } from 'primereact/inputtext';
import { InputTextarea } from 'primereact/inputtextarea';
import { Message } from 'primereact/message';
import { Toast } from 'primereact/toast';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Dialog } from 'primereact/dialog';

export const CteMdfe = () => {
    const toast=useRef(null);
    const [cte,setCte]=useState(null), [mdfe,setMdfe]=useState(null);
    const [recibo,setRecibo]=useState(''), [ret,setRet]=useState(null);
    const [erro,setErro]=useState(''), [ctes,setCtes]=useState([]), [mdfes,setMdfes]=useState([]);
    const [carregando,setCarregando]=useState(false), [detalhe,setDetalhe]=useState(null), [tipoDetalhe,setTipoDetalhe]=useState('');
    const [emissao,setEmissao]=useState(null), [xml,setXml]=useState(''), [salvando,setSalvando]=useState(false);
    const [evento,setEvento]=useState(null), [motivo,setMotivo]=useState(''), [municipio,setMunicipio]=useState(''), [uf,setUf]=useState('SP');

    const json=async r=>r.json().catch(()=>({}));
    const carregar=async()=>{setErro('');try{
        const [c,m]=await Promise.all([apiFetch('/api/fiscal/cte/status').then(json),apiFetch('/api/fiscal/mdfe/status').then(json)]);
        setCte(c);setMdfe(m);
    }catch(e){setErro('Falha ao consultar os serviços fiscais.');}};
    const carregarDocumentos=async()=>{setCarregando(true);try{
        const [rc,rm]=await Promise.all([apiFetch('/api/fiscal/cte?size=50'),apiFetch('/api/fiscal/mdfe?size=50')]);
        if(rc.ok)setCtes((await json(rc)).content||[]);
        if(rm.ok)setMdfes((await json(rm)).content||[]);
    }catch(e){setErro('Falha ao carregar documentos.');}finally{setCarregando(false);}};
    useEffect(()=>{carregar();carregarDocumentos();},[]);

    const abrirDetalhe=async(tipo,id)=>{const r=await apiFetch('/api/fiscal/'+tipo+'/'+id);if(!r.ok){toast.current?.show({severity:'error',summary:'Erro',detail:'Documento não encontrado',life:3500});return;}setTipoDetalhe(tipo==='cte'?'CT-e':'MDF-e');setDetalhe(await json(r));};
    const baixarXml=()=>{if(!detalhe?.xml)return;const blob=new Blob([detalhe.xml],{type:'application/xml'});const a=document.createElement('a');a.href=URL.createObjectURL(blob);a.download=(detalhe.chaveAcesso||tipoDetalhe+'-'+detalhe.id)+'.xml';a.click();URL.revokeObjectURL(a.href);};
    const consultarRecibo=async()=>{if(!recibo.trim())return;const r=await apiFetch('/api/fiscal/mdfe/recibo?numero='+encodeURIComponent(recibo.trim()));const b=await json(r);if(!r.ok){toast.current?.show({severity:'error',summary:'Erro',detail:b.message||b.erro||'Recibo não encontrado',life:4000});return;}setRet(b?.data||b);};
    const abrirEmissao=tipo=>{setEmissao(tipo);setXml('');};
    const enviar=async()=>{if(!xml.trim())return;setSalvando(true);try{
        const rota='/api/fiscal/'+emissao+'/emitir';const r=await apiFetch(rota,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({xml})});const b=await json(r);
        if(!r.ok)throw new Error(b.message||b.erro||'Falha na emissão.');
        toast.current?.show({severity:'success',summary:'Enviado',detail:b.xMotivo||'Documento processado pela SEFAZ.',life:5000});
        setEmissao(null);setXml('');await carregarDocumentos();
    }catch(e){toast.current?.show({severity:'error',summary:'Emissão',detail:e.message,life:7000});}finally{setSalvando(false);}};
    const executarEvento=async()=>{if(!evento)return;setSalvando(true);try{
        let body={};let rota='';
        if(evento.acao==='cancelar'){if(!motivo.trim())throw new Error('Informe o motivo.');rota='/api/fiscal/'+evento.tipo+'/'+evento.id+'/cancelar';body={motivo};}
        else {if(!municipio.match(/^\\d{7}$/))throw new Error('Código IBGE deve ter 7 dígitos.');rota='/api/fiscal/mdfe/'+evento.id+'/encerrar';body={codigoMunicipio:municipio,uf};}
        const r=await apiFetch(rota,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(body)});const b=await json(r);if(!r.ok)throw new Error(b.message||b.erro||'Operação fiscal recusada.');
        toast.current?.show({severity:'success',summary:'Operação concluída',detail:b.xMotivo||'SEFAZ processou o evento.',life:5000});
        setEvento(null);setMotivo('');setMunicipio('');await carregarDocumentos();
    }catch(e){toast.current?.show({severity:'error',summary:'Evento fiscal',detail:e.message,life:7000});}finally{setSalvando(false);}};
    const acao=(tipo,row)=> <div className='flex gap-1'>
        <Button icon='pi pi-eye' rounded text tooltip='Detalhes' onClick={()=>abrirDetalhe(tipo,row.id)}/>
        {row.status==='AUTORIZADA'&&<Button icon='pi pi-times' rounded text severity='danger' tooltip='Cancelar' onClick={()=>{setEvento({tipo,id:row.id,acao:'cancelar'});setMotivo('');}}/>}
        {tipo==='mdfe'&&row.status==='AUTORIZADA'&&<Button icon='pi pi-check' rounded text severity='success' tooltip='Encerrar' onClick={()=>{setEvento({tipo,id:row.id,acao:'encerrar'});setMunicipio('');}}/>}
    </div>;
    const kv=o=>!o||typeof o!=='object'?String(o??'-'):Object.entries(o).map(([k,v])=><div key={k}><strong>{k}:</strong> {typeof v==='object'?JSON.stringify(v):String(v)}</div>);
    const fmtData=v=>v?new Date(v).toLocaleString('pt-BR'):'-';
    return <div className='p-4'>
        <Toast ref={toast}/>
        <div className='flex justify-content-between align-items-center mb-3 flex-wrap gap-2'>
            <div><h2 className='m-0'>CT-e / MDF-e</h2><span className='bc-muted'>Emissão, consulta, cancelamento, encerramento e XML fiscal</span></div>
            <div className='flex gap-2 flex-wrap'><Button label='Emitir CT-e' icon='pi pi-send' onClick={()=>abrirEmissao('cte')}/><Button label='Emitir MDF-e' icon='pi pi-send' severity='secondary' onClick={()=>abrirEmissao('mdfe')}/><Button label='Atualizar' icon='pi pi-refresh' outlined onClick={()=>{carregar();carregarDocumentos();}}/></div>
        </div>
        {erro&&<Message severity='error' text={erro} className='w-full mb-3'/>}
        <div className='grid'><div className='bc-form-col-6 col-12 md:col-6'><Card title='CT-e — serviço'>{cte?kv(cte):'Clique em atualizar.'}</Card></div><div className='bc-form-col-6 col-12 md:col-6'><Card title='MDF-e — serviço'>{mdfe?kv(mdfe):'Clique em atualizar.'}</Card></div></div>
        <Card title='MDF-e — consultar recibo' className='mt-3'><div className='flex gap-2 flex-wrap'><InputText value={recibo} onChange={e=>setRecibo(e.target.value)} placeholder='Número do recibo' style={{flex:1}}/><Button label='Consultar' icon='pi pi-search' onClick={consultarRecibo}/></div>{ret&&<div className='mt-3'>{kv(ret)}</div>}</Card>
        <Card title='CT-e emitidos' className='mt-3'><DataTable value={ctes} loading={carregando} size='small' paginator rows={10} emptyMessage='Nenhum CT-e gravado.'><Column field='numero' header='Número'/><Column field='serie' header='Série'/><Column field='status' header='Status'/><Column field='dataEmissao' header='Emissão' body={r=>fmtData(r.dataEmissao)}/><Column field='chaveAcesso' header='Chave'/><Column header='Ações' body={r=>acao('cte',r)}/></DataTable></Card>
        <Card title='MDF-e emitidos' className='mt-3'><DataTable value={mdfes} loading={carregando} size='small' paginator rows={10} emptyMessage='Nenhum MDF-e gravado.'><Column field='numero' header='Número'/><Column field='serie' header='Série'/><Column field='status' header='Status'/><Column field='dataEmissao' header='Emissão' body={r=>fmtData(r.dataEmissao)}/><Column field='ufInicio' header='UF início'/><Column field='ufFim' header='UF fim'/><Column field='chaveAcesso' header='Chave'/><Column header='Ações' body={r=>acao('mdfe',r)}/></DataTable></Card>

        <Dialog visible={!!emissao} onHide={()=>!salvando&&setEmissao(null)} header={emissao==='cte'?'Emitir CT-e 4.00':'Emitir MDF-e 3.00'} modal style={{width:'min(96vw,1000px)'}}>
            <Message severity='info' text='Cole o XML fiscal ainda não assinado. O ERP valida, assina com o A1 configurado e transmite à SEFAZ.' className='w-full mb-3'/>
            <InputTextarea value={xml} onChange={e=>setXml(e.target.value)} rows={22} autoResize={false} className='w-full' placeholder={emissao==='cte'?'<CTe>...</CTe>':'<MDFe>...</MDFe>'}/>
            <div className='flex justify-content-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={()=>setEmissao(null)} disabled={salvando}/><Button label={salvando?'Enviando...':'Transmitir'} icon='pi pi-send' onClick={enviar} loading={salvando} disabled={!xml.trim()}/></div>
        </Dialog>
        <Dialog visible={!!evento} onHide={()=>!salvando&&setEvento(null)} header={evento?.acao==='encerrar'?'Encerrar MDF-e':'Cancelar documento fiscal'} modal style={{width:'min(96vw,600px)'}}>
            {evento?.acao==='encerrar'?<div className='grid'><div className='col-8'><label className='block mb-2'>Município de encerramento (IBGE)</label><InputText value={municipio} onChange={e=>setMunicipio(e.target.value.replace(/\\D/g,''))} maxLength={7} className='w-full'/></div><div className='col-4'><label className='block mb-2'>UF</label><InputText value={uf} onChange={e=>setUf(e.target.value.toUpperCase())} maxLength={2} className='w-full'/></div></div>:<><label className='block mb-2'>Motivo</label><InputTextarea value={motivo} onChange={e=>setMotivo(e.target.value)} rows={5} maxLength={255} className='w-full' placeholder='Informe o motivo legal do cancelamento (mínimo 15 caracteres).'/></>}
            <div className='flex justify-content-end gap-2 mt-3'><Button label='Voltar' text onClick={()=>setEvento(null)} disabled={salvando}/><Button label='Confirmar' icon='pi pi-check' onClick={executarEvento} loading={salvando}/></div>
        </Dialog>
        <Dialog visible={!!detalhe} onHide={()=>setDetalhe(null)} header={tipoDetalhe+(detalhe?' nº '+(detalhe.numero??detalhe.id):'')} modal style={{width:'min(96vw,900px)'}}>
            {detalhe&&<><div>{kv({serie:detalhe.serie,status:detalhe.status,emissao:fmtData(detalhe.dataEmissao),chaveAcesso:detalhe.chaveAcesso})}</div><h4>XML</h4><pre style={{maxHeight:'45vh',overflow:'auto',whiteSpace:'pre-wrap',wordBreak:'break-all'}}>{detalhe.xml||'Sem XML.'}</pre></>}
            <div className='flex justify-content-end gap-2 mt-3'><Button label='Baixar XML' icon='pi pi-download' outlined disabled={!detalhe?.xml} onClick={baixarXml}/><Button label='Fechar' text onClick={()=>setDetalhe(null)}/></div>
        </Dialog>
    </div>;
};
export default CteMdfe;
