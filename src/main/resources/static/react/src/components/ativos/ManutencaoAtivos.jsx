import React, {useEffect, useRef, useState} from 'react';
import {Card} from 'primereact/card';
import {TabView, TabPanel} from 'primereact/tabview';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column';
import {Button} from 'primereact/button';
import {Dialog} from 'primereact/dialog';
import {InputText} from 'primereact/inputtext';
import {InputTextarea} from 'primereact/inputtextarea';
import {InputNumber} from 'primereact/inputnumber';
import {Dropdown} from 'primereact/dropdown';
import {Calendar} from 'primereact/calendar';
import {Checkbox} from 'primereact/checkbox';
import {Toast} from 'primereact/toast';
import {Tag} from 'primereact/tag';
import {api, brl, isoDate} from './ativosApi';

const TIPOS_ORDEM = ['CORRETIVA', 'PREVENTIVA', 'PREDITIVA', 'INSPECAO', 'MELHORIA'];
const PRIORIDADES = ['BAIXA', 'MEDIA', 'ALTA', 'URGENTE'];
const TIPOS_NOTA = ['AVARIA', 'SOLICITACAO', 'ATIVIDADE'];
const SEV = {ABERTA: 'info', LIBERADA: 'warning', EM_EXECUCAO: 'warning', EM_PROCESSAMENTO: 'warning', CONCLUIDA: 'success', ENCERRADA: 'success', CANCELADA: 'danger'};
const tag = s => <Tag value={s} severity={SEV[s] || 'info'}/>;
const prio = p => <Tag value={p} severity={p === 'URGENTE' || p === 'ALTA' ? 'danger' : p === 'MEDIA' ? 'warning' : 'info'}/>;

