import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { InputNumber } from 'primereact/inputnumber';
import { Tag } from 'primereact/tag';
import { Dialog } from 'primereact/dialog';
import { InputTextarea } from 'primereact/inputtextarea';
import ConferenciaCompraService from '../../services/ConferenciaCompraService';
import { localeAtivo } from '../shared/LocaleData.js';

export default function ConferenciaFaturasCompra() {
        const { t } = useTranslation();
    const [rows,setRows]=useState([]);
    const [form,setForm]=useState({pedidoId:null,valorFatura:0,tolerancia:0,nfeId:null,tituloId:null,recebimentoId:null});
    const [error,setError]=useState('');
    const [detalhe,setDetalhe]=useState(null);
    const [itens,setItens]=useState([]);
    const [processando,setProcessando]=useState(false);
    const [excecao,setExcecao]=useState(null);
    const [motivo,setMotivo]=useState('');
    const [salvandoExcecao,setSalvandoExcecao]=useState(false);
    const [expandedRows,setExpandedRows]=useState(null);
    const [itemRows,setItemRows]=useState({});
    const carregar=async()=>setRows((await ConferenciaCompraService.conferencias()).data||[]);
    useEffect(()=>{carregar().catch(e=>setError(e.response?.data?.message || t('legacyUi.conferencia.error')))},[]);
    const carregarItens=async(row)=>{
        if (itemRows[row.id]) return;
        try {
            const data=(await ConferenciaCompraService.itensConferencia(row.id)).data || [];
            setItemRows(prev=>({...prev,[row.id]:data}));
        } catch(e) { setError(e.response?.data?.message || t('legacyUi.conferencia.error')); }
    };
    const onRowToggle=async(e)=>{
        setExpandedRows(e.data);
        for (const key of Object.keys(e.data||{})) {
            const row=rows.find(r=>String(r.id)===String(key));
            if (row) await carregarItens(row);
        }
    };
    const itemExpansion=(row)=><DataTable value={itemRows[row.id]||[]} size="small" stripedRows emptyMessage="—">
        <Column field="pedidoItemId" header={t('legacyUi.conferencia.order')}/>
        <Column field="produtoId" header={t('legacyUi.recebimentos.product')}/>
        <Column field="quantidadePedida" header={t('legacyUi.conferencia.ordered')}/>
        <Column field="quantidadeRecebida" header={t('legacyUi.conferencia.received')}/>
        <Column field="quantidadeFaturada" header={t('legacyUi.conferencia.billed')}/>
        <Column field="valorUnitarioPedido" header={t('legacyUi.conferencia.order')}/>
        <Column field="valorUnitarioRecebido" header={t('legacyUi.conferencia.received')}/>
        <Column field="valorUnitarioFaturado" header={t('legacyUi.conferencia.billed')}/>
        <Column field="status" header={t('legacyUi.conferencia.status')} body={r=><Tag value={r.status} severity={r.status==='APROVADA'?'success':'danger'}/>}/>
        <Column field="divergencia" header={t('legacyUi.conferencia.divergence')}/>
    </DataTable>;
    const conferir=async()=>{
        setError('');
        setProcessando(true);
        try { await ConferenciaCompraService.conferir(form); setForm({pedidoId:null,valorFatura:0,tolerancia:0,nfeId:null,tituloId:null,recebimentoId:null}); setItemRows({}); await carregar(); }
        catch(e){setError(e.response?.data?.message || t('legacyUi.conferencia.error'));}
        finally {setProcessando(false);}
    };
    const abrirItens=async(row)=>{
        setError('');
        try {
            const data=(await ConferenciaCompraService.itensConferencia(row.id)).data||[];
            setItens(data);
            setDetalhe(row);
        } catch(e){setError(e.response?.data?.message || t('legacyUi.conferencia.error'));}
    };
    const fecharItens=()=>{setDetalhe(null);setItens([]);};
    const abrirExcecao=(row)=>{setExcecao(row);setMotivo();setError();};
    const fecharExcecao=()=>{setExcecao(null);setMotivo();};
    const salvarExcecao=async()=>{
        if(!excecao||motivo.trim().length<10){setError(t('legacyUi.conferencia.reasonShort'));return;}
        setSalvandoExcecao(true);setError();
        try{await ConferenciaCompraService.aprovarExcepcional(excecao.id,motivo.trim());fecharExcecao();await carregar();}
        catch(e){setError(e?.response?.data?.message||e?.response?.data?.errors?.[0]?.message||t('legacyUi.conferencia.approveFail'));}
        finally{setSalvandoExcecao(false);}
    };
    const quantidade=(v)=>v==null?'':Number(v).toLocaleString(localeAtivo(),{minimumFractionDigits:3,maximumFractionDigits:4});
    const dinheiro=(v)=>v==null?'':Number(v).toLocaleString(localeAtivo(),{style:'currency',currency:'BRL'});
    return <Card title={t('legacyUi.conferencia.title')}>
        <div className="grid align-items-end mb-4">
            <div className="col-12 md:col-2 field"><label>{t('legacyUi.conferencia.orderLabel')}</label><InputNumber value={form.pedidoId} onValueChange={e=>setForm({...form,pedidoId:e.value})}/></div>
            <div className="col-12 md:col-2 field"><label>{t('legacyUi.conferencia.receiptLabel')}</label><InputNumber value={form.recebimentoId} onValueChange={e=>setForm({...form,recebimentoId:e.value})}/></div>
            <div className="col-12 md:col-2 field"><label>{t('legacyUi.conferencia.invoiceLabel')}</label><InputNumber value={form.nfeId} onValueChange={e=>setForm({...form,nfeId:e.value})}/></div>
            <div className="col-12 md:col-2 field"><label>{t('legacyUi.conferencia.titleLabel')}</label><InputNumber value={form.tituloId} onValueChange={e=>setForm({...form,tituloId:e.value})}/></div>
            <div className="col-12 md:col-2 field"><label>{t('legacyUi.conferencia.invoiceAmount')}</label><InputNumber value={form.valorFatura} onValueChange={e=>setForm({...form,valorFatura:e.value})} mode="currency" currency="BRL" locale={localeAtivo()}/></div>
            <div className="col-12 md:col-2 field"><label>{t('legacyUi.conferencia.tolerance')}</label><InputNumber value={form.tolerancia} onValueChange={e=>setForm({...form,tolerancia:e.value})} mode="currency" currency="BRL" locale={localeAtivo()}/></div>
            <div className="col-12"><Button label={t('legacyUi.conferencia.run')} icon="pi pi-check-circle" onClick={conferir} loading={processando} disabled={!form.pedidoId || !form.recebimentoId || !form.nfeId || !(form.valorFatura>0) || form.tolerancia<0}/></div>
        </div>
        {error && <div className="p-error mb-3">{error}</div>}
        <DataTable value={rows} paginator rows={15} stripedRows dataKey="id" expandedRows={expandedRows} onRowToggle={onRowToggle} rowExpansionTemplate={itemExpansion}>
            <Column expander style={{width:"3rem"}} />
            <Column field="pedidoId" header={t('legacyUi.conferencia.order')}/>
            <Column field="recebimentoId" header={t('legacyUi.conferencia.receipt')}/>
            <Column field="nfeId" header={t('legacyUi.conferencia.invoice')}/>
            <Column field="valorPedido" header={t('legacyUi.conferencia.order')}/>
            <Column field="valorRecebido" header={t('legacyUi.conferencia.received')}/>
            <Column field="valorFatura" header={t('legacyUi.conferencia.billed')}/>
            <Column field="status" header={t('legacyUi.conferencia.status')} body={r=><Tag value={r.status} severity={r.status==='APROVADA'?'success':'danger'}/>}/>
            <Column field="divergencia" header={t('legacyUi.conferencia.divergence')}/>
            <Column header={t('legacyUi.conferencia.actions')} body={r=>r.status==="DIVERGENTE"?<Button label={t('legacyUi.conferencia.approveException')} icon="pi pi-check" className="p-button-text p-button-sm p-button-warning" onClick={()=>abrirExcecao(r)}/>:null}/>
            <Column header={t('legacyUi.conferencia.items')} body={r=><Button label={t('legacyUi.conferencia.items')} icon="pi pi-list" className="p-button-text p-button-sm" onClick={()=>abrirItens(r)}/>}/>
        </DataTable>
        <Dialog header={t('legacyUi.conferencia.itemsChecked')} visible={detalhe!==null} style={{width:'75vw'}} onHide={fecharItens}>
            <DataTable value={itens} paginator rows={10} stripedRows emptyMessage={t('legacyUi.conferencia.noItemsChecked')}>
                <Column field="numeroItem" header={t('legacyUi.conferencia.item')}/>
                <Column field="descricao" header={t('legacyUi.conferencia.description')}/>
                <Column header={t('legacyUi.conferencia.qtyOrdered')} body={r=>quantidade(r.quantidadePedida)}/>
                <Column header={t('legacyUi.conferencia.qtyReceived')} body={r=>quantidade(r.quantidadeRecebida)}/>
                <Column header={t('legacyUi.conferencia.qtyBilled')} body={r=>quantidade(r.quantidadeFaturada)}/>
                <Column header={t('legacyUi.conferencia.priceOrdered')} body={r=>dinheiro(r.valorUnitarioPedido)}/>
                <Column header={t('legacyUi.conferencia.priceBilled')} body={r=>dinheiro(r.valorUnitarioFaturado)}/>
                <Column header={t('legacyUi.conferencia.situation')} body={r=><Tag value={r.conforme?'Conforme':r.tipoDivergencia} severity={r.conforme?'success':'danger'}/>}/>
                <Column field="divergencia" header={t('legacyUi.conferencia.divergence')}/>
            </DataTable>
        </Dialog>
        <Dialog header={t('legacyUi.conferencia.exceptionTitle')} visible={excecao!==null} style={{width:"32rem"}} onHide={fecharExcecao}>
            <div className="field"><label>{t('legacyUi.conferencia.reasonMin')}</label><InputTextarea value={motivo} onChange={e=>setMotivo(e.target.value)} rows={4} className="w-full" /></div>
            <div className="flex justify-content-end gap-2 mt-3"><Button label={t('legacyUi.conferencia.cancel')} icon="pi pi-times" className="p-button-text" onClick={fecharExcecao}/><Button label={t('legacyUi.conferencia.approve')} icon="pi pi-check" loading={salvandoExcecao} disabled={motivo.trim().length<10} onClick={salvarExcecao}/></div>
        </Dialog>
    </Card>;
}
