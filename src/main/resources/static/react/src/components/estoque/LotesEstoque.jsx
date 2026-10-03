import { useTranslation } from 'react-i18next';
import React,{useEffect,useState} from 'react';

import axios from 'axios';
import {Card} from 'primereact/card'; import {InputText} from 'primereact/inputtext'; import {InputNumber} from 'primereact/inputnumber'; import {Calendar} from 'primereact/calendar'; import {Dropdown} from 'primereact/dropdown'; import {Button} from 'primereact/button'; import {DataTable} from 'primereact/datatable'; import {Column} from 'primereact/column'; import {Message} from 'primereact/message'; import {Tag} from 'primereact/tag';
import {AutoComplete} from 'primereact/autocomplete';
import ProdutoService from '../../services/ProdutoService';

export default function LotesEstoque(){
 const { t } = useTranslation();
    const [produtoId,setProdutoId]=useState('');const [produto,setProduto]=useState(null);const [produtos,setProdutos]=useState([]);const [codigo,setCodigo]=useState('');const [quantidade,setQuantidade]=useState(0);const [fabricacao,setFabricacao]=useState(null);const [validade,setValidade]=useState(null);const [depositos,setDepositos]=useState([]);const [depositoId,setDepositoId]=useState(null);const [rows,setRows]=useState([]);const [statusFiltro,setStatusFiltro]=useState('ATIVO');const [error,setError]=useState('');
const load=async()=>{const r=await axios.get('/api/estoque/lotes',produtoId?{params:{produtoId}}:{});setRows((r.data||[]).filter(x=>!statusFiltro||x.status===statusFiltro))};useEffect(()=>{axios.get('/api/estoque/depositos').then(r=>setDepositos(r.data||[])).catch(()=>{});load().catch(e=>setError(e.response?.data?.message||t('legacyUi.lotes.loadError')));},[statusFiltro]);
const buscarProdutos=async e=>{try{const r=await ProdutoService.buscarPorNome((e.query||'').trim(),0,20);const d=r?.data?.data?.content??r?.data?.content??r?.data?.data??r?.data??[];setProdutos(Array.isArray(d)?d:[])}catch{setProdutos([])}};
const msg=e=>e?.response?.data?.message||e?.response?.data?.errors?.[0]?.message||'Falha na operação';
 const iso=d=>d?.toISOString().slice(0,10);
 const limpar=()=>{setCodigo('');setQuantidade(0);setFabricacao(null);setValidade(null);setEditando(null);};
 //editar manda no id do lote; criar usa a colecao. O produto do lote nao muda
 //na edicao — trocar o produto deixaria o saldo ligado a outro item.
 const salvar=async()=>{
  if(!codigo?.trim()){setError(t('legacyUi.lotes.codeRequired'));return;}
  try{
   setError('');
   const corpo={produtoId:Number(editando?.produtoId||produtoId),codigo,quantidade,depositoId:editando?.depositoId??depositoId,
                dataFabricacao:iso(fabricacao),dataValidade:iso(validade),status:editando?.status||'ATIVO'};
   if(editando) await axios.put('/api/estoque/lotes/'+editando.id,corpo);
   else await axios.post('/api/estoque/lotes',corpo);
   limpar();load();
  }catch(e){setError(msg(e));}
 };
 const editar=r=>{setEditando(r);setProdutoId(String(r.produtoId));setProduto({id:r.produtoId});setCodigo(r.codigo||'');setQuantidade(Number(r.quantidade||0));
  setFabricacao(r.dataFabricacao?new Date(r.dataFabricacao):null);setValidade(r.dataValidade?new Date(r.dataValidade):null);};
 //lote com saldo nao some: excluir zeraria o estoque do produto sem baixa, e o
 //saldo continuaria batendo com o que foi movimentado
 const excluir=r=>{
  if(Number(r.quantidade||0)>0){setError(`O lote ${r.codigo} tem ${Number(r.quantidade).toLocaleString('pt-BR')} em saldo. Zere antes de excluir.`);return;}
  if(!window.confirm(`Excluir o lote ${r.codigo}?`))return;
  axios.delete('/api/estoque/lotes/'+r.id).then(()=>{limpar();load();}).catch(e=>setError(msg(e)));
 };
return <Card title={t('legacyUi.lotes.title')}><div className="p-fluid grid"><div className="field col-12 md:col-2"><label>Produto</label><AutoComplete value={produto} suggestions={produtos} completeMethod={buscarProdutos} itemTemplate={p=><div><strong>{p.nome||p.descricao}</strong><small className="ml-2">{p.codigo||p.id}</small></div>} selectedItemTemplate={p=>p?`${p.nome||p.descricao} · ${p.codigo||p.id}`:''} disabled={!!editando} onChange={e=>{setProduto(e.value);setProdutoId(e.value?.id||'')}} placeholder="Pesquisar produto"/></div><div className="field col-12 md:col-2"><label>Lote</label><InputText value={codigo} onChange={e=>setCodigo(e.target.value)}/></div><div className="field col-12 md:col-2"><label>Quantidade</label><InputNumber value={quantidade} onValueChange={e=>setQuantidade(e.value||0)} minFractionDigits={3}/></div><div className="field col-12 md:col-2"><label>Fabricação</label><Calendar value={fabricacao} onChange={e=>setFabricacao(e.value)} dateFormat="dd/mm/yy"/></div><div className="field col-12 md:col-2"><label>Validade</label><Calendar value={validade} onChange={e=>setValidade(e.value)} dateFormat="dd/mm/yy"/></div><div className="field col-12 md:col-2"><label>Status</label><Dropdown value={statusFiltro} options={[{label:"Ativos",value:"ATIVO"},{label:"Inativos",value:"INATIVO"},{label:"Todos",value:""}]} onChange={e=>setStatusFiltro(e.value)}/></div><div className="field col-12 md:col-2"><label>Depósito</label><Dropdown value={depositoId} options={depositos} optionLabel="nome" optionValue="id" onChange={e=>setDepositoId(e.value)}/></div><div className="col-12"><Button label={editando?'Salvar alterações':'Cadastrar lote'} icon="pi pi-check" onClick={salvar} disabled={!produtoId||!codigo}/>{editando&&<Button label={t('legacyUi.lotes.cancel')} severity="secondary" text icon="pi pi-times" onClick={limpar}/>}</div></div>{error&&<Message severity="error" text={error}/>}<DataTable value={rows} className="mt-3" paginator rows={10} stripedRows><Column field="produtoId" header="Produto"/><Column field="codigo" header="Lote"/><Column field="quantidade" header="Saldo do lote"/><Column field="dataFabricacao" header="Fabricação"/><Column field="dataValidade" header="Validade"/><Column field="depositoId" header="Depósito"/><Column header="Status" body={r=><Tag value={r.status||"—"} severity={r.status==="ATIVO"?"success":r.status==="VENCIDO"?"danger":"secondary"}/>}/><Column header="" body={r=><div className="flex gap-1"><Button icon="pi pi-pencil" rounded text tooltip="Editar" onClick={()=>editar(r)}/><Button icon="pi pi-trash" rounded text severity="danger" tooltip="Excluir" onClick={()=>excluir(r)}/></div>}/></DataTable></Card>}