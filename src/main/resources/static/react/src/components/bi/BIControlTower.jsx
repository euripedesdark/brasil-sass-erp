import React,{useEffect,useState} from 'react';
import {Card} from 'primereact/card';
import {Button} from 'primereact/button';
import {InputText} from 'primereact/inputtext';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column';
import ApiConfig,{apiFetch} from '../../services/ApiConfig';

const req=async(p,o)=>{const r=await apiFetch(ApiConfig.BASE_URL+p,o);if(!r.ok)throw new Error(await r.text());return r.json();};
export default function BIControlTower(){
 const [tower,setTower]=useState({}),[etl,setEtl]=useState([]),[metrics,setMetrics]=useState([]),[sql,setSql]=useState(''),[rows,setRows]=useState([]),[msg,setMsg]=useState('');
 const load=async()=>{try{const [t,e,m]=await Promise.all([req('/api/bi/enterprise/control-tower'),req('/api/bi/enterprise/etl/execucoes'),req('/api/bi/enterprise/metricas')]);setTower(t);setEtl(e);setMetrics(m);setMsg('')}catch(e){setMsg(e.message)}};
 useEffect(()=>{load()},[]);
 const run=async(path)=>{try{await req(path,{method:'POST'});setMsg('ETL concluído');load()}catch(e){setMsg(e.message)}};
 const consulta=async()=>{try{setRows(await req('/api/bi/enterprise/consulta',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({sql})}));setMsg('Consulta executada')}catch(e){setMsg(e.message)}};
 return <div className="page-content">
  <div className="page-header"><h1>BI & Control Tower</h1><p>Data warehouse operacional, ETL, métricas corporativas e consultas analíticas.</p></div>
  {msg&&<div className="p-message p-message-info" style={{padding:10,marginBottom:12}}>{msg}</div>}
  <div className="bc-form-grid" style={{marginBottom:16}}>
   {Object.entries({Vendas:tower.vendas,Margem:tower.margem,'A receber':tower.receber,'A pagar':tower.pagar,Folha:tower.folha,Produção:tower.producao}).map(([k,v])=><Card key={k} title={k}><strong style={{fontSize:22}}>{Number(v||0).toLocaleString('pt-BR',{minimumFractionDigits:2})}</strong></Card>)}
  </div>
  <Card title="Pipeline DW / ETL" style={{marginBottom:16}}>
   <div style={{display:'flex',gap:10,flexWrap:'wrap'}}>
    <Button label="Popular dimensão tempo" icon="pi pi-calendar" onClick={()=>run('/api/bi/enterprise/dw/dimensao-tempo')}/>
    <Button label="ETL RH" icon="pi pi-users" onClick={()=>run('/api/bi/enterprise/etl/rh?competencia='+new Date().toISOString().slice(0,7))}/>
    <Button label="Atualizar" icon="pi pi-refresh" onClick={load}/>
   </div>
   <DataTable value={etl} size="small" paginator rows={8} style={{marginTop:12}}><Column field="processo" header="Processo"/><Column field="status" header="Status"/><Column field="registros" header="Registros"/><Column field="inicio" header="Início"/><Column field="fim" header="Fim"/></DataTable>
  </Card>
  <div className="bc-form-grid">
   <Card title="Métricas corporativas"><DataTable value={metrics} size="small" paginator rows={8}><Column field="codigo" header="Código"/><Column field="nome" header="Nome"/><Column field="categoria" header="Categoria"/><Column field="meta" header="Meta"/><Column field="ativo" header="Ativo"/></DataTable></Card>
   <Card title="Consulta analítica (somente SELECT)">
    <InputText value={sql} onChange={e=>setSql(e.target.value)} placeholder="SELECT ..." style={{width:'100%',marginBottom:10}}/>
    <Button label="Executar" icon="pi pi-play" onClick={consulta}/>
    <DataTable value={rows} size="small" paginator rows={8} scrollable style={{marginTop:12}}>
      {rows.length>0&&Object.keys(rows[0]).map(k=><Column key={k} field={k} header={k}/>)}
    </DataTable>
   </Card>
  </div>
 </div>;
}
