import React, { useEffect, useState } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { AutoComplete } from 'primereact/autocomplete';
import { Calendar } from 'primereact/calendar';
import { Dropdown } from 'primereact/dropdown';
import { Message } from 'primereact/message';
import { Toast } from 'primereact/toast';
import { useRef } from 'react';
import ProdutoService from '../../services/ProdutoService';
import { apiFetch } from '../../services/ApiConfig';

const vazioRoteiro = () => ({ produtoId:null, produto:null, codigo:'', nome:'', versao:1, vigenciaInicio:null, vigenciaFim:null, ativo:true, observacao:'' });
const vazioOperacao = () => ({ sequencia:1, codigo:'', nome:'', centroTrabalhoId:null, setupMinutos:0, maquinaMinutos:0, homemMinutos:0, instrucoes:'', ativo:true });
const lista = async (r) => { const d=await r.json(); return d?.data ?? d ?? []; };

export default function Roteiros(){
 const toast=useRef(null);
 const [roteiros,setRoteiros]=useState([]),[centros,setCentros]=useState([]),[produtos,setProdutos]=useState([]);
 const [produtoSug,setProdutoSug]=useState([]),[loading,setLoading]=useState(false),[erro,setErro]=useState('');
 const [roteiro,setRoteiro]=useState(vazioRoteiro()),[dialog,setDialog]=useState(false);
 const [centro,setCentro]=useState({codigo:'',nome:'',capacidadeHorasDia:8,ativo:true}),[centroDialog,setCentroDialog]=useState(false);
 const [operacoes,setOperacoes]=useState([]),[op,setOp]=useState(vazioOperacao()),[opDialog,setOpDialog]=useState(false),[roteiroAtivo,setRoteiroAtivo]=useState(null);

 const carregar=async()=>{setLoading(true);try{
   const [rr,cc]=await Promise.all([apiFetch('/api/producao/roteiros'),apiFetch('/api/producao/centros-trabalho')]);
   if(!rr.ok||!cc.ok) throw new Error('Não foi possível consultar roteiros e centros de trabalho.');
   setRoteiros(await lista(rr));setCentros(await lista(cc));
 }catch(e){setErro(e.message)}finally{setLoading(false)}};
 useEffect(()=>{carregar()},[]);

 const buscarProdutos=async(e)=>{try{
   const r=await ProdutoService.buscarPorNome(e.query||'',0,20); const d=r?.data?.content??r?.data?.data??r?.data??[];setProdutoSug(Array.isArray(d)?d:[]);
 }catch{setProdutoSug([])}};

 const novo=()=>{setRoteiro(vazioRoteiro());setDialog(true);setErro('')};
 const editar=(r)=>{setRoteiro({...r,produto:null,vigenciaInicio:r.vigenciaInicio?new Date(r.vigenciaInicio):null,vigenciaFim:r.vigenciaFim?new Date(r.vigenciaFim):null});setDialog(true)};
 const salvar=async()=>{if(!roteiro.produtoId||!roteiro.codigo||!roteiro.nome){setErro('Produto, código e nome são obrigatórios.');return}
   const payload={...roteiro,vigenciaInicio:roteiro.vigenciaInicio?.toISOString?.().slice(0,10)??roteiro.vigenciaInicio,vigenciaFim:roteiro.vigenciaFim?.toISOString?.().slice(0,10)??roteiro.vigenciaFim};
   const id=roteiro.id;const r=await apiFetch(id?`/api/producao/roteiros/${id}`:'/api/producao/roteiros',{method:id?'PUT':'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(payload)});
   if(!r.ok){setErro(await r.text()||'Falha ao salvar roteiro.');return} setDialog(false);carregar();toast.current?.show({severity:'success',summary:'Roteiro salvo',life:2500});
 };
 const excluir=async(r)=>{const x=await apiFetch('/api/producao/roteiros/'+r.id,{method:'DELETE'});if(!x.ok){setErro(await x.text());return}carregar()};
 const novoCentro=()=>{setCentro({codigo:'',nome:'',capacidadeHorasDia:8,ativo:true});setCentroDialog(true)};
 const editarCentro=(c)=>{setCentro({...c});setCentroDialog(true)};
 const salvarCentro=async()=>{if(!centro.codigo||!centro.nome){setErro('Código e nome do centro de trabalho são obrigatórios.');return}
   const id=centro.id;const x=await apiFetch(id?'/api/producao/centros-trabalho/'+id:'/api/producao/centros-trabalho',{method:id?'PUT':'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(centro)});
   if(!x.ok){setErro(await x.text()||'Falha ao salvar centro.');return}setCentroDialog(false);carregar();
 };
 const excluirCentro=async(c)=>{const x=await apiFetch('/api/producao/centros-trabalho/'+c.id,{method:'DELETE'});if(x.ok)carregar();else setErro(await x.text())};
 const abrirOps=async(r)=>{setRoteiroAtivo(r);setOp(vazioOperacao());const x=await apiFetch('/api/producao/roteiros/'+r.id+'/operacoes');setOperacoes(x.ok?await lista(x):[]);setOpDialog(true)};
 const salvarOp=async()=>{if(!op.codigo||!op.nome||!op.sequencia){setErro('Sequência, código e nome da operação são obrigatórios.');return}
   const id=op.id;const x=await apiFetch(id?`/api/producao/roteiros/operacoes/${id}`:`/api/producao/roteiros/${roteiroAtivo.id}/operacoes`,{method:id?'PUT':'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(op)});
   if(!x.ok){setErro(await x.text()||'Falha ao salvar operação.');return}
   const y=await apiFetch('/api/producao/roteiros/'+roteiroAtivo.id+'/operacoes');setOperacoes(y.ok?await lista(y):[]);setOp(vazioOperacao());toast.current?.show({severity:'success',summary:'Operação salva',life:2000});
 };
 const excluirOp=async(o)=>{const x=await apiFetch('/api/producao/roteiros/operacoes/'+o.id,{method:'DELETE'});if(x.ok){const y=await apiFetch('/api/producao/roteiros/'+roteiroAtivo.id+'/operacoes');setOperacoes(y.ok?await lista(y):[])}};
 const produtoLabel=p=>p?(`${p.codigo?p.codigo+' - ':''}${p.nome||p.descricao||'Produto #'+p.id}`):'';
 const centroOpts=centros.map(c=>({label:`${c.codigo} — ${c.nome}`,value:c.id}));

 return <div className="p-fluid"><Toast ref={toast}/>
  <Card title="PCP — Roteiros de fabricação e centros de trabalho" subTitle="Sequência operacional, capacidade e tempos padrão">
   {erro&&<Message severity="error" text={erro} className="w-full mb-3"/>}
   <div className="flex justify-content-end mb-3"><Button label="Novo roteiro" icon="pi pi-plus" onClick={novo}/></div>
   <DataTable value={roteiros} loading={loading} paginator rows={15} responsiveLayout="scroll" emptyMessage="Nenhum roteiro cadastrado.">
    <Column field="produtoId" header="Produto" sortable/><Column field="codigo" header="Código"/><Column field="nome" header="Roteiro"/>
    <Column field="versao" header="Versão"/><Column field="vigenciaInicio" header="Vigência inicial"/>
    <Column field="ativo" header="Ativo" body={r=>r.ativo?'Sim':'Não'}/>
    <Column header="Ações" body={r=><div className="flex gap-1"><Button icon="pi pi-list" text tooltip="Operações" onClick={()=>abrirOps(r)}/><Button icon="pi pi-pencil" text onClick={()=>editar(r)}/><Button icon="pi pi-trash" text severity="danger" onClick={()=>excluir(r)}/></div>}/>
   </DataTable>
  </Card>

  <Card title="Centros de trabalho" subTitle="Capacidade disponível usada no planejamento e nos roteiros" className="mt-3">
   <div className="flex justify-content-end mb-3"><Button label="Novo centro" icon="pi pi-plus" outlined onClick={novoCentro}/></div>
   <DataTable value={centros} paginator rows={10} emptyMessage="Nenhum centro de trabalho cadastrado.">
    <Column field="codigo" header="Código"/><Column field="nome" header="Nome"/><Column field="capacidadeHorasDia" header="Horas/dia"/>
    <Column field="ativo" header="Ativo" body={c=>c.ativo?'Sim':'Não'}/>
    <Column header="Ações" body={c=><div className="flex gap-1"><Button icon="pi pi-pencil" text onClick={()=>editarCentro(c)}/><Button icon="pi pi-trash" text severity="danger" onClick={()=>excluirCentro(c)}/></div>}/>
   </DataTable>
  </Card>

  <Dialog header="Centro de trabalho" visible={centroDialog} style={{width:'600px'}} onHide={()=>setCentroDialog(false)} footer={<Button label="Salvar" icon="pi pi-check" onClick={salvarCentro}/>}>
   <div className="grid">
    <div className="col-12 md:col-4 field"><label>Código *</label><InputText value={centro.codigo} onChange={e=>setCentro({...centro,codigo:e.target.value})}/></div>
    <div className="col-12 md:col-5 field"><label>Nome *</label><InputText value={centro.nome} onChange={e=>setCentro({...centro,nome:e.target.value})}/></div>
    <div className="col-12 md:col-3 field"><label>Horas/dia</label><InputNumber value={centro.capacidadeHorasDia} min={0.1} minFractionDigits={2} onValueChange={e=>setCentro({...centro,capacidadeHorasDia:e.value})}/></div>
   </div>
  </Dialog>

  <Dialog header="Roteiro de fabricação" visible={dialog} style={{width:'760px'}} onHide={()=>setDialog(false)} footer={<Button label="Salvar" icon="pi pi-check" onClick={salvar}/>}>
   <div className="grid">
    <div className="col-12 field"><label>Produto *</label><AutoComplete value={roteiro.produto} suggestions={produtoSug} completeMethod={buscarProdutos} field="nome" dropdown
      itemTemplate={produtoLabel} selectedItemTemplate={produtoLabel} onChange={e=>setRoteiro({...roteiro,produto:e.value,produtoId:e.value?.id??roteiro.produtoId})} placeholder="Consultar produto no cadastro interno"/></div>
    <div className="col-12 md:col-4 field"><label>Código *</label><InputText value={roteiro.codigo} onChange={e=>setRoteiro({...roteiro,codigo:e.target.value})}/></div>
    <div className="col-12 md:col-5 field"><label>Nome *</label><InputText value={roteiro.nome} onChange={e=>setRoteiro({...roteiro,nome:e.target.value})}/></div>
    <div className="col-12 md:col-3 field"><label>Versão</label><InputNumber value={roteiro.versao} min={1} onValueChange={e=>setRoteiro({...roteiro,versao:e.value})}/></div>
    <div className="col-12 md:col-6 field"><label>Início da vigência</label><Calendar value={roteiro.vigenciaInicio} onChange={e=>setRoteiro({...roteiro,vigenciaInicio:e.value})} dateFormat="dd/mm/yy"/></div>
    <div className="col-12 md:col-6 field"><label>Fim da vigência</label><Calendar value={roteiro.vigenciaFim} onChange={e=>setRoteiro({...roteiro,vigenciaFim:e.value})} dateFormat="dd/mm/yy"/></div>
    <div className="col-12 field"><label>Observação</label><InputText value={roteiro.observacao||''} onChange={e=>setRoteiro({...roteiro,observacao:e.target.value})}/></div>
   </div>
  </Dialog>

  <Dialog header={`Operações — ${roteiroAtivo?.codigo||''}`} visible={opDialog} style={{width:'1000px'}} onHide={()=>setOpDialog(false)}>
   <div className="grid p-fluid">
    <div className="col-12 md:col-2 field"><label>Seq.</label><InputNumber value={op.sequencia} min={1} onValueChange={e=>setOp({...op,sequencia:e.value})}/></div>
    <div className="col-12 md:col-2 field"><label>Código</label><InputText value={op.codigo} onChange={e=>setOp({...op,codigo:e.target.value})}/></div>
    <div className="col-12 md:col-4 field"><label>Operação</label><InputText value={op.nome} onChange={e=>setOp({...op,nome:e.target.value})}/></div>
    <div className="col-12 md:col-4 field"><label>Centro de trabalho</label><Dropdown value={op.centroTrabalhoId} options={centroOpts} filter onChange={e=>setOp({...op,centroTrabalhoId:e.value})} placeholder="Selecionar"/></div>
    <div className="col-12 md:col-4 field"><label>Setup (min)</label><InputNumber value={op.setupMinutos} min={0} minFractionDigits={2} onValueChange={e=>setOp({...op,setupMinutos:e.value})}/></div>
    <div className="col-12 md:col-4 field"><label>Máquina (min)</label><InputNumber value={op.maquinaMinutos} min={0} minFractionDigits={2} onValueChange={e=>setOp({...op,maquinaMinutos:e.value})}/></div>
    <div className="col-12 md:col-4 field"><label>Homem (min)</label><InputNumber value={op.homemMinutos} min={0} minFractionDigits={2} onValueChange={e=>setOp({...op,homemMinutos:e.value})}/></div>
    <div className="col-12 field"><label>Instruções de trabalho</label><InputText value={op.instrucoes||''} onChange={e=>setOp({...op,instrucoes:e.target.value})}/></div>
    <div className="col-12 flex justify-content-end"><Button label={op.id?'Atualizar operação':'Adicionar operação'} icon="pi pi-plus" onClick={salvarOp}/></div>
   </div>
   <DataTable value={operacoes} className="mt-3" emptyMessage="Nenhuma operação.">
    <Column field="sequencia" header="Seq."/><Column field="codigo" header="Código"/><Column field="nome" header="Operação"/>
    <Column field="centroTrabalhoId" header="Centro"/><Column field="setupMinutos" header="Setup"/><Column field="maquinaMinutos" header="Máquina"/><Column field="homemMinutos" header="Homem"/>
    <Column header="" body={row=><div className="flex gap-1"><Button icon="pi pi-pencil" text onClick={()=>setOp({...row})}/><Button icon="pi pi-trash" text severity="danger" onClick={()=>excluirOp(row)}/></div>}/>
   </DataTable>
  </Dialog>
 </div>;
}
