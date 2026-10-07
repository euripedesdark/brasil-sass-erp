import React,{useEffect,useState} from 'react';
import {Card} from 'primereact/card';import {TabView,TabPanel} from 'primereact/tabview';import {DataTable} from 'primereact/datatable';import {Column} from 'primereact/column';import {Button} from 'primereact/button';import {Dialog} from 'primereact/dialog';import {InputText} from 'primereact/inputtext';import {InputNumber} from 'primereact/inputnumber';import {Message} from 'primereact/message';import {api} from '../../services/ApiConfig';
const tabs=[
 ['reposicao','Reposição / MRP'],['transportes','Transportes'],['revisoes-produto','Engenharia de Produto'],['mudancas-engenharia','Mudanças de Engenharia'],['ehs','EHS'],['contratos-servico','Contratos de Serviço']
];
const labelMap={reposicao:'Política de reposição',transportes:'Ordem de transporte', 'revisoes-produto':'Revisão de produto','mudancas-engenharia':'Mudança de engenharia',ehs:'Ocorrência EHS','contratos-servico':'Contrato de serviço'};
export default function SupplyChainEnterprise(){
 const [idx,setIdx]=useState(0),[rows,setRows]=useState([]),[open,setOpen]=useState(false),[payload,setPayload]=useState({}),[error,setError]=useState(''),[loading,setLoading]=useState(false);
 const tab=tabs[idx];
 const load=async()=>{setLoading(true);try{const r=await api.get('/enterprise/'+tab[0]);setRows(Array.isArray(r.data)?r.data:(r.data?.data||[]));}catch(e){setError(e?.response?.data?.message||e.message)}finally{setLoading(false)}};
 useEffect(()=>{load()},[idx]);
 const fields={reposicao:['produto_id','deposito_id','metodo','estoque_minimo','estoque_maximo','estoque_seguranca','ponto_pedido','lote_economico','lead_time_dias','fornecedor_preferencial_id','ativo'],transportes:['numero','tipo','status','origem','destino','transportadora_id','veiculo','motorista','data_prevista','valor_frete','peso','volume'], 'revisoes-produto':['produto_id','revisao','descricao','status','vigente_desde','vigente_ate','motivo','documento_id'],'mudancas-engenharia':['numero','tipo','titulo','descricao','prioridade','status','solicitante_id','aprovador_id'],'ehs':['numero','tipo','severidade','data_ocorrencia','local_ocorrencia','funcionario_id','ativo_id','descricao','causa_raiz','acao_corretiva','status','prazo'],'contratos-servico':['numero','cliente_id','descricao','inicio','fim','tipo','sla_horas','valor_mensal','franquia_horas','status','renovacao_automatica']};
 const save=async()=>{try{await api.post('/enterprise/'+tab[0],payload);setOpen(false);setPayload({});load()}catch(e){setError(e?.response?.data?.message||e.message)}};
 const money=k=>['valor_frete','valor_mensal'].includes(k);
 return <div className="p-3"><Card title="Supply Chain, Engenharia e EHS"><Message severity="info" text="Bloco 02: reposição, transporte, engenharia/revisões, mudanças, segurança/ambiente e contratos de serviço."/>
 {error&&<Message severity="error" text={error}/>}<TabView activeIndex={idx} onTabChange={e=>setIdx(e.index)}>
 {tabs.map(([key,title])=><TabPanel key={key} header={title}><div className="flex justify-content-end mb-2"><Button label="Novo" icon="pi pi-plus" onClick={()=>{setPayload({});setOpen(true)}}/></div>
 <DataTable value={rows} loading={loading} paginator rows={10} responsiveLayout="scroll" stripedRows>{(fields[key]||[]).map(k=><Column key={k} field={k} header={k.replaceAll('_',' ')} body={r=>money(k)?Number(r[k]||0).toLocaleString('pt-BR',{style:'currency',currency:'BRL'}):(r[k]??'—')}/>)}</DataTable></TabPanel>)}
 </TabView></Card>
 <Dialog visible={open} header={'Novo '+labelMap[tab[0]]} style={{width:'min(760px,95vw)'}} onHide={()=>setOpen(false)}><div className="grid">{(fields[tab[0]]||[]).map(k=><div className="col-12 md:col-6" key={k}><label className="block mb-1">{k.replaceAll('_',' ')}</label>{k.includes('valor')||k.includes('estoque')||k.includes('lote')||k.includes('lead')||k.includes('sla')||k.includes('franquia')||k.includes('peso')||k.includes('volume')?<InputNumber className="w-full" value={payload[k]} onValueChange={e=>setPayload({...payload,[k]:e.value})}/>:<InputText className="w-full" value={payload[k]??''} onChange={e=>setPayload({...payload,[k]:e.target.value})}/>}</div>)}</div><div className="flex justify-content-end mt-3"><Button label="Salvar" icon="pi pi-check" onClick={save}/></div></Dialog>
 </div>
}
