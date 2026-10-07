import React,{useEffect,useState} from 'react';
import {Card} from 'primereact/card';
import {Button} from 'primereact/button';
import {InputNumber} from 'primereact/inputnumber';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column';
import {api} from '../../services/ApiConfig';

export default function ContabilidadeEnterprise(){
 const [regras,setRegras]=useState([]),[bal,setBal]=useState([]),[dre,setDre]=useState([]),[periodo,setPeriodo]=useState(''),[msg,setMsg]=useState('');
 const load=async()=>{try{setRegras((await api.get('/contabilidade/enterprise/regras')).data);if(periodo){setBal((await api.get('/contabilidade/enterprise/balancete',{params:{periodoId:periodo}})).data);setDre((await api.get('/contabilidade/enterprise/dre',{params:{periodoId:periodo}})).data)}}catch(e){setMsg(e?.response?.data?.message||e.message)}};
 useEffect(()=>{load()},[periodo]);
 const close=async()=>{try{await api.post('/contabilidade/enterprise/periodos/'+periodo+'/fechar');setMsg('Período contábil fechado.')}catch(e){setMsg(e?.response?.data?.message||e.message)}};
 return <div className="page-content">
  <div className="page-header"><h1>Contabilidade Enterprise / Close</h1><p>Partidas, validação, rateios, balancete, DRE e fechamento contábil.</p></div>
  {msg&&<div className="p-message p-message-info" style={{padding:10,marginBottom:12}}>{msg}</div>}
  <Card title="Fechamento e relatórios" style={{marginBottom:16}}><div style={{display:'flex',gap:10,alignItems:'end',flexWrap:'wrap'}}>
   <span><label>Período ID</label><InputNumber value={periodo?Number(periodo):null} onValueChange={e=>setPeriodo(e.value?String(e.value):'')} /></span>
   <Button label="Fechar período" icon="pi pi-lock" severity="warning" disabled={!periodo} onClick={close}/>
   <Button label="Atualizar" icon="pi pi-refresh" onClick={load}/>
  </div></Card>
  <div className="bc-form-grid">
   <Card title="Regras de lançamento"><DataTable value={regras} paginator rows={8} size="small"><Column field="codigo" header="Código"/><Column field="nome" header="Nome"/><Column field="origem" header="Origem"/><Column field="ativo" header="Ativo"/></DataTable></Card>
   <Card title="Balancete"><DataTable value={bal} paginator rows={10} size="small"><Column field="codigo" header="Conta"/><Column field="descricao" header="Descrição"/><Column field="debito" header="Débito"/><Column field="credito" header="Crédito"/><Column field="saldo" header="Saldo"/></DataTable></Card>
   <Card title="DRE"><DataTable value={dre} paginator rows={10} size="small"><Column field="codigo" header="Conta"/><Column field="descricao" header="Descrição"/><Column field="saldo" header="Saldo"/></DataTable></Card>
  </div>
 </div>
}