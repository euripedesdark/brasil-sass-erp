import React,{useEffect,useState} from 'react';
import {Card} from 'primereact/card';
import {TabView,TabPanel} from 'primereact/tabview';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column';
import {Button} from 'primereact/button';
import {Dialog} from 'primereact/dialog';
import {InputText} from 'primereact/inputtext';
import {InputNumber} from 'primereact/inputnumber';
import {Dropdown} from 'primereact/dropdown';
import {Message} from 'primereact/message';
import {api} from '../../services/ApiConfig';

const tabs=[
 ['revisoes-produto','Engenharia de Produto'],['mudancas-engenharia','Mudanças de Engenharia'],['plm-documentos','Documentos PLM'],
 ['reposicao','Reposição / MRP'],['transportes','Transportes'],['ehs','Ocorrências EHS'],['ehs-riscos','Riscos EHS'],['ehs-inspecoes','Inspeções EHS'],['ehs-acoes','Ações EHS'],['ehs-permissoes','Permissões de Trabalho'],['contratos-servico','Contratos de Serviço']
];
const fields={
 'revisoes-produto':['produto_id','revisao','descricao','status','vigente_desde','vigente_ate','motivo','documento_id'],
 'mudancas-engenharia':['numero','tipo','titulo','descricao','prioridade'],
 'plm-documentos':['revisao_id','mudanca_id','tipo','codigo','versao','nome','localizacao','status','obrigatorio','vigencia_inicio','vigencia_fim'],
 reposicao:['produto_id','deposito_id','metodo','estoque_minimo','estoque_maximo','estoque_seguranca','ponto_pedido','lote_economico','lead_time_dias','fornecedor_preferencial_id','ativo'],
 transportes:['numero','tipo','status','origem','destino','transportadora_id','veiculo','motorista','data_prevista','valor_frete','peso','volume'],
 ehs:['numero','tipo','severidade','data_ocorrencia','local_ocorrencia','funcionario_id','ativo_id','descricao','causa_raiz','acao_corretiva','status','prazo'],
 'contratos-servico':['numero','cliente_id','descricao','inicio','fim','tipo','sla_horas','valor_mensal','franquia_horas','status','renovacao_automatica']
};
const effectTypes=[
 {label:'Revisão de produto',value:'REVISAO_PRODUTO'},{label:'Estrutura / BOM',value:'ESTRUTURA'},
 {label:'Roteiro de produção',value:'ROTEIRO'},{label:'Documento',value:'DOCUMENTO'}
];
const effectActions={
 REVISAO_PRODUTO:[{label:'Vigenciar',value:'VIGENCIAR'}],
 ESTRUTURA:[{label:'Ativar',value:'ATIVAR'},{label:'Inativar',value:'INATIVAR'}],
 ROTEIRO:[{label:'Vigenciar',value:'VIGENCIAR'},{label:'Inativar',value:'INATIVAR'}],
 DOCUMENTO:[{label:'Aprovar',value:'APROVAR'},{label:'Tornar obsoleto',value:'INATIVAR'}]
};

