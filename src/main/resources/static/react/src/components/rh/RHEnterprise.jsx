import React,{useEffect,useState} from 'react';
import {Card} from 'primereact/card';
import {Button} from 'primereact/button';
import {InputText} from 'primereact/inputtext';
import {Dropdown} from 'primereact/dropdown';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column';
import ApiConfig,{apiFetch} from '../../services/ApiConfig';

const get=async(p)=>{const r=await apiFetch(ApiConfig.BASE_URL+p); if(!r.ok) throw new Error(await r.text()); return r.json();};
const post=async(p,b={})=>{const r=await apiFetch(ApiConfig.BASE_URL+p,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(b)});if(!r.ok)throw new Error(await r.text());return r.json();};

export default function RHEnterprise(){
 const [dash,setDash]=useState({}),[contratos,setContratos]=useState([]),[beneficios,setBeneficios]=useState([]),
 [afast,setAfast]=useState([]),[saude,setSaude]=useState([]),[trein,setTrein]=useState([]),[loading,setLoading]=useState(false),
 [competencia,setCompetencia]=useState(new Date().toISOString().slice(0,7)),[msg,setMsg]=useState('');
 const load=async()=>{setLoading(true);try{const [d,c,b,a,s,t]=await Promise.all([
   get('/api/rh/enterprise/dashboard'),get('/api/rh/enterprise/contratos'),get('/api/rh/enterprise/beneficios'),
   get('/api/rh/enterprise/afastamentos'),get('/api/rh/enterprise/saude-seguranca'),get('/api/rh/enterprise/treinamentos')]);
   setDash(d);setContratos(c);setBeneficios(b);setAfast(a);setSaude(s);setTrein(t);setMsg('');}catch(e){setMsg(e.message)}finally{setLoading(false)}};
 useEffect(()=>{load()},[]);
 const action=async(fn)=>{try{await fn();setMsg('Operação concluída');await load()}catch(e){setMsg(e.message)}};
 return <div className="page-content">
  <div className="page-header"><h1>RH Enterprise</h1><p>Ciclo integrado de pessoas, contratos, benefícios, ponto, folha, saúde, treinamento e eSocial.</p></div>
  {msg&&<div className="p-message p-message-info" style={{marginBottom:12,padding:10}}>{msg}</div>}
  <div className="bc-form-grid" style={{marginBottom:16}}>
   {[
    ['Funcionários',dash.funcionarios??0],['Contratos ativos',dash.contratosAtivos??0],['Férias programadas',dash.feriasProgramadas??0],
    ['Afastamentos abertos',dash.afastamentosAbertos??0],['eSocial pendente',dash.eventosEsocialPendentes??0]
   ].map(([k,v])=><Card key={k} title={k}><strong style={{fontSize:24}}>{v}</strong></Card>)}
  </div>
  <Card title="Operações de folha e ponto" style={{marginBottom:16}}>
   <div style={{display:'flex',gap:10,alignItems:'end',flexWrap:'wrap'}}>
    <span><label>Competência</label><InputText value={competencia} onChange={e=>setCompetencia(e.target.value)} placeholder="YYYY-MM"/></span>
    <Button label="Apurar banco de horas" icon="pi pi-clock" onClick={()=>action(()=>post('/api/rh/enterprise/banco-horas/apurar?competencia='+competencia))}/>
    <Button label="Atualizar" icon="pi pi-refresh" loading={loading} onClick={load}/>
   </div>
  </Card>
  <div className="bc-form-grid">
   <Card title="Contratos"><DataTable value={contratos} size="small" paginator rows={5}><Column field="funcionario_id" header="Funcionário"/><Column field="tipo" header="Tipo"/><Column field="inicio" header="Início"/><Column field="salario" header="Salário"/><Column field="status" header="Status"/></DataTable></Card>
   <Card title="Benefícios"><DataTable value={beneficios} size="small" paginator rows={5}><Column field="codigo" header="Código"/><Column field="descricao" header="Descrição"/><Column field="tipo" header="Tipo"/><Column field="valor_funcionario" header="Desconto"/><Column field="ativo" header="Ativo"/></DataTable></Card>
   <Card title="Afastamentos"><DataTable value={afast} size="small" paginator rows={5}><Column field="funcionario_id" header="Funcionário"/><Column field="tipo" header="Tipo"/><Column field="inicio" header="Início"/><Column field="fim" header="Fim"/><Column field="status" header="Status"/></DataTable></Card>
   <Card title="Saúde e segurança"><DataTable value={saude} size="small" paginator rows={5}><Column field="funcionario_id" header="Funcionário"/><Column field="tipo" header="Tipo"/><Column field="data_evento" header="Data"/><Column field="vencimento" header="Vencimento"/><Column field="status" header="Status"/></DataTable></Card>
   <Card title="Treinamentos"><DataTable value={trein} size="small" paginator rows={5}><Column field="codigo" header="Código"/><Column field="titulo" header="Título"/><Column field="tipo" header="Tipo"/><Column field="carga_horaria" header="Horas"/><Column field="status" header="Status"/></DataTable></Card>
  </div>
 </div>;
}
