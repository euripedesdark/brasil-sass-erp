import React,{useEffect,useState} from 'react';
import {useTranslation} from 'react-i18next';
import {TabView,TabPanel} from 'primereact/tabview';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column';
import {InputText} from 'primereact/inputtext';
import {InputNumber} from 'primereact/inputnumber';
import {Dropdown} from 'primereact/dropdown';
import {Button} from 'primereact/button';
import {api} from '../../services/ApiConfig';

function CrudTab({resource,fields,createLabel,extraLoad}) {
 const {t}=useTranslation(); const [rows,setRows]=useState([]); const [form,setForm]=useState({}); const [msg,setMsg]=useState('');
 const load=async()=>{try{const r=await api.get('/enterprise/'+resource);setRows(r.data||[])}catch(e){setMsg(e?.response?.data?.message||e.message)}};
 useEffect(()=>{load()},[]);
 const save=async()=>{try{await api.post('/enterprise/'+resource,form);setForm({});setMsg(t('enterpriseOps.saved'));load()}catch(e){setMsg(e?.response?.data?.message||e.message)}};
 return <div className="bc-module">
  <div className="bc-action-bar"><strong>{createLabel}</strong><Button label={t('common.add')} icon="pi pi-plus" onClick={save}/></div>
  <div className="bc-form-grid">{fields.map(f=><span key={f.key} className="p-float-label">
   {f.type==='number'?<InputNumber id={f.key} value={form[f.key]??null} onValueChange={e=>setForm({...form,[f.key]:e.value})}/>:f.type==='select'?<Dropdown id={f.key} value={form[f.key]??null} options={f.options} optionLabel="label" optionValue="value" onChange={e=>setForm({...form,[f.key]:e.value})}/>:<InputText id={f.key} value={form[f.key]??''} onChange={e=>setForm({...form,[f.key]:e.target.value})}/>}
   <label htmlFor={f.key}>{f.label}</label></span>)}</div>
  {msg&&<small>{msg}</small>}
  <DataTable value={rows} paginator rows={10} size="small" emptyMessage={t('enterpriseOps.empty')}><Column field="codigo" header={t('enterpriseOps.code')}/><Column field="descricao" header={t('enterpriseOps.description')}/><Column field="status" header={t('enterpriseOps.status')}/><Column field="nivel" header={t('enterpriseOps.level')}/><Column field="numero" header={t('enterpriseOps.number')}/></DataTable>
 </div>
}

export default function RiskTransportEnterprise(){
 const {t}=useTranslation();
 const status=[{label:'ABERTO',value:'ABERTO'},{label:'EM_TRATAMENTO',value:'EM_TRATAMENTO'},{label:'CONCLUIDO',value:'CONCLUIDO'}];
 const result=[{label:'PENDENTE',value:'PENDENTE'},{label:'APROVADO',value:'APROVADO'},{label:'REPROVADO',value:'REPROVADO'}];
 return <div className="page-content"><h1>{t('enterpriseOps.title')}</h1>
  <TabView>
   <TabPanel header={t('enterpriseOps.risks')}><CrudTab resource="grc/riscos" createLabel={t('enterpriseOps.newRisk')} fields={[
    {key:'codigo',label:t('enterpriseOps.code')},{key:'descricao',label:t('enterpriseOps.description')},{key:'categoria',label:t('enterpriseOps.category')},
    {key:'probabilidade',label:t('enterpriseOps.probability'),type:'number'},{key:'impacto',label:t('enterpriseOps.impact'),type:'number'},
    {key:'status',label:t('enterpriseOps.status'),type:'select',options:status},{key:'responsavel',label:t('enterpriseOps.owner')}]}/></TabPanel>
   <TabPanel header={t('enterpriseOps.controls')}><CrudTab resource="grc/controles" createLabel={t('enterpriseOps.newControl')} fields={[
    {key:'codigo',label:t('enterpriseOps.code')},{key:'descricao',label:t('enterpriseOps.description')},{key:'tipo',label:t('enterpriseOps.controlType')},
    {key:'frequencia',label:t('enterpriseOps.frequency')},{key:'responsavel',label:t('enterpriseOps.owner')}]}/></TabPanel>
   <TabPanel header={t('enterpriseOps.controlTests')}><CrudTab resource="grc/testes" createLabel={t('enterpriseOps.newTest')} fields={[
    {key:'controleId',label:t('enterpriseOps.controlId'),type:'number'},{key:'periodo',label:t('enterpriseOps.period')},
    {key:'resultado',label:t('enterpriseOps.result'),type:'select',options:result},{key:'observacao',label:t('enterpriseOps.notes')},{key:'testadoPor',label:t('enterpriseOps.tester')}]}/></TabPanel>
   <TabPanel header={t('enterpriseOps.transportOrders')}><CrudTab resource="tms/ordens" createLabel={t('enterpriseOps.newTransport')} fields={[
    {key:'numero',label:t('enterpriseOps.number')},{key:'origem',label:t('enterpriseOps.origin')},{key:'destino',label:t('enterpriseOps.destination')},
    {key:'modalidade',label:t('enterpriseOps.mode')},{key:'peso',label:t('enterpriseOps.weight'),type:'number'},{key:'volume',label:t('enterpriseOps.volume'),type:'number'},
    {key:'fretePrevisto',label:t('enterpriseOps.plannedFreight'),type:'number'}]}/></TabPanel>
   <TabPanel header={t('enterpriseOps.tracking')}><CrudTab resource="tms/tracking" createLabel={t('enterpriseOps.newTracking')} fields={[
    {key:'ordemId',label:t('enterpriseOps.orderId'),type:'number'},{key:'codigo',label:t('enterpriseOps.trackingCode')},{key:'transportadora',label:t('enterpriseOps.carrier')},{key:'ultimoStatus',label:t('enterpriseOps.status')}]}/></TabPanel>
   <TabPanel header={t('enterpriseOps.freight')}><CrudTab resource="tms/fretes" createLabel={t('enterpriseOps.newFreight')} fields={[
    {key:'ordemId',label:t('enterpriseOps.orderId'),type:'number'},{key:'componente',label:t('enterpriseOps.component')},{key:'valor',label:t('enterpriseOps.value'),type:'number'},{key:'documento',label:t('enterpriseOps.document')}]}/></TabPanel>
  </TabView>
 </div>
}