export default function SupplyChainEnterprise(){
 const [idx,setIdx]=useState(0),[rows,setRows]=useState([]),[open,setOpen]=useState(false),[payload,setPayload]=useState({}),[error,setError]=useState(''),[loading,setLoading]=useState(false);
 const [workflow,setWorkflow]=useState(null),[effectOpen,setEffectOpen]=useState(false),[effect,setEffect]=useState({entidadeTipo:'REVISAO_PRODUTO',acao:'VIGENCIAR',ordemExecucao:1});
 const tab=tabs[idx];
 const load=async()=>{setLoading(true);setError('');try{const r=await api.get('/enterprise/'+tab[0]);setRows(Array.isArray(r.data)?r.data:(r.data?.data||[]));}catch(e){setError(e?.response?.data?.message||e.message)}finally{setLoading(false)}};
 useEffect(()=>{load()},[idx]);
 const save=async()=>{try{const body={...payload};if(tab[0]==='mudancas-engenharia')body.status='ABERTA';await api.post('/enterprise/'+tab[0],body);setOpen(false);setPayload({});load()}catch(e){setError(e?.response?.data?.message||e.message)}};
 const openWorkflow=async(id)=>{try{const r=await api.get('/enterprise/plm/mudancas/'+id+'/workflow');setWorkflow(r.data)}catch(e){setError(e?.response?.data?.message||e.message)}};
 const actionWorkflow=async(path,body={})=>{if(!workflow?.mudanca?.id)return;try{const r=await api.post('/enterprise/plm/mudancas/'+workflow.mudanca.id+'/'+path,body);setWorkflow(r.data);load()}catch(e){setError(e?.response?.data?.message||e.message)}};
 const addEffect=async()=>{try{await api.post('/enterprise/plm/mudancas/'+workflow.mudanca.id+'/efeitos',effect);setEffectOpen(false);setEffect({entidadeTipo:'REVISAO_PRODUTO',acao:'VIGENCIAR',ordemExecucao:1});await openWorkflow(workflow.mudanca.id)}catch(e){setError(e?.response?.data?.message||e.message)}};
 const renderValue=(k,v)=>{if(v===null||v===undefined||v==='')return '—';if(typeof v==='boolean')return v?'Sim':'Não';if(['valor_frete','valor_mensal'].includes(k))return Number(v).toLocaleString('pt-BR',{style:'currency',currency:'BRL'});return String(v)};
 const actions=tab[0]==='mudancas-engenharia'?<Column header="Workflow" body={r=><Button size="small" label="Abrir fluxo" icon="pi pi-sitemap" onClick={()=>openWorkflow(r.id)}/>} />:tab[0]==='ehs-riscos'?<Column header="Ação" body={r=><Button size="small" label="Reavaliar" icon="pi pi-refresh" onClick={async()=>{try{await api.post('/enterprise/ehs/riscos/'+r.id+'/reavaliar',{probabilidade:r.probabilidade,impacto:r.impacto});load()}catch(e){setError(e?.response?.data?.message||e.message)}}}/>} />:tab[0]==='ehs-inspecoes'?<Column header="Ação" body={r=><Button size="small" label={r.status==='ENCERRADA'?'Reabrir':'Encerrar'} icon={r.status==='ENCERRADA'?'pi pi-folder-open':'pi pi-check'} onClick={async()=>{try{await api.post('/enterprise/ehs/inspecoes/'+r.id+'/'+(r.status==='ENCERRADA'?'reabrir':'encerrar'),r.status==='ENCERRADA'?{}:{resultado:r.resultado||'CONFORME',observacao:r.observacao});load()}catch(e){setError(e?.response?.data?.message||e.message)}}}/>} />:tab[0]==='ehs-acoes'?<Column header="Ação" body={r=><Button size="small" label="Concluir" icon="pi pi-check" disabled={r.status==='CONCLUIDA'} onClick={async()=>{try{await api.post('/enterprise/ehs/acoes/'+r.id+'/concluir',{evidencia:r.evidencia});load()}catch(e){setError(e?.response?.data?.message||e.message)}}}/>} />:tab[0]==='ehs-permissoes'?<Column header="Ação" body={r=><div className="flex gap-2"><Button size="small" label="Aprovar" icon="pi pi-check" disabled={r.status==='APROVADA'||r.status==='CANCELADA'} onClick={async()=>{try{await api.post('/enterprise/ehs/permissoes/'+r.id+'/aprovar');load()}catch(e){setError(e?.response?.data?.message||e.message)}}}/><Button size="small" severity="danger" label="Cancelar" icon="pi pi-times" disabled={r.status==='CANCELADA'||r.status==='CONCLUIDA'} onClick={async()=>{try{await api.post('/enterprise/ehs/permissoes/'+r.id+'/cancelar');load()}catch(e){setError(e?.response?.data?.message||e.message)}}}/></div>} />:null;
 return <div className="p-3">
  <Card title="Supply Chain / Engenharia / EHS">
   <Message severity="info" text="PLM: mudança → efeitos → aprovação → implementação sobre revisão, BOM, roteiro e documentos."/>
   {error&&<Message severity="error" text={error}/>}
   <TabView activeIndex={idx} onTabChange={e=>setIdx(e.index)}>
    {tabs.map(([key,title])=><TabPanel key={key} header={title}>
     <div className="flex justify-content-end mb-2"><Button label="Novo" icon="pi pi-plus" onClick={()=>{setPayload({});setOpen(true)}}/></div>
     <DataTable value={rows} loading={loading} paginator rows={10} responsiveLayout="scroll" stripedRows>
      {(fields[key]||[]).map(k=><Column key={k} field={k} header={k.replaceAll('_',' ')} body={r=>renderValue(k,r[k])}/>)}{actions}
     </DataTable>
    </TabPanel>)}
   </TabView>
  </Card>

  <Dialog visible={open} header={'Novo '+tab[1]} style={{width:'min(820px,96vw)'}} onHide={()=>setOpen(false)}>
   <div className="grid p-fluid">
    {(fields[tab[0]]||[]).map(k=><div className="col-12 md:col-6" key={k}><label className="block mb-1">{k.replaceAll('_',' ')}</label>
     {['valor_frete','valor_mensal','estoque_minimo','estoque_maximo','estoque_seguranca','ponto_pedido','lote_economico','lead_time_dias','sla_horas','franquia_horas','peso','volume'].includes(k)
      ?<InputNumber className="w-full" value={payload[k]} onValueChange={e=>setPayload({...payload,[k]:e.value})}/>
      :<InputText className="w-full" value={payload[k]??''} onChange={e=>setPayload({...payload,[k]:e.target.value})}/>}
    </div>)}
   </div><div className="flex justify-content-end mt-3"><Button label="Salvar" icon="pi pi-check" onClick={save}/></div>
  </Dialog>

  <Dialog visible={!!workflow} header={workflow?'ECO '+workflow.mudanca.numero+' — '+workflow.mudanca.titulo:''} style={{width:'min(1100px,96vw)'}} onHide={()=>setWorkflow(null)}>
   {workflow&&<div>
    <div className="grid mb-3"><div className="col-12 md:col-4"><strong>Status:</strong> {workflow.mudanca.status}</div><div className="col-12 md:col-4"><strong>Prioridade:</strong> {workflow.mudanca.prioridade}</div><div className="col-12 md:col-4"><strong>Tipo:</strong> {workflow.mudanca.tipo}</div></div>
    <h4>Efeitos de engenharia</h4>
    <DataTable value={workflow.efeitos||[]} size="small"><Column field="ordem_execucao" header="Ordem"/><Column field="entidade_tipo" header="Entidade"/><Column field="entidade_id" header="ID"/><Column field="acao" header="Ação"/><Column field="status" header="Status"/><Column field="erro_implementacao" header="Erro"/></DataTable>
    <div className="flex gap-2 mt-3">
     {['ABERTA','EM_DESENVOLVIMENTO','REJEITADA'].includes(workflow.mudanca.status)&&<Button label="Enviar para aprovação" icon="pi pi-send" onClick={()=>actionWorkflow('enviar-aprovacao')}/>}
     {workflow.mudanca.status==='EM_APROVACAO'&&<><Button label="Aprovar" icon="pi pi-check" onClick={()=>actionWorkflow('aprovar',{decisao:'APROVADO',etapa:1})}/><Button severity="danger" label="Rejeitar" icon="pi pi-times" onClick={()=>actionWorkflow('aprovar',{decisao:'REJEITADO',etapa:1})}/></>}
     {workflow.mudanca.status==='APROVADA'&&<Button severity="success" label="Implementar" icon="pi pi-bolt" onClick={()=>actionWorkflow('implementar')}/>}
     {!['IMPLEMENTADA','CANCELADA'].includes(workflow.mudanca.status)&&<Button outlined label="Adicionar efeito" icon="pi pi-plus" onClick={()=>setEffectOpen(true)}/>}
    </div>
   </div>}
  </Dialog>

  <Dialog visible={effectOpen} header="Adicionar efeito de engenharia" style={{width:'min(620px,95vw)'}} onHide={()=>setEffectOpen(false)}>
   <div className="grid p-fluid">
    <div className="col-12"><label className="block mb-1">Tipo</label><Dropdown value={effect.entidadeTipo} options={effectTypes} onChange={e=>setEffect({...effect,entidadeTipo:e.value,acao:effectActions[e.value][0].value})}/></div>
    <div className="col-12"><label className="block mb-1">ID da entidade existente</label><InputNumber value={effect.entidadeId} onValueChange={e=>setEffect({...effect,entidadeId:e.value})}/></div>
    <div className="col-12"><label className="block mb-1">Ação</label><Dropdown value={effect.acao} options={effectActions[effect.entidadeTipo]} onChange={e=>setEffect({...effect,acao:e.value})}/></div>
    <div className="col-12"><label className="block mb-1">Ordem de execução</label><InputNumber value={effect.ordemExecucao} min={1} onValueChange={e=>setEffect({...effect,ordemExecucao:e.value||1})}/></div>
   </div><div className="flex justify-content-end mt-3"><Button label="Adicionar" icon="pi pi-check" onClick={addEffect}/></div>
  </Dialog>
 </div>;
}
