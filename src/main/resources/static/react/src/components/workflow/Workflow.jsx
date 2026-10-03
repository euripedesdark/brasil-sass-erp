import React, { useState, useEffect, useRef, useCallback } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
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
                    </DataTable>
                </TabPanel>
                <TabPanel header='Definições'>
                    <DataTable value={defs} loading={loading} paginator rows={10} emptyMessage='Nenhuma definição.' responsiveLayout='scroll' dataKey='id'>
                        <Column field='codigo' header='Código' />
                        <Column field='nome' header='Nome' />
                        <Column field='entidadeAlvo' header='Entidade alvo' />
                        <Column header='Ativa' body={(r) => <Tag value={r.ativo ? 'Sim' : 'Não'} severity={r.ativo ? 'success' : 'secondary'} />} style={{ width: '7rem' }} />
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
        </div>
    );
};
export default Workflow;
