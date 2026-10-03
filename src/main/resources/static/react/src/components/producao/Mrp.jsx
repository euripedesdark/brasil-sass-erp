import React,{useState} from 'react';
import {Card} from 'primereact/card';
import {InputNumber} from 'primereact/inputnumber';
import {Button} from 'primereact/button';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column';
import {Message} from 'primereact/message';
import {apiFetch} from '../../services/ApiConfig';

export default function Mrp(){
 const [produtoId,setProdutoId]=useState(null),[quantidade,setQuantidade]=useState(1),[rows,setRows]=useState([]),[loading,setLoading]=useState(false),[erro,setErro]=useState('');
 const simular=async()=>{setLoading(true);setErro('');try{const r=await apiFetch('/api/producao/mrp/simular',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({produtoId,quantidade})});if(!r.ok)throw new Error((await r.text())||'Falha no MRP');setRows(await r.json())}catch(e){setErro(e.message)}finally{setLoading(false)}};
 return <div className="p-3"><Card title="MRP — Planejamento de Necessidades de Materiais" subTitle="Explosão multinível da BOM, estoque disponível e necessidade líquida">
  <div className="grid p-fluid align-items-end"><div className="col-12 md:col-4"><label>Produto final (ID)</label><InputNumber value={produtoId} onValueChange={e=>setProdutoId(e.value)}/></div><div className="col-12 md:col-4"><label>Quantidade planejada</label><InputNumber value={quantidade} onValueChange={e=>setQuantidade(e.value)} min={0.0001} maxFractionDigits={4}/></div><div className="col-12 md:col-4"><Button label="Executar MRP" icon="pi pi-cog" loading={loading} onClick={simular}/></div></div>
  {erro&&<Message severity="error" text={erro} className="w-full mt-3"/>}
  <DataTable value={rows} paginator rows={15} className="mt-4" emptyMessage="Execute o MRP para ver as necessidades."><Column field="produtoId" header="Produto"/><Column field="necessidadeBruta" header="Necessidade bruta"/><Column field="estoqueDisponivel" header="Estoque disponível"/><Column field="necessidadeLiquida" header="Necessidade líquida"/><Column field="acao" header="Ação"/></DataTable>
 </Card></div>
