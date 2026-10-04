import React,{useEffect,useState} from 'react';
import {Card} from 'primereact/card';
import {TabView,TabPanel} from 'primereact/tabview';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column';
import {Button} from 'primereact/button';
import {Dialog} from 'primereact/dialog';
import {InputText} from 'primereact/inputtext';
import {InputTextarea} from 'primereact/inputtextarea';
import {InputNumber} from 'primereact/inputnumber';
import {Dropdown} from 'primereact/dropdown';
import {Calendar} from 'primereact/calendar';
import {apiFetch} from '../../services/ApiConfig';

export default function Ativos(){
 const [ativos,setAtivos]=useState([]),[man,setMan]=useState([]),[dialog,setDialog]=useState(false),[form,setForm]=useState({}),[tab,setTab]=useState(0),[loading,setLoading]=useState(false),[dep,setDep]=useState(null);
 const load=async()=>{setLoading(true);try{setAtivos(await apiFetch('/api/ativos').then(r=>r.json()));setMan(await apiFetch('/api/ativos/manutencoes').then(r=>r.json()))}finally{setLoading(false)}};
 useEffect(()=>{load()},[]);
 const save=async()=>{const url=tab===0?'/api/ativos':'/api/ativos/manutencoes';await apiFetch(url,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(form)});setDialog(false);setForm({});load()};
 return <div className="p-3"><Card title="Ativos e Manutenção" subTitle="Ciclo de vida do ativo, depreciação e manutenção">
 <TabView activeIndex={tab} onTabChange={e=>setTab(e.index)}>
  <TabPanel header="Ativos"><div className="flex justify-content-end mb-3"><Button label="Novo ativo" icon="pi pi-plus" onClick={()=>{setForm({valorAquisicao:0,valorResidual:0,valorDepreciado:0,status:'ATIVO'});setDialog(true)}}/></div><DataTable value={ativos} loading={loading} paginator rows={10} responsiveLayout="scroll"><Column field="codigo" header="Código" sortable/><Column field="descricao" header="Descrição" sortable/><Column field="classe" header="Classe"/><Column field="localizacao" header="Localização"/><Column field="valorAquisicao" header="Aquisição"/><Column field="valorDepreciado" header="Depreciado"/><Column field="status" header="Status"/><Column header="Ações" body={r=><div className="flex gap-1"><Button icon="pi pi-chart-line" text tooltip="Calcular depreciação" onClick={async()=>{const x=await apiFetch('/api/ativos/'+r.id+'/depreciacao');setDep(x.ok?await x.json():null)}}/><Button icon="pi pi-calculator" text tooltip="Depreciar" disabled={r.status==='BAIXADO'} onClick={async()=>{await apiFetch('/api/ativos/'+r.id+'/depreciar',{method:'POST'});load()}}/><Button icon="pi pi-times" text tooltip="Baixar" disabled={r.status==='BAIXADO'} onClick={async()=>{await apiFetch('/api/ativos/'+r.id+'/baixar',{method:'POST'});load()}}/></div>}/></DataTable></TabPanel>
  <TabPanel header="Manutenção"><div className="flex justify-content-end mb-3"><Button label="Nova ordem" icon="pi pi-plus" onClick={()=>{setForm({tipo:'CORRETIVA',status:'ABERTA',prioridade:'MEDIA',custo:0});setDialog(true)}}/></div><DataTable value={man} loading={loading} paginator rows={10} responsiveLayout="scroll"><Column field="numero" header="Número"/><Column field="ativoId" header="Ativo"/><Column field="tipo" header="Tipo"/><Column field="prioridade" header="Prioridade"/><Column field="status" header="Status"/><Column field="dataProgramada" header="Programada"/><Column field="custo" header="Custo"/><Column header="Ação" body={r=><Button icon="pi pi-check" text disabled={r.status==='CONCLUIDA'} onClick={async()=>{await apiFetch('/api/ativos/manutencoes/'+r.id+'/concluir',{method:'POST'});load()}}/>}/></DataTable></TabPanel>
 </TabView>
 <Dialog header="Depreciação" visible={!!dep} onHide={()=>setDep(null)} modal style={{width:'min(560px,92vw)'}}>
  {dep && <div className="grid">
   <div className="col-12"><strong>{dep.codigo}</strong> — {dep.ativoId}</div>
   <div className="col-6">Meses decorridos: <strong>{dep.mesesDecorridos}</strong></div>
   <div className="col-6">Vida útil: <strong>{dep.mesesDepreciaveis}</strong></div>
   <div className="col-6">Depreciação mensal: <strong>{Number(dep.depreciacaoMensal||0).toLocaleString('pt-BR',{style:'currency',currency:'BRL'})}</strong></div>
   <div className="col-6">Acumulada: <strong>{Number(dep.depreciacaoAcumuladaCalculada||0).toLocaleString('pt-BR',{style:'currency',currency:'BRL'})}</strong></div>
   <div className="col-12">Valor contábil calculado: <strong>{Number(dep.valorContabilCalculado||0).toLocaleString('pt-BR',{style:'currency',currency:'BRL'})}</strong></div>
  </div>}
 </Dialog>
 <Dialog header={tab===0?'Cadastro de ativo':'Ordem de manutenção'} visible={dialog} onHide={()=>setDialog(false)} modal style={{width:'min(720px,92vw)'}}>
  <div className="grid p-fluid">{tab===0?<><div className="col-12 md:col-4"><label>Código</label><InputText value={form.codigo||''} onChange={e=>setForm({...form,codigo:e.target.value})}/></div><div className="col-12 md:col-8"><label>Descrição</label><InputText value={form.descricao||''} onChange={e=>setForm({...form,descricao:e.target.value})}/></div><div className="col-12 md:col-6"><label>Classe</label><InputText value={form.classe||''} onChange={e=>setForm({...form,classe:e.target.value})}/></div><div className="col-12 md:col-6"><label>Número de série</label><InputText value={form.numeroSerie||''} onChange={e=>setForm({...form,numeroSerie:e.target.value})}/></div><div className="col-12"><label>Localização</label><InputText value={form.localizacao||''} onChange={e=>setForm({...form,localizacao:e.target.value})}/></div><div className="col-12 md:col-4"><label>Valor de aquisição</label><InputNumber value={form.valorAquisicao} onValueChange={e=>setForm({...form,valorAquisicao:e.value})} mode="currency" currency="BRL" locale="pt-BR"/></div><div className="col-12 md:col-4"><label>Valor residual</label><InputNumber value={form.valorResidual} onValueChange={e=>setForm({...form,valorResidual:e.value})} mode="currency" currency="BRL" locale="pt-BR"/></div><div className="col-12 md:col-4"><label>Vida útil (meses)</label><InputNumber value={form.vidaUtilMeses} onValueChange={e=>setForm({...form,vidaUtilMeses:e.value})}/></div></>:<><div className="col-12 md:col-4"><label>Número</label><InputText value={form.numero||''} onChange={e=>setForm({...form,numero:e.target.value})}/></div><div className="col-12 md:col-4"><label>Ativo ID</label><InputNumber value={form.ativoId} onValueChange={e=>setForm({...form,ativoId:e.value})}/></div><div className="col-12 md:col-4"><label>Tipo</label><Dropdown value={form.tipo} options={['PREVENTIVA','CORRETIVA','PREDITIVA']} onChange={e=>setForm({...form,tipo:e.value})}/></div><div className="col-12"><label>Descrição</label><InputTextarea rows={5} value={form.descricao||''} onChange={e=>setForm({...form,descricao:e.target.value})}/></div><div className="col-12 md:col-6"><label>Custo</label><InputNumber value={form.custo} onValueChange={e=>setForm({...form,custo:e.value})} mode="currency" currency="BRL" locale="pt-BR"/></div></>}</div>
  <div className="flex justify-content-end mt-3"><Button label="Salvar" icon="pi pi-save" onClick={save}/></div>
 </Dialog>
 </Card></div>
}
