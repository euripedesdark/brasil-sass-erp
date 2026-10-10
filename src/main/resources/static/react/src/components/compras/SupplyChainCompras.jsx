
const QuickLinksCompras = () => (
  <div className="flex flex-wrap gap-2 mb-3">
    <a className="p-button p-component p-button-outlined p-button-sm" href="#/compras/contratos">Contratos de fornecimento</a>
    <a className="p-button p-component p-button-outlined p-button-sm" href="#/compras">Pedidos</a>
    <a className="p-button p-component p-button-outlined p-button-sm" href="#/compras/conferencia-faturas">3-way match</a>
  </div>
);
import { useTranslation } from 'react-i18next';
import React, { useNavigate, useEffect, useState } from 'react';
import {Card} from 'primereact/card';import {DataTable} from 'primereact/datatable';import {Column} from 'primereact/column';import {Button} from 'primereact/button';import {InputNumber} from 'primereact/inputnumber';import {InputText} from 'primereact/inputtext';import {Calendar} from 'primereact/calendar';import {Message} from 'primereact/message';import ComprasSupplyChainService from '../../services/ComprasSupplyChainService';import {useAuth} from '../../contexts/AuthContext';
import { formatoData } from '../shared/LocaleData.js';

export default function SupplyChainCompras(){
    const { t } = useTranslation();
 const {user}=useAuth();const empresaId=user?.empresaId;const [lista,setLista]=useState([]);const [cotacaoId,setCotacaoId]=useState(null);const [mapa,setMapa]=useState([]);const [form,setForm]=useState({numero:'',dataNecessidade:null,observacao:'',produtoId:null,quantidade:1});const [fornecedores,setFornecedores]=useState('');const [precos,setPrecos]=useState('');const [error,setError]=useState('');
 const load=async()=>{try{const r=await ComprasSupplyChainService.listarSolicitacoes();setLista(r.data?.data??r.data??[]);}catch(e){setError(e.response?.data?.errors?.[0]?.message||e.response?.data?.message||'Não foi possível carregar as solicitações');}};useEffect(()=>{load()},[empresaId]);
 const criar=async()=>{try{setError('');await ComprasSupplyChainService.criarSolicitacao(null,{numero:form.numero||null,dataNecessidade:form.dataNecessidade?new Date(form.dataNecessidade).toISOString().slice(0,10):null,observacao:form.observacao,itens:[{produtoId:form.produtoId,quantidade:form.quantidade}]});setForm({numero:'',dataNecessidade:null,observacao:'',produtoId:null,quantidade:1});load()}catch(e){setError(e.response?.data?.errors?.[0]?.message||e.response?.data?.message||'Não foi possível criar a solicitação')}};
 const aprovar=async(id)=>{try{setError('');await ComprasSupplyChainService.aprovar(id);await load();}catch(e){setError(e.response?.data?.message||'Não foi possível aprovar a solicitação')}};
const rejeitar=async(id)=>{try{setError('');await ComprasSupplyChainService.rejeitar(id);await load();}catch(e){setError(e.response?.data?.message||'Não foi possível rejeitar a solicitação')}};
 const cotar=async(s)=>{try{const fs=String(fornecedores).split(',').map(x=>Number(x.trim())).filter(Boolean);const ps=String(precos).split(',').map(x=>Number(x.trim()));if(!fs.length||!s.itens?.length)throw Error();const itens=s.itens.map((i,n)=>({produtoId:i.produtoId,quantidade:i.quantidade,valorUnitario:ps[n]||0}));const r=await ComprasSupplyChainService.criarCotacao(s.id,{numero:null,dataLimite:null,observacao:'Cotação criada pelo mapa de compras',fornecedores:fs.map(f=>({fornecedorId:f,prazoEntrega:7,condicaoPagamentoId:null,frete:0,desconto:0,itens}))});setCotacaoId(r.data?.data?.id??r.data?.id)}catch(e){setError('Informe fornecedores como IDs separados por vírgula e preços dos itens na mesma ordem.')}};
 const abrirMapa=async()=>{if(!cotacaoId)return;const r=await ComprasSupplyChainService.mapa(cotacaoId);setMapa(r.data?.data??r.data??[])};
 const gerar=async(id)=>{try{setError('');await ComprasSupplyChainService.gerarPedido(id);await load();}catch(e){setError(e.response?.data?.message||'Não foi possível gerar o pedido')}};
 return <div className="grid"><div className="col-12"><QuickLinksCompras /></div>
  <div className="col-12"><Card title="Solicitação → Cotação → Pedido"><p className="text-color-secondary">Fluxo de suprimentos operacional: solicitação interna, aprovação, cotação multi-fornecedor, mapa comparativo e geração do pedido.</p>{error&&<Message severity="error" text={error} className="w-full mb-3"/>}</Card></div>
  <div className="col-12 md:col-5"><Card title="Nova solicitação"><div className="grid p-fluid">
   <div className="col-6"><label>Número</label><InputText value={form.numero} onChange={e=>setForm({...form,numero:e.target.value})}/></div>
   <div className="col-6"><label>Produto ID</label><InputNumber value={form.produtoId} onValueChange={e=>setForm({...form,produtoId:e.value})}/></div>
   <div className="col-6"><label>Quantidade</label><InputNumber value={form.quantidade} min={0.001} onValueChange={e=>setForm({...form,quantidade:e.value})}/></div>
   <div className="col-6"><label>Necessidade</label><Calendar value={form.dataNecessidade} onChange={e=>setForm({...form,dataNecessidade:e.value})} dateFormat={formatoData()} showIcon/></div>
   <div className="col-12"><label>Observação</label><InputText value={form.observacao} onChange={e=>setForm({...form,observacao:e.target.value})}/></div>
   <div className="col-12"><Button label="Enviar para aprovação" icon="pi pi-send" onClick={criar}/></div>
  </div></Card></div>
  <div className="col-12 md:col-7"><Card title="Solicitações"><DataTable value={lista} size="small"><Column field="numero" header="Número"/><Column field="status" header="Status"/><Column field="dataNecessidade" header="Necessidade"/><Column body={s=><div className="flex gap-2">{s.status==='PENDENTE_APROVACAO'&&<><Button label="Aprovar" size="small" onClick={()=>aprovar(s.id)}/><Button label="Rejeitar" size="small" severity="danger" outlined onClick={()=>rejeitar(s.id)}/></>} {s.status==='APROVADA'&&<Button label="Cotação" size="small" onClick={()=>cotar(s)}/>}</div>}/></DataTable></Card></div>
  <div className="col-12"><Card title="Cotação e mapa comparativo"><div className="grid p-fluid"><div className="col-12 md:col-3"><label>ID cotação</label><InputNumber value={cotacaoId} onValueChange={e=>setCotacaoId(e.value)}/></div><div className="col-12 md:col-4"><label>Fornecedores (IDs)</label><InputText value={fornecedores} onChange={e=>setFornecedores(e.target.value)} placeholder="10,11,12"/></div><div className="col-12 md:col-5"><label>Preços por item</label><InputText value={precos} onChange={e=>setPrecos(e.target.value)} placeholder="100,50,25"/></div><div className="col-12"><Button label="Atualizar mapa" icon="pi pi-table" onClick={abrirMapa}/></div></div><DataTable value={mapa} size="small" className="mt-3"><Column field="cotacaoFornecedorId" header="Cotação fornecedor"/><Column field="fornecedorId" header="Fornecedor"/><Column field="prazoEntrega" header="Prazo (dias)"/><Column field="frete" header="Frete"/><Column field="desconto" header="Desconto"/><Column field="valorTotal" header="Total"/><Column header="Pontos" body={(r)=>(<b>{r.pontuacao}{r.vencedor ? '★' : ''}</b>)} style={{ width: "7rem" }} /><Column body={r=><Button label="Gerar pedido" size="small" onClick={()=>gerar(r.cotacaoFornecedorId)} disabled={r.status==='SELECIONADA'}/>} /></DataTable></Card></div>
 </div>
}