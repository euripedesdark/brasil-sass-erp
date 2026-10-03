import React, { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';

import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dropdown } from 'primereact/dropdown';
import { AutoComplete } from 'primereact/autocomplete';
import { InputNumber } from 'primereact/inputnumber';
import { InputTextarea } from 'primereact/inputtextarea';
import { Toast } from 'primereact/toast';
import axios from 'axios';
import ProdutoService from '../../services/ProdutoService';

const msg=e=>e?.response?.data?.errors?.[0]?.message||e?.response?.data?.message||e?.response?.data?.error||e?.message||'Operação não realizada';
export default function TransferenciasEstoque(){
 const { t } = useTranslation();
 
 const toast=useRef(null); const [modoInterno,setModoInterno]=useState(false); const [endOrigem,setEndOrigem]=useState(null); const [endDestino,setEndDestino]=useState(null); const [enderecos,setEnderecos]=useState([]); const [loteId,setLoteId]=useState(null); const [lotes,setLotes]=useState([]); const [depositos,setDepositos]=useState([]); const [rows,setRows]=useState([]); const [loading,setLoading]=useState(false); const [origem,setOrigem]=useState(null); const [destino,setDestino]=useState(null); const [produto,setProduto]=useState(null); const [sug,setSug]=useState([]); const [qtd,setQtd]=useState(0); const [itens,setItens]=useState([]); const [obs,setObs]=useState('');
 const load=async()=>{setLoading(true);try{const [d,t]=await Promise.all([axios.get('/api/estoque/depositos'),axios.get('/api/estoque/transferencias')]);setDepositos(d.data?.data??d.data??[]);setRows(t.data?.data??t.data??[])}catch(e){toast.current?.show({severity:'error',summary:'Erro',detail:msg(e),life:4000})}finally{setLoading(false)}};
 useEffect(()=>{load()},[]); useEffect(()=>{if(modoInterno&&origem)axios.get('/api/estoque/enderecos',{params:{depositoId:origem}}).then(r=>setEnderecos(r.data||[])).catch(()=>setEnderecos([]));},[modoInterno,origem]); useEffect(()=>{if(modoInterno&&produto?.id&&origem)axios.get('/api/estoque/lotes',{params:{produtoId:produto.id,depositoId:origem}}).then(r=>setLotes((r.data||[]).filter(x=>x.status==='ATIVO'))).catch(()=>setLotes([]));},[modoInterno,produto,origem]);
 const search=async e=>{const q=(e.query||'').trim();if(!q)return setSug([]);try{const r=await ProdutoService.buscarPorNome(q,0,20);const d=r.data?.data?.content??r.data?.content??r.data?.data??r.data??[];setSug(Array.isArray(d)?d:[])}catch{setSug([])}};
 const add=()=>{if(!produto?.id||qtd<=0)return toast.current?.show({severity:'warn',summary:'Item',detail:t('legacyUi.transferencias.productQty'),life:3000});setItens(x=>[...x.filter(i=>i.produtoId!==produto.id),{produtoId:produto.id,produtoNome:produto.nome||produto.descricao||('#'+produto.id),quantidade:qtd}]);setProduto(null);setQtd(0)};
 const warn=(summary,detail)=>toast.current?.show({severity:'warn',summary,detail,life:3000});
 const erro=(detail)=>toast.current?.show({severity:'error',summary:'Erro',detail,life:5000});
 const ok=(summary,detail)=>toast.current?.show({severity:'success',summary,detail,life:3000});
 const transferirInterno=async()=>{
  if(!origem||!endOrigem||!endDestino||endOrigem===endDestino)return warn('WMS','Informe depósito e endereços de origem e destino diferentes');
  if(!produto?.id||qtd<=0)return warn('WMS',t('legacyUi.transferencias.productQty'));
  try{
   await axios.post('/api/estoque/transferencias/interna',{
    depositoId:origem,produtoId:produto.id,loteId:loteId||null,
    enderecoOrigemId:endOrigem,enderecoDestinoId:endDestino,
    quantidade:qtd,observacoes:obs});
   ok('Movimentado',t('legacyUi.transferencias.wmsSuccess'));
   setProduto(null);setQtd(0);setLoteId(null);setObs('');
  }catch(e){erro(msg(e));}
 };
 const transferirEntreDepositos=async()=>{
  if(!origem||!destino||origem===destino)return warn('Depósitos',t('legacyUi.transferencias.sourceTarget'));
  if(!itens.length)return warn('Itens',t('legacyUi.transferencias.noItems'));
  try{
   await axios.post('/api/estoque/transferencias',{
    depositoOrigemId:origem,depositoDestinoId:destino,
    itens:itens.map(i=>({produtoId:i.produtoId,quantidade:i.quantidade})),
    observacoes:obs});
   ok('Transferido',t('legacyUi.transferencias.success'));
   setItens([]);setObs('');
  }catch(e){erro(msg(e));}
 };
 const transfer=async()=>{
  if(modoInterno)await transferirInterno();else await transferirEntreDepositos();
  load();
 };
 const dep=id=>depositos.find(x=>x.id===id)?.nome||id;
 return <div><Toast ref={toast}/><Card title={modoInterno?"Movimentação WMS entre Endereços":"Transferência entre Depósitos"}><div className="mb-3"><Button label={modoInterno?"Voltar para transferência entre depósitos":"Movimentação interna WMS"} icon={modoInterno?"pi pi-arrow-left":"pi pi-box"} className="p-button-outlined" onClick={()=>setModoInterno(!modoInterno)}/></div>
 <div className="grid p-fluid"><div className="col-12 md:col-5 field"><label>Origem *</label><Dropdown value={origem} options={depositos.filter(d=>d.id!==destino).map(d=>({label:d.codigo+' — '+d.nome,value:d.id}))} onChange={e=>setOrigem(e.value)} placeholder={t('legacyUi.transferencias.select')}/></div>
 <div className="col-12 md:col-5 field" style={{display:modoInterno?"none":"block"}}><label>Destino *</label><Dropdown value={destino} options={depositos.filter(d=>d.id!==origem).map(d=>({label:d.codigo+' — '+d.nome,value:d.id}))} onChange={e=>setDestino(e.value)} placeholder={t('legacyUi.transferencias.select')}/></div>
 <div className="col-12 md:col-5 field" style={{display:modoInterno?"block":"none"}}><label>Endereço origem *</label><Dropdown value={endOrigem} options={enderecos.map(e=>({label:e.codigo+" — "+e.descricao,value:e.id}))} onChange={e=>setEndOrigem(e.value)} placeholder={t('legacyUi.transferencias.select')}/></div><div className="col-12 md:col-5 field" style={{display:modoInterno?"block":"none"}}><label>Endereço destino *</label><Dropdown value={endDestino} options={enderecos.filter(e=>e.id!==endOrigem).map(e=>({label:e.codigo+" — "+e.descricao,value:e.id}))} onChange={e=>setEndDestino(e.value)} placeholder={t('legacyUi.transferencias.select')}/></div><div className="col-12 md:col-2 field"><label>&nbsp;</label><Button label={t('legacyUi.transferencias.transfer')} icon="pi pi-arrow-right-arrow-left" onClick={transfer}/></div>
 <div className="col-12 md:col-5 field"><label>Produto</label><AutoComplete value={produto} suggestions={sug} completeMethod={search} field="nome" itemTemplate={p=><div><strong>{p.nome}</strong><small className="ml-2">{p.codigo||p.id}</small></div>} onChange={e=>setProduto(e.value)} placeholder={t('legacyUi.transferencias.nameOrCode')}/></div>
 <div className="col-12 md:col-2 field" style={{display:modoInterno?"block":"none"}}><label>Lote</label><Dropdown value={loteId} options={lotes} optionLabel="codigo" optionValue="id" onChange={e=>setLoteId(e.value)} placeholder="Opcional"/></div><div className="col-12 md:col-3 field"><label>Quantidade</label><InputNumber value={qtd} min={0} minFractionDigits={3} onValueChange={e=>setQtd(e.value||0)}/></div><div className="col-12 md:col-2 field"><label>&nbsp;</label><Button label="Adicionar" icon="pi pi-plus" onClick={add}/></div>
 <div className="col-12 field"><label>Observações</label><InputTextarea value={obs} rows={2} onChange={e=>setObs(e.target.value)}/></div></div>
 <DataTable value={itens} size="small" emptyMessage={t('legacyUi.transferencias.empty')}><Column field="produtoNome" header={t('legacyUi.transferencias.product')}/><Column field="quantidade" header={t('legacyUi.transferencias.quantity')}/><Column body={(_,m)=><Button icon="pi pi-trash" className="p-button-text p-button-danger" onClick={()=>setItens(x=>x.filter((__,i)=>i!==m.rowIndex))}/>} style={{width:'60px'}}/></DataTable>
 </Card><Card title={t('legacyUi.transferencias.history')} className="mt-3"><DataTable value={rows} loading={loading} paginator rows={10} size="small"><Column field="id" header="#"/><Column field="depositoOrigemId" header="Origem" body={r=>dep(r.depositoOrigemId)}/><Column field="depositoDestinoId" header="Destino" body={r=>dep(r.depositoDestinoId)}/><Column field="status" header="Status"/><Column field="dataTransferencia" header="Data"/></DataTable></Card></div>;
}