export default function ManutencaoAtivos() {
    const toast = useRef(null);
    const [tab, setTab] = useState(0);
    const [ativos, setAtivos] = useState([]);
    const [ordens, setOrdens] = useState([]);
    const [notas, setNotas] = useState([]);
    const [planos, setPlanos] = useState([]);
    const [medicoes, setMedicoes] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dlg, setDlg] = useState(null);           // {tipo, form}
    const [ordem, setOrdem] = useState(null);       // detalhe da ordem
    const [itens, setItens] = useState({materiais: [], apontamentos: []});
    const [novoMat, setNovoMat] = useState({});
    const [novoAp, setNovoAp] = useState({});
    const [conclusao, setConclusao] = useState(null);
    const [historico, setHistorico] = useState(null);

    const erro = e => toast.current?.show({severity: 'error', summary: 'Erro', detail: e.message, life: 5000});
    const ok = m => toast.current?.show({severity: 'success', summary: 'OK', detail: m, life: 3000});
    const run = async (fn, msg) => { try { const r = await fn(); if (msg) ok(msg); return r; } catch (e) { erro(e); return undefined; } };

    const load = async () => {
        setLoading(true);
        try {
            const [a, o, n, p, m] = await Promise.all(['/api/ativos', '/api/ativos/manutencoes', '/api/ativos/notas', '/api/ativos/planos', '/api/ativos/medicoes'].map(u => api(u)));
            setAtivos(a); setOrdens(o); setNotas(n); setPlanos(p); setMedicoes(m);
        } catch (e) { erro(e); } finally { setLoading(false); }
    };
    useEffect(() => { load(); }, []);

    const ativoOpts = ativos.filter(a => a.status !== 'BAIXADO').map(a => ({label: `${a.codigo} - ${a.descricao}`, value: a.id}));
    const nomeAtivo = id => { const a = ativos.find(x => x.id === id); return a ? `${a.codigo} - ${a.descricao}` : id; };

    const abrirOrdem = async o => {
        const r = await run(async () => ({
            materiais: await api(`/api/ativos/manutencoes/${o.id}/materiais`),
            apontamentos: await api(`/api/ativos/manutencoes/${o.id}/apontamentos`)
        }));
        if (r) { setItens(r); setOrdem(await api(`/api/ativos/manutencoes/${o.id}`)); setNovoMat({}); setNovoAp({}); }
    };
    const recarregarOrdem = async () => { if (ordem) await abrirOrdem(ordem); load(); };
    const acao = (o, verbo, msg, body) => run(() => api(`/api/ativos/manutencoes/${o.id}/${verbo}`, 'POST', body), msg).then(load);

    const salvar = async () => {
        const {tipo, form} = dlg;
        const conv = {...form};
        ['dataProgramada', 'dataMedicao', 'dataNota', 'proximaData', 'ultimaExecucao'].forEach(k => { if (conv[k] instanceof Date) conv[k] = isoDate(conv[k]); });
        const url = {ordem: '/api/ativos/manutencoes', nota: '/api/ativos/notas', plano: '/api/ativos/planos', medicao: '/api/ativos/medicoes'}[tipo];
        const r = await run(() => form.id ? api(`${url}/${form.id}`, 'PUT', conv) : api(url, 'POST', conv), 'Registro salvo');
        if (r !== undefined) { setDlg(null); load(); }
    };

    const f = dlg?.form || {};
    const setF = v => setDlg({...dlg, form: v});
    const campoAtivo = <div className="col-12 md:col-6"><label>Ativo / equipamento</label><Dropdown value={f.ativoId} options={ativoOpts} filter onChange={e => setF({...f, ativoId: e.value})} placeholder="Selecione"/></div>;
    const dd = (campo, label, opts) => <div className="col-12 md:col-3"><label>{label}</label><Dropdown value={f[campo]} options={opts} onChange={e => setF({...f, [campo]: e.value})}/></div>;
    const data = (campo, label) => <div className="col-12 md:col-3"><label>{label}</label><Calendar value={f[campo]} onChange={e => setF({...f, [campo]: e.value})} dateFormat="dd/mm/yy" showIcon/></div>;
    const num = (campo, label, props = {}) => <div className="col-12 md:col-3"><label>{label}</label><InputNumber value={f[campo]} onValueChange={e => setF({...f, [campo]: e.value})} {...props}/></div>;
    const txt = (campo, label, area) => <div className="col-12"><label>{label}</label>{area
        ? <InputTextarea rows={3} value={f[campo] || ''} onChange={e => setF({...f, [campo]: e.target.value})}/>
        : <InputText value={f[campo] || ''} onChange={e => setF({...f, [campo]: e.target.value})}/>}</div>;
    const moeda = {mode: 'currency', currency: 'BRL', locale: 'pt-BR'};

    const formDlg = () => {
        if (!dlg) return null;
        switch (dlg.tipo) {
            case 'ordem': return <>{campoAtivo}{dd('tipo', 'Tipo', TIPOS_ORDEM)}{dd('prioridade', 'Prioridade', PRIORIDADES)}{data('dataProgramada', 'Programada para')}
                {num('custoServico', 'Serviços de terceiros', moeda)}{txt('descricao', 'Descrição', true)}{txt('checklist', 'Checklist / instruções', true)}</>;
            case 'nota': return <>{campoAtivo}{dd('tipo', 'Tipo', TIPOS_NOTA)}{dd('prioridade', 'Prioridade', PRIORIDADES)}{txt('descricao', 'Descrição', true)}{txt('sintoma', 'Sintoma')}
                <div className="col-12 flex align-items-center gap-2"><Checkbox inputId="parado" checked={!!f.equipamentoParado} onChange={e => setF({...f, equipamentoParado: e.checked})}/><label htmlFor="parado">Equipamento parado</label></div></>;
            case 'plano': return <>{campoAtivo}<div className="col-12 md:col-3"><label>Código</label><InputText value={f.codigo || ''} onChange={e => setF({...f, codigo: e.target.value})}/></div>
                {dd('tipoCiclo', 'Ciclo', ['TEMPO', 'CONTADOR'])}{txt('descricao', 'Descrição')}
                {f.tipoCiclo === 'CONTADOR' ? num('intervaloContador', 'Intervalo do contador', {maxFractionDigits: 2}) : <>{num('intervaloDias', 'Intervalo (dias)')}{data('proximaData', 'Próxima execução')}</>}
                {num('antecedenciaDias', 'Antecedência (dias)')}{dd('prioridade', 'Prioridade', PRIORIDADES)}{num('horasEstimadas', 'Horas estimadas', {maxFractionDigits: 2})}{num('custoEstimado', 'Custo estimado', moeda)}
                {txt('checklist', 'Checklist / lista de tarefas', true)}</>;
            case 'medicao': return <>{campoAtivo}{data('dataMedicao', 'Data')}{num('valor', 'Leitura do contador', {maxFractionDigits: 2})}{txt('observacao', 'Observação')}</>;
            default: return null;
        }
    };

    const acoesOrdem = o => {
        const fim = ['CONCLUIDA', 'CANCELADA'].includes(o.status);
        return <div className="flex gap-1">
            <Button icon="pi pi-list" text rounded tooltip="Materiais e horas" onClick={() => abrirOrdem(o)}/>
            <Button icon="pi pi-lock-open" text rounded tooltip="Liberar" disabled={o.status !== 'ABERTA'} onClick={() => acao(o, 'liberar', 'Ordem liberada')}/>
            <Button icon="pi pi-play" text rounded tooltip="Iniciar" disabled={!['ABERTA', 'LIBERADA'].includes(o.status)} onClick={() => acao(o, 'iniciar', 'Ordem iniciada')}/>
            <Button icon="pi pi-check" text rounded tooltip="Concluir" disabled={fim} onClick={() => setConclusao({ordem: o, form: {}})}/>
            <Button icon="pi pi-ban" text rounded severity="danger" tooltip="Cancelar" disabled={fim} onClick={() => acao(o, 'cancelar', 'Ordem cancelada', {})}/>
        </div>;
    };

    return <div className="p-3"><Toast ref={toast}/>
        <Card title="Manutenção de ativos" subTitle="Notas, ordens com materiais e horas, planos preventivos e contadores (PM)">
            <TabView activeIndex={tab} onTabChange={e => setTab(e.index)}>
                <TabPanel header="Ordens">
                    <div className="flex justify-content-end mb-3"><Button label="Nova ordem" icon="pi pi-plus" onClick={() => setDlg({tipo: 'ordem', form: {tipo: 'CORRETIVA', prioridade: 'MEDIA', custoServico: 0}})}/></div>
                    <DataTable value={ordens} loading={loading} paginator rows={10} responsiveLayout="scroll" sortField="dataProgramada" emptyMessage="Nenhuma ordem">
                        <Column field="numero" header="Número" sortable/><Column header="Ativo" body={r => nomeAtivo(r.ativoId)}/>
                        <Column field="tipo" header="Tipo" sortable/><Column header="Prioridade" body={r => prio(r.prioridade)}/>
                        <Column field="dataProgramada" header="Programada" sortable/><Column field="horasTrabalhadas" header="Horas"/>
                        <Column header="Custo" body={r => brl(r.custo)}/><Column header="Status" body={r => tag(r.status)} sortable sortField="status"/>
                        <Column header="Ações" body={acoesOrdem} style={{minWidth: '14rem'}}/>
                    </DataTable>
                </TabPanel>
                <TabPanel header="Notas de manutenção">
                    <div className="flex justify-content-end mb-3"><Button label="Nova nota" icon="pi pi-plus" onClick={() => setDlg({tipo: 'nota', form: {tipo: 'AVARIA', prioridade: 'MEDIA', equipamentoParado: false}})}/></div>
                    <DataTable value={notas} loading={loading} paginator rows={10} responsiveLayout="scroll" emptyMessage="Nenhuma nota">
                        <Column field="numero" header="Número"/><Column field="dataNota" header="Data"/><Column header="Ativo" body={r => nomeAtivo(r.ativoId)}/>
                        <Column field="tipo" header="Tipo"/><Column header="Prioridade" body={r => prio(r.prioridade)}/><Column field="descricao" header="Descrição"/>
                        <Column header="Parado" body={r => r.equipamentoParado ? <Tag value="Sim" severity="danger"/> : 'Não'}/><Column header="Status" body={r => tag(r.status)}/>
                        <Column header="Ações" body={r => <div className="flex gap-1">
                            <Button icon="pi pi-wrench" text rounded tooltip="Gerar ordem" disabled={r.status !== 'ABERTA'} onClick={() => run(() => api(`/api/ativos/notas/${r.id}/gerar-ordem`, 'POST'), 'Ordem gerada').then(load)}/>
                            <Button icon="pi pi-check-circle" text rounded tooltip="Encerrar" disabled={r.status === 'ENCERRADA'} onClick={() => run(() => api(`/api/ativos/notas/${r.id}/encerrar`, 'POST', {}), 'Nota encerrada').then(load)}/>
                        </div>}/>
                    </DataTable>
                </TabPanel>
                <TabPanel header="Planos preventivos">
                    <div className="flex justify-content-end gap-2 mb-3">
                        <Button label="Gerar ordens vencidas" icon="pi pi-bolt" outlined onClick={() => run(() => api('/api/ativos/planos/gerar-ordens', 'POST')).then(r => { if (r) { ok(`${r.length} ordem(ns) gerada(s)`); load(); } })}/>
                        <Button label="Novo plano" icon="pi pi-plus" onClick={() => setDlg({tipo: 'plano', form: {tipoCiclo: 'TEMPO', prioridade: 'MEDIA', antecedenciaDias: 0}})}/>
                    </div>
                    <DataTable value={planos} loading={loading} responsiveLayout="scroll" emptyMessage="Nenhum plano">
                        <Column field="codigo" header="Código"/><Column field="descricao" header="Descrição"/><Column header="Ativo" body={r => nomeAtivo(r.ativoId)}/>
                        <Column header="Ciclo" body={r => r.tipoCiclo === 'CONTADOR' ? `a cada ${r.intervaloContador}` : `a cada ${r.intervaloDias} dias`}/>
                        <Column field="ultimaExecucao" header="Última"/><Column header="Próxima" body={r => r.tipoCiclo === 'CONTADOR' ? `${Number(r.contadorUltimaExecucao || 0) + Number(r.intervaloContador || 0)}` : r.proximaData}/>
                        <Column header="Ações" body={r => <div className="flex gap-1">
                            <Button icon="pi pi-pencil" text rounded onClick={() => setDlg({tipo: 'plano', form: {...r, proximaData: r.proximaData ? new Date(r.proximaData + 'T00:00') : null}})}/>
                            <Button icon="pi pi-trash" text rounded severity="danger" onClick={() => run(() => api(`/api/ativos/planos/${r.id}`, 'DELETE'), 'Plano excluído').then(load)}/>
                        </div>}/>
                    </DataTable>
                </TabPanel>
                <TabPanel header="Medições / contadores">
                    <div className="flex justify-content-end mb-3"><Button label="Nova medição" icon="pi pi-plus" onClick={() => setDlg({tipo: 'medicao', form: {dataMedicao: new Date()}})}/></div>
                    <DataTable value={medicoes} loading={loading} paginator rows={10} responsiveLayout="scroll" emptyMessage="Nenhuma medição">
                        <Column field="dataMedicao" header="Data"/><Column header="Ativo" body={r => nomeAtivo(r.ativoId)}/>
                        <Column field="valor" header="Leitura"/><Column field="unidade" header="Unidade"/><Column field="observacao" header="Observação"/>
                    </DataTable>
                </TabPanel>
                <TabPanel header="Histórico por ativo">
                    <div className="flex gap-2 mb-3 align-items-end">
                        <div style={{minWidth: '22rem'}}><label className="block">Ativo</label>
                            <Dropdown className="w-full" options={ativos.map(a => ({label: `${a.codigo} - ${a.descricao}`, value: a.id}))} filter value={historico?.ativoId}
                                onChange={e => run(() => api(`/api/ativos/${e.value}/historico-manutencao`)).then(h => h && setHistorico(h))}/></div>
                    </div>
                    {historico && <>
                        <div className="grid mb-3">
                            {[['Ordens abertas', historico.ordensAbertas], ['Concluídas', historico.ordensConcluidas], ['Custo total', brl(historico.custoTotal)],
                              ['Materiais', brl(historico.custoMaterial)], ['Mão de obra', brl(historico.custoMaoObra)], ['Serviços', brl(historico.custoServico)],
                              ['Horas trabalhadas', historico.horasTrabalhadas], ['Horas de parada', historico.horasParada]].map(([k, v]) =>
                                <div className="col-6 md:col-3" key={k}><Card><div className="text-500">{k}</div><div className="text-xl font-bold">{v}</div></Card></div>)}
                        </div>
                        <DataTable value={historico.ordens} responsiveLayout="scroll">
                            <Column field="numero" header="Ordem"/><Column field="tipo" header="Tipo"/><Column field="dataConclusao" header="Conclusão"/>
                            <Column field="causa" header="Causa"/><Column field="solucao" header="Solução"/><Column header="Custo" body={r => brl(r.custo)}/><Column header="Status" body={r => tag(r.status)}/>
                        </DataTable>
                    </>}
                </TabPanel>
            </TabView>
        </Card>

        <Dialog header={{ordem: 'Ordem de manutenção', nota: 'Nota de manutenção', plano: 'Plano preventivo', medicao: 'Medição de contador'}[dlg?.tipo] || ''}
                visible={!!dlg} onHide={() => setDlg(null)} modal style={{width: 'min(860px,95vw)'}}>
            <div className="grid p-fluid">{formDlg()}</div>
            <div className="flex justify-content-end mt-3"><Button label="Salvar" icon="pi pi-save" onClick={salvar}/></div>
        </Dialog>

        <Dialog header={conclusao ? `Concluir ${conclusao.ordem.numero}` : ''} visible={!!conclusao} onHide={() => setConclusao(null)} modal style={{width: 'min(640px,95vw)'}}>
            {conclusao && <div className="grid p-fluid">
                <div className="col-12 md:col-6"><label>Data de conclusão</label><Calendar value={conclusao.form.data} onChange={e => setConclusao({...conclusao, form: {...conclusao.form, data: e.value}})} dateFormat="dd/mm/yy" showIcon/></div>
                <div className="col-12 md:col-6"><label>Horas de parada</label><InputNumber value={conclusao.form.horasParada} onValueChange={e => setConclusao({...conclusao, form: {...conclusao.form, horasParada: e.value}})} maxFractionDigits={2}/></div>
                <div className="col-12"><label>Causa</label><InputText value={conclusao.form.causa || ''} onChange={e => setConclusao({...conclusao, form: {...conclusao.form, causa: e.target.value}})}/></div>
                <div className="col-12"><label>Solução aplicada</label><InputTextarea rows={3} value={conclusao.form.solucao || ''} onChange={e => setConclusao({...conclusao, form: {...conclusao.form, solucao: e.target.value}})}/></div>
            </div>}
            <div className="flex justify-content-end mt-3"><Button label="Concluir" icon="pi pi-check" onClick={async () => {
                const r = await acao(conclusao.ordem, 'concluir', 'Ordem concluída', {...conclusao.form, data: isoDate(conclusao.form.data)});
                if (r !== undefined) setConclusao(null);
            }}/></div>
        </Dialog>

        <Dialog header={ordem ? `${ordem.numero} — ${nomeAtivo(ordem.ativoId)}` : ''} visible={!!ordem} onHide={() => setOrdem(null)} modal style={{width: 'min(980px,96vw)'}}>
            {ordem && <>
                <div className="grid mb-2">
                    <div className="col-6 md:col-3">Status: {tag(ordem.status)}</div>
                    <div className="col-6 md:col-3">Materiais: <strong>{brl(ordem.custoMaterial)}</strong></div>
                    <div className="col-6 md:col-3">Mão de obra: <strong>{brl(ordem.custoMaoObra)}</strong> ({ordem.horasTrabalhadas} h)</div>
                    <div className="col-6 md:col-3">Total: <strong>{brl(ordem.custo)}</strong></div>
                    {ordem.checklist && <div className="col-12"><small className="text-500" style={{whiteSpace: 'pre-wrap'}}>{ordem.checklist}</small></div>}
                </div>
                <h4>Materiais</h4>
                <DataTable value={itens.materiais} responsiveLayout="scroll" emptyMessage="Sem materiais">
                    <Column field="descricao" header="Material"/><Column field="produtoId" header="Produto"/><Column field="quantidade" header="Qtd."/>
                    <Column header="Unitário" body={r => brl(r.custoUnitario)}/><Column header="Total" body={r => brl(r.custoTotal)}/>
                    <Column body={r => <Button icon="pi pi-trash" text rounded severity="danger" disabled={['CONCLUIDA', 'CANCELADA'].includes(ordem.status)}
                        onClick={() => run(() => api(`/api/ativos/manutencoes/${ordem.id}/materiais/${r.id}`, 'DELETE')).then(recarregarOrdem)}/>}/>
                </DataTable>
                {!['CONCLUIDA', 'CANCELADA'].includes(ordem.status) && <div className="flex gap-2 align-items-end mt-2 flex-wrap p-fluid">
                    <div style={{flex: 2}}><label>Descrição</label><InputText value={novoMat.descricao || ''} onChange={e => setNovoMat({...novoMat, descricao: e.target.value})}/></div>
                    <div style={{flex: 1}}><label>Produto (ID)</label><InputNumber value={novoMat.produtoId} onValueChange={e => setNovoMat({...novoMat, produtoId: e.value})} useGrouping={false}/></div>
                    <div style={{flex: 1}}><label>Qtd.</label><InputNumber value={novoMat.quantidade} onValueChange={e => setNovoMat({...novoMat, quantidade: e.value})} maxFractionDigits={4}/></div>
                    <div style={{flex: 1}}><label>Custo unit.</label><InputNumber value={novoMat.custoUnitario} onValueChange={e => setNovoMat({...novoMat, custoUnitario: e.value})} {...moeda}/></div>
                    <Button icon="pi pi-plus" onClick={() => run(() => api(`/api/ativos/manutencoes/${ordem.id}/materiais`, 'POST', novoMat), 'Material incluído').then(r => r !== undefined && recarregarOrdem())}/>
                </div>}
                <h4>Apontamento de horas</h4>
                <DataTable value={itens.apontamentos} responsiveLayout="scroll" emptyMessage="Sem apontamentos">
                    <Column field="dataApontamento" header="Data"/><Column field="funcionarioId" header="Funcionário"/><Column field="horas" header="Horas"/>
                    <Column header="Custo/h" body={r => brl(r.custoHora)}/><Column header="Total" body={r => brl(r.custoTotal)}/><Column field="descricao" header="Atividade"/>
                    <Column body={r => <Button icon="pi pi-trash" text rounded severity="danger" disabled={['CONCLUIDA', 'CANCELADA'].includes(ordem.status)}
                        onClick={() => run(() => api(`/api/ativos/manutencoes/${ordem.id}/apontamentos/${r.id}`, 'DELETE')).then(recarregarOrdem)}/>}/>
                </DataTable>
                {!['CONCLUIDA', 'CANCELADA'].includes(ordem.status) && <div className="flex gap-2 align-items-end mt-2 flex-wrap p-fluid">
                    <div style={{flex: 1}}><label>Funcionário (ID)</label><InputNumber value={novoAp.funcionarioId} onValueChange={e => setNovoAp({...novoAp, funcionarioId: e.value})} useGrouping={false}/></div>
                    <div style={{flex: 1}}><label>Horas</label><InputNumber value={novoAp.horas} onValueChange={e => setNovoAp({...novoAp, horas: e.value})} maxFractionDigits={2}/></div>
                    <div style={{flex: 1}}><label>Custo/hora</label><InputNumber value={novoAp.custoHora} onValueChange={e => setNovoAp({...novoAp, custoHora: e.value})} {...moeda}/></div>
                    <div style={{flex: 2}}><label>Atividade</label><InputText value={novoAp.descricao || ''} onChange={e => setNovoAp({...novoAp, descricao: e.target.value})}/></div>
                    <Button icon="pi pi-plus" onClick={() => run(() => api(`/api/ativos/manutencoes/${ordem.id}/apontamentos`, 'POST', novoAp), 'Horas apontadas').then(r => r !== undefined && recarregarOrdem())}/>
                </div>}
            </>}
        </Dialog>
    </div>;
}
