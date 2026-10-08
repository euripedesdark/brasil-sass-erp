import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import ConferenciaCompraService from '../../services/ConferenciaCompraService';

export default function ConferenciaFaturasCompra() {
        const { t } = useTranslation();
    const [rows,setRows]=useState([]);
    const [form,setForm]=useState({pedidoId:null,valorFatura:0,tolerancia:0,nfeId:null,tituloId:null,recebimentoId:null});
    const [error,setError]=useState('');
    const [expandedRows,setExpandedRows]=useState(null);
    const [itemRows,setItemRows]=useState({});
    const carregar=async()=>setRows((await ConferenciaCompraService.conferencias()).data||[]);
    useEffect(()=>{carregar()},[]);
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
        <Column field="status" header="Status" body={r=><Tag value={r.status} severity={r.status==='APROVADA'?'success':'danger'}/>}/>
        <Column field="divergencia" header={t('legacyUi.conferencia.divergence')}/>
    </DataTable>;
    const conferir=async()=>{
        setError('');
        try { await ConferenciaCompraService.conferir(form); setForm({...form,pedidoId:null,valorFatura:0}); await carregar(); }
        catch(e){setError(e.response?.data?.message || t('legacyUi.conferencia.error'));}
    };
    return <Card title={t('legacyUi.conferencia.title')}>
        <div className="grid align-items-end mb-4">
            <div className="col-12 md:col-2 field"><label>Pedido</label><InputNumber value={form.pedidoId} onValueChange={e=>setForm({...form,pedidoId:e.value})}/></div>
            <div className="col-12 md:col-2 field"><label>Recebimento</label><InputNumber value={form.recebimentoId} onValueChange={e=>setForm({...form,recebimentoId:e.value})}/></div>
            <div className="col-12 md:col-2 field"><label>NF-e</label><InputNumber value={form.nfeId} onValueChange={e=>setForm({...form,nfeId:e.value})}/></div>
            <div className="col-12 md:col-2 field"><label>Título</label><InputNumber value={form.tituloId} onValueChange={e=>setForm({...form,tituloId:e.value})}/></div>
            <div className="col-12 md:col-2 field"><label>Valor da fatura</label><InputNumber value={form.valorFatura} onValueChange={e=>setForm({...form,valorFatura:e.value})} mode="currency" currency="BRL" locale="pt-BR"/></div>
            <div className="col-12 md:col-2 field"><label>Tolerância</label><InputNumber value={form.tolerancia} onValueChange={e=>setForm({...form,tolerancia:e.value})} mode="currency" currency="BRL" locale="pt-BR"/></div>
            <div className="col-12"><Button label={t('legacyUi.conferencia.run')} icon="pi pi-check-circle" onClick={conferir}/></div>
        </div>
        {error && <div className="p-error mb-3">{error}</div>}
        <DataTable value={rows} paginator rows={15} stripedRows dataKey="id" expandedRows={expandedRows} onRowToggle={onRowToggle} rowExpansionTemplate={itemExpansion}>\n            <Column expander style={{width:"3rem"}} />
            <Column field="pedidoId" header={t('legacyUi.conferencia.order')}/>
            <Column field="recebimentoId" header={t('legacyUi.conferencia.receipt')}/>
            <Column field="nfeId" header={t('legacyUi.conferencia.invoice')}/>
            <Column field="valorPedido" header={t('legacyUi.conferencia.order')}/>
            <Column field="valorRecebido" header={t('legacyUi.conferencia.received')}/>
            <Column field="valorFatura" header={t('legacyUi.conferencia.billed')}/>
            <Column field="status" header="Status" body={r=><Tag value={r.status} severity={r.status==='APROVADA'?'success':'danger'}/>}/>
            <Column field="divergencia" header={t('legacyUi.conferencia.divergence')}/>
        </DataTable>
    </Card>;
}
