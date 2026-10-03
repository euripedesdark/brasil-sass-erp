import React, { useEffect, useRef, useState } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';
import axios from 'axios';
import { useTranslation } from 'react-i18next';

const empty = { codigo: '', nome: '', tipo: 'PADRAO' };
const errMsg = (e, fallback) => e?.response?.data?.errors?.[0]?.message || e?.response?.data?.message || e?.response?.data?.error || e?.message || fallback;

export const Depositos = () => {
 const { t } = useTranslation();
 const types = [{ label: t('stock.default'), value: 'PADRAO' }, { label: t('stock.shipping'), value: 'EXPEDICAO' }, { label: t('stock.quarantine'), value: 'QUARENTENA' }, { label: t('stock.return'), value: 'DEVOLUCAO' }];
 const toast=useRef(null); const [rows,setRows]=useState([]); const [loading,setLoading]=useState(false); const [visible,setVisible]=useState(false); const [editing,setEditing]=useState(null); const [form,setForm]=useState(empty);
 const load=async()=>{setLoading(true);try{const r=await axios.get('/api/estoque/depositos');const d=r.data?.data??r.data??[];setRows(Array.isArray(d)?d:[])}catch(e){toast.current?.show({severity:'error',summary:t('errors.title'),detail:errMsg(e,t('errors.load')),life:4000})}finally{setLoading(false)}};
 useEffect(()=>{load()},[]);
 const save=async()=>{try{if(editing) await axios.put('/api/estoque/depositos/'+editing.id,form);else await axios.post('/api/estoque/depositos',form);setVisible(false);setEditing(null);setForm(empty);load();toast.current?.show({severity:'success',summary:t('messages.success'),detail:t('stock.warehouseSaved'),life:3000})}catch(e){toast.current?.show({severity:'error',summary:t('errors.title'),detail:errMsg(e,t('errors.save')),life:4000})}};
 const edit=r=>{setEditing(r);setForm({codigo:r.codigo||'',nome:r.nome||'',tipo:r.tipo||'PADRAO'});setVisible(true)};
 const remove=async r=>{if(!window.confirm(t('stock.deactivateWarehouse')))return;try{await axios.delete('/api/estoque/depositos/'+r.id);load()}catch(e){toast.current?.show({severity:'error',summary:t('errors.title'),detail:errMsg(e,t('errors.delete')),life:4000})}};
 return <div><Toast ref={toast}/><Card title={t('stock.warehousesTitle')}><div className="flex justify-content-between mb-3"><span className="text-color-secondary">{t('stock.warehousesDescription')}</span><Button label={t('stock.newWarehouse')} icon="pi pi-plus" onClick={()=>{setEditing(null);setForm(empty);setVisible(true)}}/></div>
 <DataTable value={rows} loading={loading} paginator rows={10} size="small" emptyMessage={t('stock.noWarehouses')}><Column field="codigo" header={t('common.code')}/><Column field="nome" header={t('common.name')}/><Column field="tipo" header={t('common.type')}/><Column field="ativo" header={t('common.status')} body={()=>t('common.active')}/><Column body={r=><div className="flex gap-1"><Button icon="pi pi-pencil" className="p-button-text p-button-sm" tooltip={t('common.edit')} onClick={()=>edit(r)}/><Button icon="pi pi-trash" className="p-button-text p-button-danger p-button-sm" tooltip={t('common.delete')} onClick={()=>remove(r)}/></div>} style={{width:'100px'}}/></DataTable></Card>
 <Dialog header={editing?t('stock.editWarehouse'):t('stock.newWarehouse')} visible={visible} style={{width:'520px'}} onHide={()=>setVisible(false)} footer={<><Button label={t('common.cancel')} className="p-button-text" onClick={()=>setVisible(false)}/><Button label={t('common.save')} icon="pi pi-save" onClick={save}/></>}>
 <div className="grid p-fluid"><div className="col-12 md:col-4 field"><label>{t('common.code')} *</label><InputText value={form.codigo} onChange={e=>setForm({...form,codigo:e.target.value})}/></div><div className="col-12 md:col-8 field"><label>{t('common.name')} *</label><InputText value={form.nome} onChange={e=>setForm({...form,nome:e.target.value})}/></div><div className="col-12 field"><label>{t('common.type')}</label><Dropdown value={form.tipo} options={types} onChange={e=>setForm({...form,tipo:e.value})}/></div></div>
 </Dialog></div>;
};
export default Depositos;