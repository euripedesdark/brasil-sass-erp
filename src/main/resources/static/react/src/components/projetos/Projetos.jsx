import React, { useState, useEffect, useRef, useCallback } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { TabView, TabPanel } from 'primereact/tabview';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/projetos';
const fmt = (v) => Number(v ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

export const Projetos = () => {
    const toast = useRef(null);
    const [projs, setProjs] = useState([]);
    const [sel, setSel] = useState(null);
    const [etapas, setEtapas] = useState([]);
    const [movs, setMovs] = useState([]);
    const [riscos, setRiscos] = useState([]);
    const [muds, setMuds] = useState([]);
    const [fats, setFats] = useState([]);
    const [resumo, setResumo] = useState(null);
    const [loading, setLoading] = useState(true);
    const [dlg, setDlg] = useState(false);
    const [f, setF] = useState({});
    const [fEtapa, setFEtapa] = useState({});
    const [fMov, setFMov] = useState({ tipo: 'CUSTO' });
    const [dlgEtapa, setDlgEtapa] = useState(false);
    const [dlgMov, setDlgMov] = useState(false);

    const pesoImpacto = (imp) => { const s = String(imp || '').trim().toUpperCase(); return s === 'BAIXO' ? 1 : s === 'ALTO' ? 3 : s === 'CRITICO' ? 4 : 2; };
    const exposicao = (r) => (Number(r.probabilidade ?? 0) * pesoImpacto(r.impacto));
        const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? j ?? []); };
    const carregar = useCallback(async () => {
        setLoading(true);
        try { setProjs(await apiFetch(BASE).then(js)); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha ao carregar', life: 4000 }); }
        finally { setLoading(false); }
    }, []);
    useEffect(() => { carregar(); }, [carregar]);
    const ver = async (p) => {
        setSel(p);
        const [e, m, r, mu, fa, rs] = await Promise.all([
            apiFetch(BASE + '/' + p.id + '/etapas').then(js),
            apiFetch(BASE + '/' + p.id + '/movimentos').then(js),
            apiFetch(BASE + '/' + p.id + '/riscos').then(js),
            apiFetch(BASE + '/' + p.id + '/mudancas').then(js),
            apiFetch(BASE + '/' + p.id + '/faturamentos').then(js),
            apiFetch(BASE + '/' + p.id + '/resumo').then((r) => r.json().catch(() => null)),
        ]);
        setEtapas(e); setMovs(m); setRiscos(r); setMuds(mu); setFats(fa); setResumo(rs);
    };
    const salvar = async () => {
        if (!f.codigo?.trim() || !f.nome?.trim()) { toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Código e nome obrigatórios', life: 3000 }); return; }
        await apiFetch(BASE, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ...f, orcamentoTotal: Number(f.orcamentoTotal || 0) }) });
        setDlg(false); setF({}); carregar();
    };
    const salvarEtapa = async () => { await apiFetch(BASE + '/' + sel.id + '/etapas', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ...fEtapa, ordem: Number(fEtapa.ordem || 1) }) }); setDlgEtapa(false); setFEtapa({}); ver(sel); };
    const salvarMov = async () => { await apiFetch(BASE + '/' + sel.id + '/movimentos', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ...fMov, valor: Number(fMov.valor || 0), data: fMov.data ? fMov.data.toISOString().slice(0, 10) : null }) }); setDlgMov(false); setFMov({ tipo: 'CUSTO' }); ver(sel); };
    const decidir = async (id, aprovar) => { await apiFetch(BASE + '/' + sel.id + '/mudancas/' + id + '/decidir', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ aprovar }) }); ver(sel); };
    const [dlgFat, setDlgFat] = useState(false);
    const [fatId, setFatId] = useState(null);
    const [fServ, setFServ] = useState([]);
    const [selServ, setSelServ] = useState(null);
    const [selCli, setSelCli] = useState('');
    const abrirFat = async (id) => { setFatId(id); setSelServ(null); setSelCli(''); try { const r = await apiFetch('/api/cadastro/servicos'); const j = await r.json().catch(() => []); setFServ(Array.isArray(j) ? j : (j?.data ?? j?.content ?? [])); } catch (e) {} setDlgFat(true); };
    const confirmarFat = async () => {
        const r = await apiFetch(BASE + '/' + sel.id + '/faturamentos/' + fatId + '/faturar', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ servicoId: selServ, clienteId: selCli ? Number(selCli) : null }) });
        if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha ao faturar (verifique serviço/cliente)', life: 4500 }); return; } toast.current?.show({ severity: 'success', summary: 'Faturado', detail: 'Título gerado', life: 3000 }); setDlgFat(false); ver(sel); };
    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='flex justify-content-between align-items-center mb-3 flex-wrap gap-2'>
                <div><h2 className='m-0'>Projetos</h2><span className='bc-muted'>WBS, custos, riscos, mudanças e faturamento</span></div>
                <Button label='Novo projeto' icon='pi pi-plus' onClick={() => { setF({}); setDlg(true); }} />
            </div>
            <div className='grid'>
                <div className='bc-form-col-4 col-12 md:col-4'>
                    <DataTable value={projs} loading={loading} paginator rows={10} emptyMessage='Nenhum projeto.' responsiveLayout='scroll' dataKey='id' selectionMode='single' selection={sel} onSelectionChange={(e) => e.value && ver(e.value)}>
                        <Column field='codigo' header='Código' />
                        <Column field='nome' header='Nome' />
                        <Column field='status' header='Status' body={(r) => <Tag value={r.status} severity={r.status === 'CONCLUIDO' ? 'success' : 'info'} />} />
                    </DataTable>
                </div>
                <div className='bc-form-col-8 col-12 md:col-8'>
                    {!sel && (<Card><p className='bc-muted'>Selecione um projeto.</p></Card>)}
                    {sel && resumo && (<div className='grid mb-2'>
                        {[['Orçamento', resumo.orcamento], ['Custo', resumo.custoRealizado], ['Receita', resumo.receitaRealizada], ['Faturado', resumo.faturado], ['Margem', resumo.margem], ['Riscos', resumo.riscosAbertos ?? 0]].map(([l, v]) => (<div key={l} className='bc-form-col-4'><Card><small>{l}</small><div className='text-xl font-bold'>{l === 'Riscos' ? v : fmt(v)}</div></Card></div>))}
                    </div>)}
                    {sel && (<TabView>
                        <TabPanel header='WBS'>
                            <div className='flex justify-end mb-2'><Button label='Etapa' size='small' icon='pi pi-plus' outlined onClick={() => { setFEtapa({}); setDlgEtapa(true); }} /></div>
                            <DataTable value={etapas} paginator rows={8} emptyMessage='Sem etapas.' responsiveLayout='scroll' dataKey='id'>
                                <Column field='codigoWbs' header='WBS' style={{ width: '6rem' }} />
                                <Column field='nome' header='Etapa' />
                                <Column field='pctConcluido' header='%' style={{ width: '4rem' }} />
                                <Column field='status' header='Status' style={{ width: '9rem' }} />
                            </DataTable>
                        </TabPanel>
                        <TabPanel header='Custos/Receitas'>
                            <div className='flex justify-end mb-2'><Button label='Lançar' size='small' icon='pi pi-plus' outlined onClick={() => { setFMov({ tipo: 'CUSTO' }); setDlgMov(true); }} /></div>
                            <DataTable value={movs} paginator rows={8} emptyMessage='Sem movimentos.' responsiveLayout='scroll' dataKey='id'>
                                <Column field='tipo' header='Tipo' style={{ width: '7rem' }} />
                                <Column field='descricao' header='Descrição' />
                                <Column header='Valor' body={(r) => fmt(r.valor)} style={{ width: '9rem' }} />
                            </DataTable>
                        </TabPanel>
                        <TabPanel header='Riscos'>
                            <DataTable value={riscos} paginator rows={8} emptyMessage='Sem riscos.' responsiveLayout='scroll' dataKey='id'>
                                <Column field='descricao' header='Risco' />
                                <Column field='probabilidade' header='Prob.%' style={{ width: '5rem' }} />
                                <Column field='impacto' header='Impacto' style={{ width: '7rem' }} />
                                <Column header='Exposicao' body={(r) => exposicao(r)} style={{ width: '6rem' }} />
                                <Column field='status' header='Status' style={{ width: '7rem' }} />
                            </DataTable>
                        </TabPanel>
                        <TabPanel header='Mudanças'>
                            <DataTable value={muds} paginator rows={8} emptyMessage='Sem mudanças.' responsiveLayout='scroll' dataKey='id'>
                                <Column field='descricao' header='Mudança' />
                                <Column field='status' header='Status' style={{ width: '9rem' }} />
                                <Column header='' body={(r) => (r.status === 'SOLICITADA' ? (<div className='flex gap-1'><Button icon='pi pi-check' rounded text severity='success' onClick={() => decidir(r.id, true)} /><Button icon='pi pi-times' rounded text severity='danger' onClick={() => decidir(r.id, false)} /></div>) : null)} style={{ width: '6rem' }} />
                            </DataTable>
                        </TabPanel>
                        <TabPanel header='Faturamento'>
                            <DataTable value={fats} paginator rows={8} emptyMessage='Sem marcos.' responsiveLayout='scroll' dataKey='id'>
                                <Column field='descricao' header='Marco' />
                                <Column header='Valor' body={(r) => fmt(r.valor)} style={{ width: '9rem' }} />
                                <Column field='status' header='Status' style={{ width: '8rem' }} />
                                <Column field='tituloId' header='Título' style={{ width: '6rem' }} />
                                <Column header='' body={(r) => (r.status === 'PREVISTO' ? (<Button label='Faturar' size='small' onClick={() => abrirFat(r.id)} />) : null)} style={{ width: '7rem' }} />
                            </DataTable>
                        </TabPanel>
                    </TabView>)}
                </div>
            </div>
            <Dialog visible={dlg} onHide={() => setDlg(false)} header='Novo projeto' modal style={{ width: 'min(96vw, 560px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-6'><label className='bc-label'>Código *</label><InputText value={f.codigo || ''} onChange={(e) => setF({ ...f, codigo: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Gerente</label><InputText value={f.gerente || ''} onChange={(e) => setF({ ...f, gerente: e.target.value })} /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Nome *</label><InputText value={f.nome || ''} onChange={(e) => setF({ ...f, nome: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Orçamento</label><InputNumber value={f.orcamentoTotal} onValueChange={(e) => setF({ ...f, orcamentoTotal: e.value })} mode='currency' currency='BRL' locale='pt-BR' /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Início</label><Calendar value={f.dataInicio} onChange={(e) => setF({ ...f, dataInicio: e.target.value })} dateFormat='dd/mm/yy' showIcon /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlg(false)} /><Button label='Salvar' icon='pi pi-check' onClick={salvar} /></div>
            </Dialog>
            <Dialog visible={dlgEtapa} onHide={() => setDlgEtapa(false)} header='Nova etapa (WBS)' modal style={{ width: 'min(96vw, 480px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-6'><label className='bc-label'>Código WBS *</label><InputText placeholder='1.2' value={fEtapa.codigoWbs || ''} onChange={(e) => setFEtapa({ ...fEtapa, codigoWbs: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Ordem</label><InputNumber value={fEtapa.ordem} onValueChange={(e) => setFEtapa({ ...fEtapa, ordem: e.value })} useGrouping={false} /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Nome *</label><InputText value={fEtapa.nome || ''} onChange={(e) => setFEtapa({ ...fEtapa, nome: e.target.value })} /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Responsável</label><InputText value={fEtapa.responsavel || ''} onChange={(e) => setFEtapa({ ...fEtapa, responsavel: e.target.value })} /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlgEtapa(false)} /><Button label='Salvar' icon='pi pi-check' onClick={salvarEtapa} /></div>
            </Dialog>
            <Dialog visible={dlgMov} onHide={() => setDlgMov(false)} header='Lançar custo/receita' modal style={{ width: 'min(96vw, 480px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-6'><label className='bc-label'>Tipo</label><Dropdown value={fMov.tipo} options={['CUSTO','RECEITA'].map((t) => ({ label: t, value: t }))} onChange={(e) => setFMov({ ...fMov, tipo: e.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Valor *</label><InputNumber value={fMov.valor} onValueChange={(e) => setFMov({ ...fMov, valor: e.value })} mode='currency' currency='BRL' locale='pt-BR' /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Descrição *</label><InputText value={fMov.descricao || ''} onChange={(e) => setFMov({ ...fMov, descricao: e.target.value })} /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlgMov(false)} /><Button label='Lançar' icon='pi pi-check' onClick={salvarMov} /></div>
            <Dialog visible={dlgFat} onHide={() => setDlgFat(false)} header='Faturar marco' modal style={{ width: 'min(96vw, 480px)' }}>
                <p className='bc-muted'>Gera título a receber. Com serviço + cliente, emite NFS-e de verdade na prefeitura.</p>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-12'><label className='bc-label'>Serviço (p/ NFS-e, opcional)</label><Dropdown value={selServ} options={fServ.map((s) => ({ label: (s.codigo || s.id) + ' - ' + (s.descricao || s.nome || ''), value: s.id }))} onChange={(e) => setSelServ(e.value)} placeholder='Somente título' showClear filter /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Cliente ID (p/ NFS-e, opcional)</label><InputText value={selCli} onChange={(e) => setSelCli(e.target.value)} keyfilter='int' /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlgFat(false)} /><Button label='Faturar' icon='pi pi-check' severity='success' onClick={confirmarFat} /></div>
            </Dialog>
            </Dialog>
        </div>
    );
};
export default Projetos;
