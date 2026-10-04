import React, { useState, useEffect, useRef, useCallback } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { Checkbox } from 'primereact/checkbox';
import { InputTextarea } from 'primereact/inputtextarea';
import { TabView, TabPanel } from 'primereact/tabview';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/workflow';

const sev = (s) => s === 'APROVADA' || s === 'CONCLUIDA' ? 'success' : s === 'REJEITADA' ? 'danger' : s === 'PENDENTE' || s === 'EM_ANDAMENTO' ? 'warning' : 'info';

export const Workflow = () => {
    const toast = useRef(null);
    const [defs, setDefs] = useState([]);
    const [insts, setInsts] = useState([]);
    const [pend, setPend] = useState([]);
    const [loading, setLoading] = useState(true);
    const [dlgDef, setDlgDef] = useState(false);
    const [dlgAbrir, setDlgAbrir] = useState(false);
    const [fDef, setFDef] = useState({ codigo: '', nome: '', entidadeAlvo: '', descricao: '' });
    const [fAbrir, setFAbrir] = useState({ definitionId: null, entidadeTipo: '', entidadeId: '', observacao: '' });
    const STAGE_VAZIA = { nome: '', tipo: 'APROVACAO', slaHoras: 48, aprovadores: '', exigeTodos: false };
    const [dlgStages, setDlgStages] = useState(false);
    const [defSel, setDefSel] = useState(null);
    const [stages, setStages] = useState([]);
    const [fStage, setFStage] = useState(STAGE_VAZIA);
    const [dlgTasks, setDlgTasks] = useState(false);
    const [instSel, setInstSel] = useState(null);
    const [tasks, setTasks] = useState([]);

    const carregar = useCallback(async () => {
        setLoading(true);
        try {
            const [d, i, p] = await Promise.all([
                apiFetch(BASE + '/definitions').then(r => r.json()),
                apiFetch(BASE + '/instances').then(r => r.json()),
                apiFetch(BASE + '/tasks/pendentes').then(r => r.json()),
            ]);
            setDefs(Array.isArray(d) ? d : (d?.data ?? []));
            setInsts(Array.isArray(i) ? i : (i?.data ?? []));
            setPend(Array.isArray(p) ? p : (p?.data ?? []));
        } catch (e) {
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha ao carregar workflow', life: 4000 });
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => { carregar(); }, [carregar]);

    const salvarDef = async () => {
        if (!fDef.codigo?.trim() || !fDef.nome?.trim() || !fDef.entidadeAlvo?.trim()) {
            toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Código, nome e entidade alvo são obrigatórios', life: 3000 });
            return;
        }
        await apiFetch(BASE + '/definitions', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ...fDef, ativo: true }) });
        setDlgDef(false);
        setFDef({ codigo: '', nome: '', entidadeAlvo: '', descricao: '' });
        carregar();
    };

    const abrirInst = async () => {
        if (!fAbrir.definitionId || !fAbrir.entidadeTipo?.trim() || !fAbrir.entidadeId) {
            toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Definição, tipo e ID da entidade são obrigatórios', life: 3000 });
            return;
        }
        await apiFetch(BASE + '/instances', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ...fAbrir, entidadeId: Number(fAbrir.entidadeId) }) });
        setDlgAbrir(false);
        carregar();
    };

    const lista = (j) => (Array.isArray(j) ? j : (j?.data ?? []));
    const msgErro = async (r, padrao) => { const j = await r.json().catch(() => null); return j?.message || j?.errors?.[0]?.message || padrao; };

    const carregarStages = async (id) => {
        const r = await apiFetch(BASE + '/definitions/' + id + '/stages');
        if (r.ok) setStages(lista(await r.json().catch(() => [])));
    };
    const abrirStages = async (d) => {
        setDefSel(d);
        setStages([]);
        setFStage(STAGE_VAZIA);
        setDlgStages(true);
        await carregarStages(d.id);
    };
    const salvarStage = async () => {
        if (!fStage.nome?.trim() || !fStage.aprovadores?.trim()) {
            toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Nome e aprovadores são obrigatórios', life: 3000 });
            return;
        }
        const ordem = stages.length ? Math.max(...stages.map((s) => Number(s.ordem || 0))) + 1 : 1;
        const r = await apiFetch(BASE + '/definitions/' + defSel.id + '/stages', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ...fStage, ordem, definitionId: defSel.id, slaHoras: Number(fStage.slaHoras || 48) })
        });
        if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: await msgErro(r, 'Não foi possível adicionar a etapa'), life: 4000 }); return; }
        setFStage(STAGE_VAZIA);
        carregarStages(defSel.id);
    };
    const inativar = async (d) => {
        if (!window.confirm('Inativar a definição ' + d.codigo + '? Novas instâncias não poderão usá-la.')) return;
        const r = await apiFetch(BASE + '/definitions/' + d.id + '/inativar', { method: 'POST' });
        if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: await msgErro(r, 'Não foi possível inativar'), life: 4000 }); return; }
        toast.current?.show({ severity: 'success', summary: 'Definição inativada', life: 2500 });
        carregar();
    };
    const verTarefas = async (i) => {
        setInstSel(i);
        setTasks([]);
        setDlgTasks(true);
        const r = await apiFetch(BASE + '/instances/' + i.id + '/tasks');
        if (r.ok) setTasks(lista(await r.json().catch(() => [])));
    };

    const decidir = async (id, aprovar) => {
        await apiFetch(BASE + '/tasks/' + id + '/decidir', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ aprovar }) });
        toast.current?.show({ severity: 'success', summary: aprovar ? 'Aprovada' : 'Rejeitada', life: 2500 });
        carregar();
    };

    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='flex justify-content-between align-items-center mb-3 flex-wrap gap-2'>
                <div>
                    <h2 className='m-0'>Workflow</h2>
                    <span className='bc-muted'>Aprovação multinível + SLA, transversal aos módulos</span>
                </div>
                <div className='flex gap-2'>
                    <Button label='Nova definição' icon='pi pi-plus' outlined onClick={() => setDlgDef(true)} />
                    <Button label='Abrir instância' icon='pi pi-play' onClick={() => setDlgAbrir(true)} />
                </div>
            </div>

            <TabView>
                <TabPanel header={'Pendentes (' + pend.length + ')'}>
                    <DataTable value={pend} loading={loading} paginator rows={10} emptyMessage='Nenhuma tarefa pendente.' responsiveLayout='scroll' dataKey='id'>
                        <Column field='id' header='#' style={{ width: '4rem' }} />
                        <Column field='instanceId' header='Instância' style={{ width: '7rem' }} />
                        <Column field='responsavel' header='Responsável' />
                        <Column field='slaLimite' header='SLA até' />
                        <Column header='Ação' body={(t) => (
                            <div className='flex gap-1'>
                                <Button icon='pi pi-check' rounded text severity='success' tooltip='Aprovar' onClick={() => decidir(t.id, true)} />
                                <Button icon='pi pi-times' rounded text severity='danger' tooltip='Rejeitar' onClick={() => decidir(t.id, false)} />
                            </div>
                        )} style={{ width: '7rem' }} />
                    </DataTable>
                </TabPanel>
                <TabPanel header='Instâncias'>
                    <DataTable value={insts} loading={loading} paginator rows={10} emptyMessage='Nenhuma instância.' responsiveLayout='scroll' dataKey='id'>
                        <Column field='id' header='#' style={{ width: '4rem' }} />
                        <Column field='entidadeTipo' header='Entidade' />
                        <Column field='entidadeId' header='ID' style={{ width: '6rem' }} />
                        <Column field='etapaAtual' header='Etapa' style={{ width: '6rem' }} />
                        <Column header='Status' body={(r) => <Tag value={r.status} severity={sev(r.status)} />} style={{ width: '10rem' }} />
                        <Column header='' body={(r) => (<Button label='Tarefas' icon='pi pi-list' size='small' text onClick={() => verTarefas(r)} />)} style={{ width: '8rem' }} />
                    </DataTable>
                </TabPanel>
                <TabPanel header='Definições'>
                    <DataTable value={defs} loading={loading} paginator rows={10} emptyMessage='Nenhuma definição.' responsiveLayout='scroll' dataKey='id'>
                        <Column field='codigo' header='Código' />
                        <Column field='nome' header='Nome' />
                        <Column field='entidadeAlvo' header='Entidade alvo' />
                        <Column header='Ativa' body={(r) => <Tag value={r.ativo ? 'Sim' : 'Não'} severity={r.ativo ? 'success' : 'secondary'} />} style={{ width: '7rem' }} />
                        <Column header='' body={(r) => (<div className='flex gap-1'>
                            <Button label='Etapas' icon='pi pi-sitemap' size='small' outlined onClick={() => abrirStages(r)} />
                            {r.ativo && <Button label='Inativar' icon='pi pi-ban' size='small' severity='danger' text onClick={() => inativar(r)} />}
                        </div>)} style={{ width: '15rem' }} />
                    </DataTable>
                </TabPanel>
            </TabView>

            <Dialog visible={dlgDef} onHide={() => setDlgDef(false)} header='Nova definição' modal style={{ width: 'min(96vw, 520px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-6'><label className='bc-label'>Código *</label><InputText value={fDef.codigo} onChange={(e) => setFDef({ ...fDef, codigo: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Entidade alvo *</label><InputText placeholder='titulo, pedido, os...' value={fDef.entidadeAlvo} onChange={(e) => setFDef({ ...fDef, entidadeAlvo: e.target.value })} /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Nome *</label><InputText value={fDef.nome} onChange={(e) => setFDef({ ...fDef, nome: e.target.value })} /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Descrição</label><InputTextarea rows={2} value={fDef.descricao} onChange={(e) => setFDef({ ...fDef, descricao: e.target.value })} /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'>
                    <Button label='Cancelar' text severity='secondary' onClick={() => setDlgDef(false)} />
                    <Button label='Salvar' icon='pi pi-check' onClick={salvarDef} />
                </div>
            </Dialog>

            <Dialog visible={dlgAbrir} onHide={() => setDlgAbrir(false)} header='Abrir instância' modal style={{ width: 'min(96vw, 520px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-12'><label className='bc-label'>Definição *</label>
                        <Dropdown value={fAbrir.definitionId} options={defs.filter(d => d.ativo).map(d => ({ label: d.codigo + ' — ' + d.nome, value: d.id }))} onChange={(e) => setFAbrir({ ...fAbrir, definitionId: e.value })} placeholder='Selecione' />
                    </div>
                    <div className='bc-form-col-6'><label className='bc-label'>Tipo entidade *</label><InputText value={fAbrir.entidadeTipo} onChange={(e) => setFAbrir({ ...fAbrir, entidadeTipo: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>ID entidade *</label><InputText keyfilter='int' value={fAbrir.entidadeId} onChange={(e) => setFAbrir({ ...fAbrir, entidadeId: e.target.value })} /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Observação</label><InputTextarea rows={2} value={fAbrir.observacao} onChange={(e) => setFAbrir({ ...fAbrir, observacao: e.target.value })} /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'>
                    <Button label='Cancelar' text severity='secondary' onClick={() => setDlgAbrir(false)} />
                    <Button label='Abrir' icon='pi pi-play' onClick={abrirInst} />
                </div>
            </Dialog>

            <Dialog visible={dlgStages} onHide={() => setDlgStages(false)} header={'Etapas — ' + (defSel ? defSel.codigo : '')} modal style={{ width: 'min(96vw, 760px)' }}>
                <DataTable value={stages} size='small' emptyMessage='Nenhuma etapa. Sem etapas a definição não gera tarefas de aprovação.' responsiveLayout='scroll' dataKey='id'>
                    <Column field='ordem' header='#' style={{ width: '3rem' }} />
                    <Column field='nome' header='Etapa' />
                    <Column field='tipo' header='Tipo' style={{ width: '8rem' }} />
                    <Column field='aprovadores' header='Aprovadores' />
                    <Column field='slaHoras' header='SLA (h)' style={{ width: '5rem' }} />
                    <Column header='Todos' body={(r) => (r.exigeTodos ? 'Sim' : 'Não')} style={{ width: '5rem' }} />
                </DataTable>
                <h4 className='mt-3 mb-2'>Nova etapa</h4>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-8'><label className='bc-label'>Nome *</label><InputText value={fStage.nome} onChange={(e) => setFStage({ ...fStage, nome: e.target.value })} /></div>
                    <div className='bc-form-col-4'><label className='bc-label'>SLA (horas)</label><InputNumber value={fStage.slaHoras} onValueChange={(e) => setFStage({ ...fStage, slaHoras: e.value })} min={1} useGrouping={false} /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Aprovadores * (usuário ou perfil)</label><InputText value={fStage.aprovadores} onChange={(e) => setFStage({ ...fStage, aprovadores: e.target.value })} /></div>
                    <div className='bc-form-col-12 flex align-items-center gap-2'><Checkbox inputId='wkfExigeTodos' checked={!!fStage.exigeTodos} onChange={(e) => setFStage({ ...fStage, exigeTodos: e.checked })} /><label htmlFor='wkfExigeTodos'>Exige a aprovação de todos</label></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'>
                    <Button label='Fechar' text severity='secondary' onClick={() => setDlgStages(false)} />
                    <Button label='Adicionar etapa' icon='pi pi-plus' onClick={salvarStage} />
                </div>
            </Dialog>

            <Dialog visible={dlgTasks} onHide={() => setDlgTasks(false)} header={'Tarefas — instância ' + (instSel ? instSel.id : '')} modal style={{ width: 'min(96vw, 760px)' }}>
                <DataTable value={tasks} size='small' emptyMessage='Nenhuma tarefa nesta instância.' responsiveLayout='scroll' dataKey='id'>
                    <Column field='stageId' header='Etapa' style={{ width: '5rem' }} />
                    <Column field='responsavel' header='Responsável' />
                    <Column header='Status' body={(r) => <Tag value={r.status} severity={sev(r.status)} />} style={{ width: '9rem' }} />
                    <Column field='slaLimite' header='SLA até' />
                    <Column field='decidedAt' header='Decidido em' />
                    <Column field='comentario' header='Comentário' />
                </DataTable>
            </Dialog>
        </div>
    );
};
export default Workflow;
