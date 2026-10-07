import React,{useEffect,useState} from 'react';
import {Card} from 'primereact/card';
import {Button} from 'primereact/button';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column';
import {InputText} from 'primereact/inputtext';
import {InputNumber} from 'primereact/inputnumber';
import {api} from '../../services/ApiConfig';

export default function SupplyChainControlTower(){
 const [demanda,setDemanda]=useState([]),[atp,setAtp]=useState([]),[rotas,setRotas]=useState([]),[msg,setMsg]=useState('');
 const [produtoId,setProdutoId]=useState(null),[data,setData]=useState(new Date().toISOString().slice(0,10));
 const load=async()=>{try{const [d,a,r]=await Promise.all([api.get('/supply-chain/enterprise/demanda'),api.get('/supply-chain/enterprise/atp'),api.get('/supply-chain/enterprise/rotas')]);setDemanda(d.data);setAtp(a.data);setRotas(r.data);setMsg('')}catch(e){setMsg(e?.response?.data?.message||e.message)}};
 useEffect(()=>{load()},[]);
 const calc=async()=>{try{await api.post('/supply-chain/enterprise/atp/calcular',null,{params:{produtoId,data}});setMsg('ATP/CTP recalculado');load()}catch(e){setMsg(e?.response?.data?.message||e.message)}};
 return <div className="page-content">
  <div className="page-header"><h1>Supply Chain / TMS Enterprise</h1><p>Planejamento de demanda, ATP/CTP, rotas, cargas, tracking e conferência de frete.</p></div>
  {msg&&<div className="p-message p-message-info" style={{padding:10,marginBottom:12}}>{msg}</div>}
  <Card title="ATP / CTP" style={{marginBottom:16}}><div style={{display:'flex',gap:10,alignItems:'end',flexWrap:'wrap'}}>
   <span><label>Produto ID</label><InputNumber value={produtoId} onValueChange={e=>setProdutoId(e.value)}/></span>
   <span><label>Data</label><InputText value={data} onChange={e=>setData(e.target.value)}/></span>
   <Button label="Calcular disponibilidade" icon="pi pi-calculator" disabled={!produtoId} onClick={calc}/>
   <Button label="Atualizar" icon="pi pi-refresh" onClick={load}/>
  </div></Card>
  <div className="bc-form-grid">
   <Card title="Disponibilidade ATP/CTP"><DataTable value={atp} paginator rows={10} size="small"><Column field="produto_id" header="Produto"/><Column field="data" header="Data"/><Column field="estoque_disponivel" header="Estoque"/><Column field="reservas" header="Reservas"/><Column field="quantidade_atp" header="ATP"/><Column field="quantidade_ctp" header="CTP"/></DataTable></Card>
   <Card title="Demanda / planejamento"><DataTable value={demanda} paginator rows={10} size="small"><Column field="produto_id" header="Produto"/><Column field="periodo" header="Período"/><Column field="tipo" header="Tipo"/><Column field="quantidade" header="Quantidade"/><Column field="confianca" header="Confiança"/></DataTable></Card>
   <Card title="Rotas TMS"><DataTable value={rotas} paginator rows={10} size="small"><Column field="codigo" header="Código"/><Column field="origem" header="Origem"/><Column field="destino" header="Destino"/><Column field="distancia_km" header="Km"/><Column field="tempo_minutos" header="Minutos"/><Column field="custo_base" header="Base"/><Column field="pedagio" header="Pedágio"/></DataTable></Card>
  </div>
 </div>
}