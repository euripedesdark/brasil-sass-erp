import React, { useState } from 'react';
import { Card } from 'primereact/card';
import { Button } from 'primereact/button';
import { Tag } from 'primereact/tag';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { InputText } from 'primereact/inputtext';
import { Message } from 'primereact/message';
import { apiFetch } from '../../services/ApiConfig';
import BuscaFiscal from './BuscaFiscal';

const items=[
 // NFS-e em primeiro: e o unico modulo fiscal que EMITE documento contra a
 // prefeitura, e a tela tinha o codigo pronto sem estar neste menu nem no
 // router. A rota agora existe em App.jsx.
 ['fiscal.nfe','fiscal.nfeDesc','pi pi-file-check','/fiscal/nfe'],
 ['fiscal.nfse','fiscal.nfseDesc','pi pi-send','/fiscal/nfse'],
 ['menu.taxEntries','fiscal.entriesDesc','pi pi-download','/fiscal/entradas'],
 ['menu.ncm','fiscal.ncmDesc','pi pi-list','/fiscal/ncm'],
 ['menu.cfop','fiscal.cfopDesc','pi pi-sitemap','/fiscal/cfop'],
 ['menu.cest','fiscal.cestDesc','pi pi-tags','/fiscal/cest'],
 ['menu.issqn','fiscal.issqnDesc','pi pi-briefcase','/fiscal/issqn'],
 ['menu.taxes','fiscal.taxesDesc','pi pi-percentage','/fiscal/impostos'],
 ['menu.digitalCertificate','fiscal.certificateDesc','pi pi-key','/fiscal/certificados'],
 ['menu.sped','fiscal.spedDesc','pi pi-file-export','/fiscal/sped'],
 ['menu.sefaz','fiscal.sefazDesc','pi pi-cloud','/fiscal/sefaz']
];
export default function FiscalHub(){
 const navigate=useNavigate();
 const { t } = useTranslation();
 const [status, setStatus] = useState({ cte: null, mdfe: null });
 const [loading, setLoading] = useState(false);
 const [recibo, setRecibo] = useState('');
 const [reciboResultado, setReciboResultado] = useState(null);
 const [reciboLoading, setReciboLoading] = useState(false);
 const [reciboErro, setReciboErro] = useState('');
 const consultarStatus = async () => {
  setLoading(true);
  try {
   const [cte, mdfe] = await Promise.all([
	apiFetch('/api/fiscal/cte/status').then((r) => r.json()),
	apiFetch('/api/fiscal/mdfe/status').then((r) => r.json())
   ]);
   setStatus({ cte, mdfe });
  } finally {
   setLoading(false);
  }
 };
 const consultarRecibo = async (event) => {
  event?.preventDefault();
  if (!recibo.trim()) {
   setReciboErro('Informe o número do recibo do MDF-e.');
   setReciboResultado(null);
   return;
  }
  setReciboLoading(true);
  setReciboErro('');
  try {
   const response = await apiFetch('/api/fiscal/mdfe/recibo?numero=' + encodeURIComponent(recibo.trim()));
   const body = await response.json().catch(() => ({}));
   if (!response.ok) throw new Error(body.message || body.erro || 'Não foi possível consultar o recibo.');
   setReciboResultado(body?.data ?? body);
  } catch (error) {
   setReciboResultado(null);
   setReciboErro(error.message || 'Falha ao consultar o recibo.');
  } finally {
   setReciboLoading(false);
  }
 };
 const statusLabel = (value) => value?.status || value?.situacao || (value ? t('common.available') : t('common.notChecked'));
 return <div className="p-3">
   <h2>{t('nav.tax')}</h2>
   <p className="text-color-secondary">{t('fiscal.subtitle')}</p>
  <Card className="mb-3">
   <div className="flex justify-content-between align-items-center gap-3 flex-wrap">
   <div><strong>{t('fiscal.services')}</strong><div className="text-color-secondary mt-1">{t('fiscal.servicesDesc')}</div></div>
   <Button label={t('fiscal.checkStatus')} icon="pi pi-refresh" outlined onClick={consultarStatus} loading={loading} />
   </div>
   <div className="grid mt-2 mb-0">
	<div className="col-12 md:col-6"><span className="mr-2">CT-e</span><Tag value={statusLabel(status.cte)} severity={status.cte ? 'success' : 'secondary'} /></div>
	<div className="col-12 md:col-6"><span className="mr-2">MDF-e</span><Tag value={statusLabel(status.mdfe)} severity={status.mdfe ? 'success' : 'secondary'} /></div>
   </div>
  </Card>
  <Card title="Consulta de recibo MDF-e" className="mb-3">
   <form onSubmit={consultarRecibo} className="flex align-items-end gap-2 flex-wrap">
    <div className="flex-1 min-w-0">
     <label htmlFor="mdfe-recibo" className="block mb-2">Número do recibo</label>
     <InputText id="mdfe-recibo" value={recibo} onChange={(e) => setRecibo(e.target.value)} className="w-full" placeholder="Ex.: 123456789" />
    </div>
    <Button type="submit" label="Consultar recibo" icon="pi pi-search" loading={reciboLoading} />
   </form>
   {reciboErro && <Message severity="error" text={reciboErro} className="w-full mt-2" />}
   {reciboResultado && <pre className="mt-3 mb-0 p-3 surface-ground border-round overflow-auto" style={{ maxHeight: '18rem' }}>{JSON.stringify(reciboResultado, null, 2)}</pre>}
  </Card>
   <BuscaFiscal />
   <div className="grid">{items.map(([titleKey,descKey,i,p])=><div className="col-12 md:col-6 xl:col-4" key={p}><Card className="h-full"><i className={i} style={{fontSize:'1.8rem'}}/><h3>{t(titleKey)}</h3><p className="text-color-secondary">{t(descKey)}</p><Button label={t('common.open')} icon="pi pi-arrow-right" outlined onClick={()=>navigate(p)}/></Card></div>)}</div>
 </div>
}
