import React,{useEffect,useState} from 'react';
import {Card} from 'primereact/card';
import {TabView,TabPanel} from 'primereact/tabview';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column';
import {Button} from 'primereact/button';
import {Dialog} from 'primereact/dialog';
import {InputText} from 'primereact/inputtext';
import {InputTextarea} from 'primereact/inputtextarea';
import {Dropdown} from 'primereact/dropdown';
import {Calendar} from 'primereact/calendar';
import {apiFetch} from '../../services/ApiConfig';

export default function Qualidade(){
 const [tab,setTab]=useState(0),[planos,setPlanos]=useState([]),[inspecoes,setInspecoes]=useState([]),[ncs,setNcs]=useState([]),[dialog,setDialog]=useState(null),[loading,setLoading]=useState(false);
 const [form,setForm]=useState({});
 const load=async()=>{setLoading(true);try{const [a,b,c]=await Promise.all(['/api/qualidade/planos','/api/qualidade/inspecoes','/api/qualidade/nao-conformidades'].map(x=>apiFetch(x).then(r=>r.json())));setPlanos(a);setInspecoes(b);setNcs(c)}finally{setLoading(false)}};
 useEffect(()=>{load()},[]);
 const save=async()=>{
  const map={plano:'/api/qualidade/planos',inspecao:'/api/qualidade/inspecoes',nc:'/api/qualidade/nao-conformidades'};
  await apiFetch(map[dialog],{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(form)});setDialog(null);setForm({});load();
 };
 const footer=<div><Button label="Cancelar" text onClick={()=>setDialog(null)}/><Button label="Salvar" icon="pi pi-save" onClick={save}/></div>;
 return <div className="p-3"><Card title="Gestão da Qualidade" subTitle="Planejamento, inspeção, não conformidade e ações corretivas">
 <TabView activeIndex={tab} onTabChange={e=>setTab(e.index)}>
  <TabPanel header="Planos de inspeção"><div className="flex justify-content-end mb-3"><Button label="Novo plano" icon="pi pi-plus" onClick={()=>{setForm({tipo:'RECEBIMENTO',ativo:true});setDialog('plano')}}/></div><DataTable value={planos} loading={loading} paginator rows={10} responsiveLayout="scroll"><Column field="codigo" header="Código" sortable/><Column field="descricao" header="Descrição" sortable/><Column field="tipo" header="Tipo"/><Column field="ativo" header="Ativo"/></DataTable></TabPanel>
  <TabPanel header="Inspeções"><div className="flex justify-content-end mb-3"><Button label="Nova inspeção" icon="pi pi-plus" onClick={()=>{setForm({dataInspecao:new Date().toISOString().slice(0,10),status:'ABERTA',referenciaTipo:'RECEBIMENTO'});setDialog('inspecao')}}/></div><DataTable value={inspecoes} loading={loading} paginator rows={10} responsiveLayout="scroll"><Column field="numero" header="Número"/><Column field="referenciaTipo" header="Origem"/><Column field="dataInspecao" header="Data"/><Column field="status" header="Status"/><Column field="resultado" header="Resultado"/></DataTable></TabPanel>
  <TabPanel header="Não conformidades"><div className="flex justify-content-end mb-3"><Button label="Nova NC" icon="pi pi-plus" onClick={()=>{setForm({severidade:'MEDIA',status:'ABERTA'});setDialog('nc')}}/></div><DataTable value={ncs} loading={loading} paginator rows={10} responsiveLayout="scroll"><Column field="numero" header="Número"/><Column field="severidade" header="Severidade"/><Column field="status" header="Status"/><Column field="descricao" header="Descrição"/><Column field="prazo" header="Prazo"/><Column header="Ação" body={r=><Button icon="pi pi-check" text disabled={r.status==='ENCERRADA'} onClick={async()=>{await apiFetch('/api/qualidade/nao-conformidades/'+r.id+'/encerrar',{method:'POST'});load()}}/>}/></DataTable></TabPanel>
 </TabView>
 <Dialog header={dialog==='plano'?'Plano de inspeção':dialog==='inspecao'?'Inspeção':'Não conformidade'} visible={!!dialog} onHide={()=>setDialog(null)} footer={footer} modal style={{width:'min(680px,92vw)'}}>
  {dialog==='plano'&&<div className="grid p-fluid"><div className="col-12 md:col-4"><label>Código</label><InputText value={form.codigo||''} onChange={e=>setForm({...form,codigo:e.target.value})}/></div><div className="col-12 md:col-8"><label>Descrição</label><InputText value={form.descricao||''} onChange={e=>setForm({...form,descricao:e.target.value})}/></div><div className="col-12"><label>Tipo</label><Dropdown value={form.tipo} options={['RECEBIMENTO','PROCESSO','FINAL']} onChange={e=>setForm({...form,tipo:e.value})}/></div></div>}
  {dialog==='inspecao'&&<div className="grid p-fluid"><div className="col-12 md:col-6"><label>Número</label><InputText value={form.numero||''} onChange={e=>setForm({...form,numero:e.target.value})}/></div><div className="col-12 md:col-6"><label>Tipo de referência</label><Dropdown value={form.referenciaTipo} options={['RECEBIMENTO','PRODUCAO','EXPEDICAO','OUTRO']} onChange={e=>setForm({...form,referenciaTipo:e.value})}/></div><div className="col-12"><label>Observação</label><InputTextarea rows={4} value={form.observacao||''} onChange={e=>setForm({...form,observacao:e.target.value})}/></div></div>}
  {dialog==='nc'&&<div className="grid p-fluid"><div className="col-12 md:col-6"><label>Número</label><InputText value={form.numero||''} onChange={e=>setForm({...form,numero:e.target.value})}/></div><div className="col-12 md:col-6"><label>Severidade</label><Dropdown value={form.severidade} options={['BAIXA','MEDIA','ALTA','CRITICA']} onChange={e=>setForm({...form,severidade:e.value})}/></div><div className="col-12"><label>Descrição</label><InputTextarea rows={5} value={form.descricao||''} onChange={e=>setForm({...form,descricao:e.target.value})}/></div><div className="col-12"><label>Causa raiz</label><InputTextarea rows={3} value={form.causaRaiz||''} onChange={e=>setForm({...form,causaRaiz:e.target.value})}/></div></div>}
 </Dialog>
 </Card></div>
}
