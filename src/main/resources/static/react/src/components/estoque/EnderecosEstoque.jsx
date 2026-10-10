import { useTranslation } from 'react-i18next';
import React,{useEffect,useState} from 'react';
import axios from 'axios';
import {Card} from 'primereact/card'; import {InputText} from 'primereact/inputtext'; import {InputNumber} from 'primereact/inputnumber'; import {Dropdown} from 'primereact/dropdown'; import {Button} from 'primereact/button'; import {DataTable} from 'primereact/datatable'; import {Column} from 'primereact/column'; import {Message} from 'primereact/message'; import {Tag} from 'primereact/tag';
import { localeAtivo } from '../shared/LocaleData.js';

const TIPOS=['PULMAO','PICKING','RECEBIMENTO','EXPEDICAO','AVARIA'];

/**
 * Endereçamento WMS.
 *
 * A coluna de Ocupação já existia aqui chamando um `ocupacao` que nunca foi
 * declarado no componente: toda renderização da tela estourava com
 * ReferenceError e o usuário via a página em branco. O endpoint que a coluna
 * precisa, GET /enderecos/ocupacao, existia no backend desde antes — faltava
 * o estado e a chamada.
 *
 * A soma é feita por endereço aqui, e não pelo backend: /ocupacao devolve uma
 * linha por (endereco, produto, lote), e o que a coluna mostra é o total
 * guardado naquele endereço.
 */
export default function EnderecosEstoque(){
    const { t } = useTranslation();
  const [depositos,setDepositos]=useState([]); const [depositoId,setDepositoId]=useState(null);
  const [rows,setRows]=useState([]); const [ocupacao,setOcupacao]=useState([]);
  const [codigo,setCodigo]=useState(''); const [descricao,setDescricao]=useState('');
  const [tipo,setTipo]=useState('PULMAO'); const [capacidade,setCapacidade]=useState(null); const [error,setError]=useState('');
  const [editando,setEditando]=useState(null);

  const msg=e=>e?.response?.data?.message||e?.response?.data?.errors?.[0]?.message||'Falha na operação';

  const loadDepositos=async()=>{const r=await axios.get('/api/estoque/depositos');setDepositos(r.data||[]);if(!depositoId&&r.data?.length)setDepositoId(r.data[0].id);};
  const load=async(id=depositoId)=>{if(!id)return;
    const [r,o]=await Promise.all([
      axios.get('/api/estoque/enderecos',{params:{depositoId:id}}),
      axios.get('/api/estoque/enderecos/ocupacao',{params:{depositoId:id}})
    ]);
    setRows(r.data||[]); setOcupacao(o.data||[]);};
  useEffect(()=>{loadDepositos().catch(e=>setError(msg(e)));},[]);
  useEffect(()=>{load().catch(e=>setError(msg(e)));},[depositoId]);

  const limpar=()=>{setCodigo('');setDescricao('');setCapacidade(null);setTipo('PULMAO');setEditando(null);};

  const salvar=async()=>{
    if(!codigo?.trim()){setError('Informe o código do endereço');return;}
    try{
      setError('');
      const corpo={depositoId,codigo,descricao,tipo,capacidade};
      // edicao vai no proprio id do endereco; criar usa a colecao
      if(editando) await axios.put('/api/estoque/enderecos/'+editando.id,corpo);
      else await axios.post('/api/estoque/enderecos',corpo);
      limpar(); load();
    }catch(e){setError(msg(e));}
  };

  const editar=r=>{
    setEditando(r); setCodigo(r.codigo||''); setDescricao(r.descricao||'');
    setTipo(r.tipo||'PULMAO'); setCapacidade(r.capacidade??null);
  };

  const excluir=r=>{
    const q=ocupacao.filter(x=>x.enderecoId===r.id).reduce((s,x)=>s+Number(x.quantidade||0),0);
    // endereco com saldo nao pode sumir: a ocupacao ficaria sem referencia e
    // o saldo do produto deixaria de aparecer em qualquer consulta
    if(q>0){
      setError(`O endereço ${r.codigo} tem ${q.toLocaleString(localeAtivo())} em estoque. Esvazie antes de excluir.`);
      return;
    }
    if(!window.confirm(`Excluir o endereço ${r.codigo}?`))return;
    axios.delete('/api/estoque/enderecos/'+r.id)
      .then(()=>{limpar();load();})
      .catch(e=>setError(msg(e)));
  };

  const total=r=>ocupacao.filter(x=>x.enderecoId===r.id).reduce((s,x)=>s+Number(x.quantidade||0),0);
  const corpo={codigo,descricao,tipo,capacidade};
  const cheio=r=>{const q=total(r);return Number(r.capacidade||0)>0&&q>=Number(r.capacidade);};

  return <Card title="Endereçamento WMS"><div className="p-fluid grid">
    <div className="field col-12 md:col-3"><label>Depósito</label><Dropdown value={depositoId} options={depositos} optionLabel="nome" optionValue="id" disabled={!!editando} onChange={e=>{setDepositoId(e.value);setEditando(null);}}/></div>
    <div className="field col-12 md:col-2"><label>Código</label><InputText value={codigo} onChange={e=>setCodigo(e.target.value)}/></div>
    <div className="field col-12 md:col-3"><label>Descrição</label><InputText value={descricao} onChange={e=>setDescricao(e.target.value)}/></div>
    <div className="field col-12 md:col-2"><label>Tipo</label><Dropdown value={tipo} options={TIPOS} onChange={e=>setTipo(e.value)}/></div>
    <div className="field col-12 md:col-2"><label>Capacidade</label><InputNumber value={capacidade} onValueChange={e=>setCapacidade(e.value)} minFractionDigits={0} maxFractionDigits={4}/></div>
    <div className="col-12 flex gap-2">
      <Button label={editando?'Salvar alterações':'Adicionar endereço'} icon="pi pi-check" onClick={salvar} disabled={!depositoId||!codigo}/>
      {editando&&<Button label="Cancelar" severity="secondary" text icon="pi pi-times" onClick={limpar}/>}
    </div>
  </div>{error&&<Message severity="error" text={error}/>}
  <DataTable value={rows} className="mt-3" stripedRows paginator rows={10} dataKey="id"><Column field="codigo" header="Código"/><Column field="descricao" header="Descrição"/><Column header="Tipo" body={r=><Tag value={r.tipo||'—'}/>}/><Column field="capacidade" header="Capacidade"/><Column header="Ocupação" body={r=>{const q=total(r);const cap=Number(r.capacidade||0);return cap>0?`${q.toLocaleString(localeAtivo())} / ${cap.toLocaleString(localeAtivo())}`:q.toLocaleString(localeAtivo())}}/><Column header="Situação" body={r=>cheio(r)?<Tag value="CHEIO" severity="danger"/>:<Tag value="LIVRE" severity="success"/>}/><Column header="" body={r=><div className="flex gap-1"><Button icon="pi pi-pencil" rounded text tooltip="Editar" onClick={()=>editar(r)}/><Button icon="pi pi-trash" rounded text severity="danger" tooltip="Excluir" onClick={()=>excluir(r)}/></div>}/></DataTable></Card>;
}
