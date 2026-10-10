import React, { useState, useEffect, useRef, useCallback } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { useTranslation } from 'react-i18next';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { InputTextarea } from 'primereact/inputtextarea';
import { TabView, TabPanel } from 'primereact/tabview';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { formatoData } from '../shared/LocaleData.js';

const BASE = '/api/crm';
const ETAPAS = ['PROSPECCAO','QUALIFICACAO','PROPOSTA','NEGOCIACAO','FECHAMENTO'];
const TIPOS_ATIV = ['LIGACAO','EMAIL','VISITA','TAREFA','REUNIAO'];
const fmt = (v) => Number(v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export const CRM = () => {
    const { t } = useTranslation();
    const toast = useRef(null);
    const [leads, setLeads] = useState([]);
    const [pipe, setPipe] = useState([]);
    const [fc, setFc] = useState(null);
    const [ativs, setAtivs] = useState([]);
    const [campanhas, setCampanhas] = useState([]);
    const [campSel, setCampSel] = useState(null);
    const [campContatos, setCampContatos] = useState([]);
    const [campResumo, setCampResumo] = useState(null);
    const [campNovo, setCampNovo] = useState(false);
    const [fCamp, setFCamp] = useState({ nome: '', canal: 'EMAIL', orcamento: 0 });
    const [leadCampId, setLeadCampId] = useState(null);
    const [loading, setLoading] = useState(true);
    const [dlgLead, setDlgLead] = useState(false);
    const [dlgAtiv, setDlgAtiv] = useState(false);
    const [fLead, setFLead] = useState({});
    const [fAtiv, setFAtiv] = useState({});
    const [ano, setAno] = useState(new Date().getFullYear());
    const [mes, setMes] = useState(new Date().getMonth() + 1);

    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? j ?? []); };
    const carregar = useCallback(async () => {
        setLoading(true);
        try {
            const [l, p, a, c] = await Promise.all([apiFetch(BASE + '/leads').then(js), apiFetch(BASE + '/pipeline').then(js), apiFetch(BASE + '/atividades?pendentes=true').then(js), apiFetch(BASE + '/campanhas').then(js)]);
            setLeads(l); setPipe(p); setAtivs(a); setCampanhas(c);
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha ao carregar CRM', life: 4000 }); }
        finally { setLoading(false); }
    }, []);
    useEffect(() => { carregar(); }, [carregar]);
    const buscarForecast = async () => { const r = await apiFetch(BASE + '/forecast?ano=' + ano + '&mes=' + mes); setFc(await r.json().catch(() => null)); };
    const salvarLead = async () => {
        if (!fLead.nome?.trim()) { toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Nome obrigatório', life: 3000 }); return; }
        await apiFetch(BASE + '/leads', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ...fLead, valorEstimado: Number(fLead.valorEstimado || 0), probabilidade: Number(fLead.probabilidade ?? 10) }) });
        setDlgLead(false); setFLead({}); carregar();
    };
    const mover = async (id, etapa) => { await apiFetch(BASE + '/leads/' + id + '/etapa', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ etapa }) }); carregar(); };
    const [dlgPed, setDlgPed] = useState(false);
    const [pedLead, setPedLead] = useState(null);
    const [cliId, setCliId] = useState('');
    const gerarPedido = async () => {
        if (!cliId) return;
        const r = await apiFetch(BASE + '/leads/' + pedLead + '/gerar-pedido', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ clienteId: Number(cliId) }) });
        const j = await r.json().catch(() => ({}));
        if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha ao gerar pedido', life: 4000 }); return; }
        toast.current?.show({ severity: 'success', summary: 'Pedido criado', detail: '#' + (j.pedidoId ?? ''), life: 3500 });
        setDlgPed(false); setCliId(''); carregar();
    };
    const excluirLead = async (id) => { await apiFetch(BASE + '/leads/' + id, { method: 'DELETE' }); carregar(); };
    const salvarAtiv = async () => {
        if (!fAtiv.assunto?.trim()) { toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Assunto obrigatório', life: 3000 }); return; }
        await apiFetch(BASE + '/atividades', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ...fAtiv, dataAgendada: fAtiv.dataAgendada ? fAtiv.dataAgendada.toISOString() : null }) });
        setDlgAtiv(false); setFAtiv({}); carregar();
    };
    const concluir = async (id) => { await apiFetch(BASE + '/atividades/' + id + '/concluir', { method: 'POST' }); carregar(); };


    const campRequest = async (url, method, body) => {
        const r = await apiFetch(BASE + '/campanhas' + url, {
            method, ...(body ? { headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) } : {})
        });
        if (!r.ok) {
            const data = await r.json().catch(() => null);
            toast.current?.show({ severity: 'error', summary: t('common.error'), detail: data?.message || t('erpExtensions.campaigns.falha'), life: 4000 });
            return null;
        }
        return r.json().catch(() => ({}));
    };
    const detalheCampanha = async (c) => {
        setCampSel(c);
        const [r, res] = await Promise.all([apiFetch(BASE + '/campanhas/' + c.id + '/contatos'), apiFetch(BASE + '/campanhas/' + c.id + '/resumo')]);
        if (r.ok) setCampContatos(await js(r));
        if (res.ok) setCampResumo(await res.json());
    };
    const criarCampanha = async () => {
        if (!fCamp.nome?.trim()) return;
        if (await campRequest('', 'POST', { ...fCamp, orcamento: Number(fCamp.orcamento || 0) })) {
            setCampNovo(false); setFCamp({ nome: '', canal: 'EMAIL', orcamento: 0 }); carregar();
        }
    };
    const statusCampanha = async (c, status) => {
        if (await campRequest('/' + c.id + '/status', 'POST', { status })) {
            carregar(); if (campSel?.id === c.id) detalheCampanha({ ...c, status });
        }
    };
    const vincularCampanha = async () => {
        if (!campSel || !leadCampId) return;
        if (await campRequest('/' + campSel.id + '/contatos', 'POST', { leadId: Number(leadCampId) })) {
            setLeadCampId(null); detalheCampanha(campSel);
        }
    };
    const resultadoCampanha = async (c, status) => {
        if (await campRequest('/' + campSel.id + '/contatos/' + c.id + '/resultado', 'POST', { status })) detalheCampanha(campSel);
    };
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='flex justify-content-between align-items-center mb-3 flex-wrap gap-2'>
                <div><h2 className='m-0'>CRM</h2><span className='bc-muted'>Leads, pipeline, forecast e agenda</span></div>
                <div className='flex gap-2'><Button label='Novo lead' icon='pi pi-plus' outlined onClick={() => { setFLead({}); setDlgLead(true); }} /><Button label='Nova atividade' icon='pi pi-calendar-plus' onClick={() => { setFAtiv({}); setDlgAtiv(true); }} /></div>
            </div>
            <TabView>
                <TabPanel header='Leads'>
                    <DataTable value={leads} loading={loading} paginator rows={10} emptyMessage='Nenhum lead.' responsiveLayout='scroll' dataKey='id'>
                        <Column field='nome' header='Nome' />
                        <Column field='empresaNome' header='Empresa' />
                        <Column field='etapa' header='Etapa' style={{ width: '9rem' }} />
                        <Column header='Valor' body={(r) => fmt(r.valorEstimado)} style={{ width: '9rem' }} />
                        <Column header='Avançar' body={(r) => { const i = ETAPAS.indexOf(r.etapa); return i >= 0 && i < ETAPAS.length - 1 ? (<Button label={ETAPAS[i + 1]} size='small' outlined onClick={() => mover(r.id, ETAPAS[i + 1])} />) : (<><Button label='Pedido' size='small' severity='help' onClick={() => { setPedLead(r.id); setDlgPed(true); }} /></>); }} style={{ width: '11rem' }} />
                        <Column header='' body={(r) => (<Button icon='pi pi-trash' rounded text severity='danger' tooltip='Excluir' onClick={() => excluirLead(r.id)} />)} style={{ width: '4rem' }} />
                    </DataTable>
                </TabPanel>
                <TabPanel header='Pipeline'>
                    <div className='grid'>
                        {pipe.map((col) => (<div key={col.etapa} className='bc-form-col-4'><Card title={col.etapa} subTitle={fmt(col.total)}><ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>{(col.leads || []).map((l) => (<li key={l.id}>{l.nome} — <strong>{fmt(l.valor)}</strong></li>))}</ul></Card></div>))}
                    </div>
                </TabPanel>
                <TabPanel header='Forecast'>
                    <div className='flex gap-2 mb-3 flex-wrap'><InputNumber value={ano} onValueChange={(e) => setAno(e.value)} useGrouping={false} placeholder='Ano' /><InputNumber value={mes} onValueChange={(e) => setMes(e.value)} min={1} max={12} useGrouping={false} placeholder='Mês' /><Button label='Calcular' icon='pi pi-calculator' onClick={buscarForecast} /></div>
                    {fc && (<div className='grid'><div className='bc-form-col-4'><Card><small>Leads abertos</small><div className='text-2xl font-bold'>{fc.qtd}</div></Card></div><div className='bc-form-col-4'><Card><small>Bruto</small><div className='text-2xl font-bold'>{fmt(fc.bruto)}</div></Card></div><div className='bc-form-col-4'><Card><small>Ponderado</small><div className='text-2xl font-bold'>{fmt(fc.ponderado)}</div></Card></div></div>)}
                </TabPanel>
                <TabPanel header='Atividades / Agenda'>
                    <DataTable value={ativs} loading={loading} paginator rows={10} emptyMessage='Nenhuma atividade pendente.' responsiveLayout='scroll' dataKey='id'>
                        <Column field='tipo' header='Tipo' style={{ width: '7rem' }} />
                        <Column field='assunto' header='Assunto' />
                        <Column field='dataAgendada' header='Agendada' style={{ width: '11rem' }} />
                        <Column field='responsavel' header='Responsável' style={{ width: '10rem' }} />
                        <Column header='' body={(r) => (<Button icon='pi pi-check' rounded text severity='success' tooltip='Concluir' onClick={() => concluir(r.id)} />)} style={{ width: '4rem' }} />
                    </DataTable>
                </TabPanel>

                <TabPanel header={t('erpExtensions.campaigns.campanhas')}>
                    <div className='flex justify-content-end mb-2'><Button icon='pi pi-plus' label={t('erpExtensions.campaigns.nova')} onClick={() => setCampNovo(true)} /></div>
                    <DataTable value={campanhas} paginator rows={10} dataKey='id' emptyMessage={t('erpExtensions.campaigns.vazio')} responsiveLayout='scroll'>
                        <Column field='nome' header={t('erpExtensions.campaigns.nome')} />
                        <Column field='canal' header={t('erpExtensions.campaigns.canal')} />
                        <Column field='status' header={t('erpExtensions.campaigns.status')} />
                        <Column header={t('common.actions')} body={(c) => <div className='flex gap-1 flex-wrap'>
                            <Button size='small' outlined label={t('erpExtensions.campaigns.detalhes')} onClick={() => detalheCampanha(c)} />
                            {c.status === 'RASCUNHO' && <Button size='small' label={t('erpExtensions.campaigns.ativar')} onClick={() => statusCampanha(c, 'ATIVA')} />}
                            {c.status === 'ATIVA' && <Button size='small' outlined severity='warning' label={t('erpExtensions.campaigns.encerrar')} onClick={() => statusCampanha(c, 'ENCERRADA')} />}
                        </div>} />
                    </DataTable>
                </TabPanel>
            </TabView>
            <Dialog visible={dlgLead} onHide={() => setDlgLead(false)} header='Novo lead' modal style={{ width: 'min(96vw, 560px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-6'><label className='bc-label'>Nome *</label><InputText value={fLead.nome || ''} onChange={(e) => setFLead({ ...fLead, nome: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Empresa</label><InputText value={fLead.empresaNome || ''} onChange={(e) => setFLead({ ...fLead, empresaNome: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Email</label><InputText value={fLead.email || ''} onChange={(e) => setFLead({ ...fLead, email: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Telefone</label><InputText value={fLead.telefone || ''} onChange={(e) => setFLead({ ...fLead, telefone: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Valor estimado</label><InputNumber value={fLead.valorEstimado} onValueChange={(e) => setFLead({ ...fLead, valorEstimado: e.value })} mode='currency' currency='BRL' locale='pt-BR' /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Probabilidade %</label><InputNumber value={fLead.probabilidade ?? 10} onValueChange={(e) => setFLead({ ...fLead, probabilidade: e.value })} suffix=' %' min={0} max={100} /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Observação</label><InputTextarea rows={2} value={fLead.observacao || ''} onChange={(e) => setFLead({ ...fLead, observacao: e.target.value })} /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlgLead(false)} /><Button label='Salvar' icon='pi pi-check' onClick={salvarLead} /></div>
            </Dialog>
            <Dialog visible={dlgPed} onHide={() => setDlgPed(false)} header='Gerar pedido de venda' modal style={{ width: 'min(96vw, 420px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-12'><label className='bc-label'>Cliente ID *</label><InputText value={cliId} onChange={(e) => setCliId(e.target.value)} keyfilter='int' /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlgPed(false)} /><Button label='Gerar' icon='pi pi-check' onClick={gerarPedido} /></div>
            </Dialog>
            <Dialog visible={dlgAtiv} onHide={() => setDlgAtiv(false)} header='Nova atividade' modal style={{ width: 'min(96vw, 520px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-6'><label className='bc-label'>Tipo</label><Dropdown value={fAtiv.tipo || 'TAREFA'} options={TIPOS_ATIV.map((t) => ({ label: t, value: t }))} onChange={(e) => setFAtiv({ ...fAtiv, tipo: e.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Agendada em</label><Calendar value={fAtiv.dataAgendada} onChange={(e) => setFAtiv({ ...fAtiv, dataAgendada: e.value })} dateFormat={formatoData()} showTime showIcon /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Assunto *</label><InputText value={fAtiv.assunto || ''} onChange={(e) => setFAtiv({ ...fAtiv, assunto: e.target.value })} /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Responsável</label><InputText value={fAtiv.responsavel || ''} onChange={(e) => setFAtiv({ ...fAtiv, responsavel: e.target.value })} /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlgAtiv(false)} /><Button label='Salvar' icon='pi pi-check' onClick={salvarAtiv} /></div>
            </Dialog>

            <Dialog visible={campNovo} onHide={() => setCampNovo(false)} header={t('erpExtensions.campaigns.nova')} modal style={{ width: 'min(96vw, 520px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-12'><label className='bc-label'>{t('erpExtensions.campaigns.nome')}</label><InputText value={fCamp.nome} onChange={(e) => setFCamp({ ...fCamp, nome: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>{t('erpExtensions.campaigns.canal')}</label><Dropdown value={fCamp.canal} options={['EMAIL', 'TELEFONE', 'EVENTO', 'OUTRO'].map(s => ({ label: s, value: s }))} onChange={(e) => setFCamp({ ...fCamp, canal: e.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>{t('erpExtensions.campaigns.orcamento')}</label><InputNumber value={fCamp.orcamento} min={0} onValueChange={(e) => setFCamp({ ...fCamp, orcamento: e.value })} mode='currency' currency='BRL' /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>{t('erpExtensions.campaigns.descricao')}</label><InputTextarea rows={2} value={fCamp.descricao || ''} onChange={(e) => setFCamp({ ...fCamp, descricao: e.target.value })} /></div>
                </div>
                <div className='flex justify-content-end mt-3'><Button label={t('erpExtensions.campaigns.criar')} onClick={criarCampanha} /></div>
            </Dialog>
            <Dialog visible={!!campSel} onHide={() => { setCampSel(null); setCampResumo(null); }} header={campSel?.nome || t('erpExtensions.campaigns.detalhes')} modal style={{ width: 'min(96vw, 850px)' }}>
                {campResumo && <div className='flex gap-4 mb-3 flex-wrap'>
                    <span>{t('erpExtensions.campaigns.leads')}: <strong>{campResumo.totalLeads}</strong></span>
                    <span>{t('erpExtensions.campaigns.respostas')}: <strong>{campResumo.respostas}</strong></span>
                    <span>{t('erpExtensions.campaigns.conversoes')}: <strong>{campResumo.conversoes}</strong></span>
                </div>}
                {campSel?.status !== 'ENCERRADA' && <div className='flex gap-2 mb-3'>
                    <Dropdown value={leadCampId} onChange={(e) => setLeadCampId(e.value)} options={leads.map(l => ({ label: l.nome, value: l.id }))} filter showClear placeholder={t('erpExtensions.campaigns.selecionar')} className='flex-1' />
                    <Button label={t('erpExtensions.campaigns.vincular')} disabled={!leadCampId} onClick={vincularCampanha} />
                </div>}
                <DataTable value={campContatos} rows={8} paginator dataKey='id' responsiveLayout='scroll'>
                    <Column field='leadId' header={t('erpExtensions.campaigns.lead')} />
                    <Column field='status' header={t('erpExtensions.campaigns.status')} />
                    <Column header={t('erpExtensions.campaigns.resultado')} body={(c) => campSel?.status === 'ATIVA' && c.status !== 'CONVERTIDO' ?
                        <div className='flex gap-1 flex-wrap'>
                            <Button size='small' outlined label={t('erpExtensions.campaigns.contatado')} onClick={() => resultadoCampanha(c, 'CONTATADO')} />
                            <Button size='small' outlined label={t('erpExtensions.campaigns.respondeu')} onClick={() => resultadoCampanha(c, 'RESPONDEU')} />
                            <Button size='small' severity='success' label={t('erpExtensions.campaigns.converteu')} onClick={() => resultadoCampanha(c, 'CONVERTIDO')} />
                            <Button size='small' outlined severity='secondary' label={t('erpExtensions.campaigns.descartado')} onClick={() => resultadoCampanha(c, 'DESCARTADO')} />
                        </div> : null} />
                </DataTable>
            </Dialog>
        </div>
    );
};
export default CRM;
