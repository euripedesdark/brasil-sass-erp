import React, {useEffect, useRef, useState} from 'react';
import { useTranslation } from 'react-i18next';
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
import { formatoData } from '../shared/LocaleData.js';

const TIPOS_ORDEM = ['CORRETIVA', 'PREVENTIVA', 'PREDITIVA', 'INSPECAO', 'MELHORIA'];
const PRIORIDADES = ['BAIXA', 'MEDIA', 'ALTA', 'URGENTE'];
const TIPOS_NOTA = ['AVARIA', 'SOLICITACAO', 'ATIVIDADE'];
const SEV = {ABERTA: 'info', LIBERADA: 'warning', EM_EXECUCAO: 'warning', EM_PROCESSAMENTO: 'warning', CONCLUIDA: 'success', ENCERRADA: 'success', CANCELADA: 'danger'};
const tag = s => <Tag value={s} severity={SEV[s] || 'info'}/>;
const prio = p => <Tag value={p} severity={p === 'URGENTE' || p === 'ALTA' ? 'danger' : p === 'MEDIA' ? 'warning' : 'info'}/>;

export default function ManutencaoAtivos() {
    const { t } = useTranslation();
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

    const erro = e => toast.current?.show({severity: 'error', summary: t('ativos.sumErro'), detail: e.message, life: 5000});
    const ok = m => toast.current?.show({severity: 'success', summary: t('ativos.sumOk'), detail: m, life: 3000});
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
        const r = await run(() => form.id ? api(`${url}/${form.id}`, 'PUT', conv) : api(url, 'POST', conv), t('ativos.msgRegistroSalvo'));
        if (r !== undefined) { setDlg(null); load(); }
    };

    const f = dlg?.form || {};
    const setF = v => setDlg({...dlg, form: v});
    const campoAtivo = <div className="col-12 md:col-6"><label>{t('ativos.fAtivoEq')}</label><Dropdown value={f.ativoId} options={ativoOpts} filter onChange={e => setF({...f, ativoId: e.value})} placeholder={t('ativos.phSelecione')}/></div>;
    const dd = (campo, label, opts) => <div className="col-12 md:col-3"><label>{label}</label><Dropdown value={f[campo]} options={opts} onChange={e => setF({...f, [campo]: e.value})}/></div>;
    const data = (campo, label) => <div className="col-12 md:col-3"><label>{label}</label><Calendar value={f[campo]} onChange={e => setF({...f, [campo]: e.value})} dateFormat={formatoData()} showIcon/></div>;
    const num = (campo, label, props = {}) => <div className="col-12 md:col-3"><label>{label}</label><InputNumber value={f[campo]} onValueChange={e => setF({...f, [campo]: e.value})} {...props}/></div>;
    const txt = (campo, label, area) => <div className="col-12"><label>{label}</label>{area
        ? <InputTextarea rows={3} value={f[campo] || ''} onChange={e => setF({...f, [campo]: e.target.value})}/>
        : <InputText value={f[campo] || ''} onChange={e => setF({...f, [campo]: e.target.value})}/>}</div>;
    const moeda = {mode: 'currency', currency: 'BRL', locale: 'pt-BR'};

    const formDlg = () => {
        if (!dlg) return null;
        switch (dlg.tipo) {
            case 'ordem': return <>{campoAtivo}{dd('tipo', t('ativos.hTipo'), TIPOS_ORDEM.map(v => ({label: t('ativos.toOrdem' + v.toLowerCase()), value: v})))}{dd('prioridade', t('ativos.hPri'), PRIORIDADES.map(v => ({label: t('ativos.prio' + v.toLowerCase()), value: v})))}{data('dataProgramada', t('ativos.fProgPara'))}
                {num('custoServico', t('ativos.fServTerc'), moeda)}{txt('descricao', t('ativos.hDescricao'), true)}{txt('checklist', t('ativos.fChecklist'), true)}</>;
            case 'nota': return <>{campoAtivo}{dd('tipo', t('ativos.hTipo'), TIPOS_NOTA.map(v => ({label: t('ativos.toNota' + v.toLowerCase()), value: v})))}{dd('prioridade', t('ativos.hPri'), PRIORIDADES.map(v => ({label: t('ativos.prio' + v.toLowerCase()), value: v})))}{txt('descricao', t('ativos.hDescricao'), true)}{txt('sintoma', t('ativos.fSintoma'))}
                <div className="col-12 flex align-items-center gap-2"><Checkbox inputId="parado" checked={!!f.equipamentoParado} onChange={e => setF({...f, equipamentoParado: e.checked})}/><label htmlFor="parado">{t('ativos.fEqParado')}</label></div></>;
            case 'plano': return <>{campoAtivo}<div className="col-12 md:col-3"><label>{t('ativos.hCodigo')}</label><InputText value={f.codigo || ''} onChange={e => setF({...f, codigo: e.target.value})}/></div>
                {dd('tipoCiclo', t('ativos.hCiclo'), ['TEMPO', 'CONTADOR'].map(v => ({label: t('ativos.ciclo' + v.toLowerCase()), value: v})))}{txt('descricao', t('ativos.hDescricao'))}
                {f.tipoCiclo === 'CONTADOR' ? num('intervaloContador', t('ativos.fIntervaloCont'), {maxFractionDigits: 2}) : <>{num('intervaloDias', t('ativos.fIntervaloDias'))}{data('proximaData', t('ativos.fProxExec'))}</>}
                {num('antecedenciaDias', t('ativos.fAntecedencia'))}{dd('prioridade', t('ativos.hPri'), PRIORIDADES.map(v => ({label: t('ativos.prio' + v.toLowerCase()), value: v})))}{num('horasEstimadas', t('ativos.fHorasEst'), {maxFractionDigits: 2})}{num('custoEstimado', t('ativos.fCustoEst'), moeda)}
                {txt('checklist', t('ativos.fChecklistTarefas'), true)}</>;
            case 'medicao': return <>{campoAtivo}{data('dataMedicao', t('ativos.hData'))}{num('valor', t('ativos.fLeituraCont'), {maxFractionDigits: 2})}{txt('observacao', t('ativos.hObservacao'))}</>;
            default: return null;
        }
    };

    const acoesOrdem = o => {
        const fim = ['CONCLUIDA', 'CANCELADA'].includes(o.status);
        return <div className="flex gap-1">
            <Button icon="pi pi-list" text rounded tooltip={t('ativos.tipMat')} onClick={() => abrirOrdem(o)}/>
            <Button icon="pi pi-lock-open" text rounded tooltip={t('ativos.tipLiberar')} disabled={o.status !== 'ABERTA'} onClick={() => acao(o, 'liberar', t('ativos.msgLiberada'))}/>
            <Button icon="pi pi-play" text rounded tooltip={t('ativos.tipIniciar')} disabled={!['ABERTA', 'LIBERADA'].includes(o.status)} onClick={() => acao(o, 'iniciar', t('ativos.msgIniciada'))}/>
            <Button icon="pi pi-check" text rounded tooltip={t('ativos.tipConcluir')} disabled={fim} onClick={() => setConclusao({ordem: o, form: {}})}/>
            <Button icon="pi pi-ban" text rounded severity="danger" tooltip={t('ativos.tipCancelar')} disabled={fim} onClick={() => acao(o, 'cancelar', t('ativos.msgCancelada'), {})}/>
        </div>;
    };

    return <div className="p-3"><Toast ref={toast}/>
        <Card title={t('ativos.mTitulo')} subTitle={t('ativos.mSubtitulo')}>
            <TabView activeIndex={tab} onTabChange={e => setTab(e.index)}>
                <TabPanel header={t('ativos.tabOrdens')}>
                    <div className="flex justify-content-end mb-3"><Button label={t('ativos.newOrdem')} icon="pi pi-plus" onClick={() => setDlg({tipo: 'ordem', form: {tipo: 'CORRETIVA', prioridade: 'MEDIA', custoServico: 0}})}/></div>
                    <DataTable value={ordens} loading={loading} paginator rows={10} responsiveLayout="scroll" sortField="dataProgramada" emptyMessage={t('ativos.emptyOrdem')}>
                        <Column field="numero" header={t('ativos.hNumero')} sortable/><Column header={t('ativos.hAtivo')} body={r => nomeAtivo(r.ativoId)}/>
                        <Column field="tipo" header={t('ativos.hTipo')} sortable/><Column header={t('ativos.hPri')} body={r => prio(r.prioridade)}/>
                        <Column field="dataProgramada" header={t('ativos.hProg')} sortable/><Column field="horasTrabalhadas" header={t('ativos.hHoras')}/>
                        <Column header={t('ativos.hCusto')} body={r => brl(r.custo)}/><Column header={t('ativos.hStatus')} body={r => tag(r.status)} sortable sortField="status"/>
                        <Column header={t('ativos.hAcoes')} body={acoesOrdem} style={{minWidth: '14rem'}}/>
                    </DataTable>
                </TabPanel>
                <TabPanel header={t('ativos.tabNotas')}>
                    <div className="flex justify-content-end mb-3"><Button label={t('ativos.newNota')} icon="pi pi-plus" onClick={() => setDlg({tipo: 'nota', form: {tipo: 'AVARIA', prioridade: 'MEDIA', equipamentoParado: false}})}/></div>
                    <DataTable value={notas} loading={loading} paginator rows={10} responsiveLayout="scroll" emptyMessage={t('ativos.emptyNota')}>
                        <Column field="numero" header={t('ativos.hNumero')}/><Column field="dataNota" header={t('ativos.hData')}/><Column header={t('ativos.hAtivo')} body={r => nomeAtivo(r.ativoId)}/>
                        <Column field="tipo" header={t('ativos.hTipo')}/><Column header={t('ativos.hPri')} body={r => prio(r.prioridade)}/><Column field="descricao" header={t('ativos.hDescricao')}/>
                        <Column header={t('ativos.hParado')} body={r => r.equipamentoParado ? <Tag value={t('ativos.sim')} severity="danger"/> : t('ativos.nao')}/><Column header={t('ativos.hStatus')} body={r => tag(r.status)}/>
                        <Column header={t('ativos.hAcoes')} body={r => <div className="flex gap-1">
                            <Button icon="pi pi-wrench" text rounded tooltip={t('ativos.tipGerarOrdem')} disabled={r.status !== 'ABERTA'} onClick={() => run(() => api(`/api/ativos/notas/${r.id}/gerar-ordem`, 'POST'), t('ativos.msgOrdemGerada')).then(load)}/>
                            <Button icon="pi pi-check-circle" text rounded tooltip={t('ativos.tipEncerrar')} disabled={r.status === 'ENCERRADA'} onClick={() => run(() => api(`/api/ativos/notas/${r.id}/encerrar`, 'POST', {}), t('ativos.msgNotaEncerrada')).then(load)}/>
                        </div>}/>
                    </DataTable>
                </TabPanel>
                <TabPanel header={t('ativos.tabPlanos')}>
                    <div className="flex justify-content-end gap-2 mb-3">
                        <Button label={t('ativos.btnGerarVencidas')} icon="pi pi-bolt" outlined onClick={() => run(() => api('/api/ativos/planos/gerar-ordens', 'POST')).then(r => { if (r) { ok(t('ativos.msgOrdensGeradas', {n: r.length})); load(); } })}/>
                        <Button label={t('ativos.newPlano')} icon="pi pi-plus" onClick={() => setDlg({tipo: 'plano', form: {tipoCiclo: 'TEMPO', prioridade: 'MEDIA', antecedenciaDias: 0}})}/>
                    </div>
                    <DataTable value={planos} loading={loading} responsiveLayout="scroll" emptyMessage={t('ativos.emptyPlano')}>
                        <Column field="codigo" header={t('ativos.hCodigo')}/><Column field="descricao" header={t('ativos.hDescricao')}/><Column header={t('ativos.hAtivo')} body={r => nomeAtivo(r.ativoId)}/>
                        <Column header={t('ativos.hCiclo')} body={r => r.tipoCiclo === 'CONTADOR' ? t('ativos.aCada', {n: r.intervaloContador}) : t('ativos.aCadaDias', {n: r.intervaloDias})}/>
                        <Column field="ultimaExecucao" header={t('ativos.hUltima')}/><Column header={t('ativos.hProxima')} body={r => r.tipoCiclo === 'CONTADOR' ? `${Number(r.contadorUltimaExecucao || 0) + Number(r.intervaloContador || 0)}` : r.proximaData}/>
                        <Column header={t('ativos.hAcoes')} body={r => <div className="flex gap-1">
                            <Button icon="pi pi-pencil" text rounded onClick={() => setDlg({tipo: 'plano', form: {...r, proximaData: r.proximaData ? new Date(r.proximaData + 'T00:00') : null}})}/>
                            <Button icon="pi pi-trash" text rounded severity="danger" onClick={() => run(() => api(`/api/ativos/planos/${r.id}`, 'DELETE'), t('ativos.msgPlanoExcluido')).then(load)}/>
                        </div>}/>
                    </DataTable>
                </TabPanel>
                <TabPanel header={t('ativos.tabMed')}>
                    <div className="flex justify-content-end mb-3"><Button label={t('ativos.newMed')} icon="pi pi-plus" onClick={() => setDlg({tipo: 'medicao', form: {dataMedicao: new Date()}})}/></div>
                    <DataTable value={medicoes} loading={loading} paginator rows={10} responsiveLayout="scroll" emptyMessage={t('ativos.emptyMed')}>
                        <Column field="dataMedicao" header={t('ativos.hData')}/><Column header={t('ativos.hAtivo')} body={r => nomeAtivo(r.ativoId)}/>
                        <Column field="valor" header={t('ativos.hLeitura')}/><Column field="unidade" header={t('ativos.hUnidade')}/><Column field="observacao" header={t('ativos.hObservacao')}/>
                    </DataTable>
                </TabPanel>
                <TabPanel header={t('ativos.tabHist')}>
                    <div className="flex gap-2 mb-3 align-items-end">
                        <div style={{minWidth: '22rem'}}><label className="block">{t('ativos.hAtivo')}</label>
                            <Dropdown className="w-full" options={ativos.map(a => ({label: `${a.codigo} - ${a.descricao}`, value: a.id}))} filter value={historico?.ativoId}
                                onChange={e => run(() => api(`/api/ativos/${e.value}/historico-manutencao`)).then(h => h && setHistorico(h))}/></div>
                    </div>
                    {historico && <>
                        <div className="grid mb-3">
                            {[[t('ativos.kOrdAbertas'), historico.ordensAbertas], [t('ativos.kConcluidas'), historico.ordensConcluidas], [t('ativos.kCustoTotal'), brl(historico.custoTotal)],
                              [t('ativos.kMateriais'), brl(historico.custoMaterial)], [t('ativos.kMaoObra'), brl(historico.custoMaoObra)], [t('ativos.kServicos'), brl(historico.custoServico)],
                              [t('ativos.kHorasTrab'), historico.horasTrabalhadas], [t('ativos.kHorasParada'), historico.horasParada]].map(([k, v]) =>
                                <div className="col-6 md:col-3" key={k}><Card><div className="text-500">{k}</div><div className="text-xl font-bold">{v}</div></Card></div>)}
                        </div>
                        <DataTable value={historico.ordens} responsiveLayout="scroll">
                            <Column field="numero" header={t('ativos.hOrdem')}/><Column field="tipo" header={t('ativos.hTipo')}/><Column field="dataConclusao" header={t('ativos.hConclusao')}/>
                            <Column field="causa" header={t('ativos.hCausa')}/><Column field="solucao" header={t('ativos.hSolucao')}/><Column header={t('ativos.hCusto')} body={r => brl(r.custo)}/><Column header={t('ativos.hStatus')} body={r => tag(r.status)}/>
                        </DataTable>
                    </>}
                </TabPanel>
            </TabView>
        </Card>

        <Dialog header={{ordem: t('ativos.dlgOrdem'), nota: t('ativos.tabNotas'), plano: t('ativos.dlgPlano'), medicao: t('ativos.dlgMed')}[dlg?.tipo] || ''}
                visible={!!dlg} onHide={() => setDlg(null)} modal style={{width: 'min(860px,95vw)'}}>
            <div className="grid p-fluid">{formDlg()}</div>
            <div className="flex justify-content-end mt-3"><Button label={t('ativos.btnSalvar')} icon="pi pi-save" onClick={salvar}/></div>
        </Dialog>

        <Dialog header={conclusao ? `${t('ativos.dlgConcluir')} ${conclusao.ordem.numero}` : ''} visible={!!conclusao} onHide={() => setConclusao(null)} modal style={{width: 'min(640px,95vw)'}}>
            {conclusao && <div className="grid p-fluid">
                <div className="col-12 md:col-6"><label>{t('ativos.fDataConcl')}</label><Calendar value={conclusao.form.data} onChange={e => setConclusao({...conclusao, form: {...conclusao.form, data: e.value}})} dateFormat={formatoData()} showIcon/></div>
                <div className="col-12 md:col-6"><label>{t('ativos.kHorasParada')}</label><InputNumber value={conclusao.form.horasParada} onValueChange={e => setConclusao({...conclusao, form: {...conclusao.form, horasParada: e.value}})} maxFractionDigits={2}/></div>
                <div className="col-12"><label>{t('ativos.hCausa')}</label><InputText value={conclusao.form.causa || ''} onChange={e => setConclusao({...conclusao, form: {...conclusao.form, causa: e.target.value}})}/></div>
                <div className="col-12"><label>{t('ativos.fSolAplicada')}</label><InputTextarea rows={3} value={conclusao.form.solucao || ''} onChange={e => setConclusao({...conclusao, form: {...conclusao.form, solucao: e.target.value}})}/></div>
            </div>}
            <div className="flex justify-content-end mt-3"><Button label={t('ativos.btnConcluir')} icon="pi pi-check" onClick={async () => {
                const r = await acao(conclusao.ordem, 'concluir', t('ativos.msgConcluida'), {...conclusao.form, data: isoDate(conclusao.form.data)});
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
                <h4>{t('ativos.kMateriais')}</h4>
                <DataTable value={itens.materiais} responsiveLayout="scroll" emptyMessage={t('ativos.emptyMat')}>
                    <Column field="descricao" header={t('ativos.hMaterial')}/><Column field="produtoId" header={t('ativos.hProduto')}/><Column field="quantidade" header={t('ativos.hQtd')}/>
                    <Column header={t('ativos.hUnitario')} body={r => brl(r.custoUnitario)}/><Column header={t('ativos.hTotal')} body={r => brl(r.custoTotal)}/>
                    <Column body={r => <Button icon="pi pi-trash" text rounded severity="danger" disabled={['CONCLUIDA', 'CANCELADA'].includes(ordem.status)}
                        onClick={() => run(() => api(`/api/ativos/manutencoes/${ordem.id}/materiais/${r.id}`, 'DELETE')).then(recarregarOrdem)}/>}/>
                </DataTable>
                {!['CONCLUIDA', 'CANCELADA'].includes(ordem.status) && <div className="flex gap-2 align-items-end mt-2 flex-wrap p-fluid">
                    <div style={{flex: 2}}><label>{t('ativos.hDescricao')}</label><InputText value={novoMat.descricao || ''} onChange={e => setNovoMat({...novoMat, descricao: e.target.value})}/></div>
                    <div style={{flex: 1}}><label>{t('ativos.fProdutoID')}</label><InputNumber value={novoMat.produtoId} onValueChange={e => setNovoMat({...novoMat, produtoId: e.value})} useGrouping={false}/></div>
                    <div style={{flex: 1}}><label>{t('ativos.hQtd')}</label><InputNumber value={novoMat.quantidade} onValueChange={e => setNovoMat({...novoMat, quantidade: e.value})} maxFractionDigits={4}/></div>
                    <div style={{flex: 1}}><label>{t('ativos.fCustoUnit')}</label><InputNumber value={novoMat.custoUnitario} onValueChange={e => setNovoMat({...novoMat, custoUnitario: e.value})} {...moeda}/></div>
                    <Button icon="pi pi-plus" onClick={() => run(() => api(`/api/ativos/manutencoes/${ordem.id}/materiais`, 'POST', novoMat), t('ativos.msgMaterialIncluido')).then(r => r !== undefined && recarregarOrdem())}/>
                </div>}
                <h4>{t('ativos.hAponthoras')}</h4>
                <DataTable value={itens.apontamentos} responsiveLayout="scroll" emptyMessage={t('ativos.emptyApon')}>
                    <Column field="dataApontamento" header={t('ativos.hData')}/><Column field="funcionarioId" header={t('ativos.hFunc')}/><Column field="horas" header={t('ativos.hHoras')}/>
                    <Column header={t('ativos.hCustoh')} body={r => brl(r.custoHora)}/><Column header={t('ativos.hTotal')} body={r => brl(r.custoTotal)}/><Column field="descricao" header={t('ativos.hAtividade')}/>
                    <Column body={r => <Button icon="pi pi-trash" text rounded severity="danger" disabled={['CONCLUIDA', 'CANCELADA'].includes(ordem.status)}
                        onClick={() => run(() => api(`/api/ativos/manutencoes/${ordem.id}/apontamentos/${r.id}`, 'DELETE')).then(recarregarOrdem)}/>}/>
                </DataTable>
                {!['CONCLUIDA', 'CANCELADA'].includes(ordem.status) && <div className="flex gap-2 align-items-end mt-2 flex-wrap p-fluid">
                    <div style={{flex: 1}}><label>{t('ativos.fFuncID')}</label><InputNumber value={novoAp.funcionarioId} onValueChange={e => setNovoAp({...novoAp, funcionarioId: e.value})} useGrouping={false}/></div>
                    <div style={{flex: 1}}><label>{t('ativos.hHoras')}</label><InputNumber value={novoAp.horas} onValueChange={e => setNovoAp({...novoAp, horas: e.value})} maxFractionDigits={2}/></div>
                    <div style={{flex: 1}}><label>{t('ativos.fCustohora')}</label><InputNumber value={novoAp.custoHora} onValueChange={e => setNovoAp({...novoAp, custoHora: e.value})} {...moeda}/></div>
                    <div style={{flex: 2}}><label>{t('ativos.hAtividade')}</label><InputText value={novoAp.descricao || ''} onChange={e => setNovoAp({...novoAp, descricao: e.target.value})}/></div>
                    <Button icon="pi pi-plus" onClick={() => run(() => api(`/api/ativos/manutencoes/${ordem.id}/apontamentos`, 'POST', novoAp), t('ativos.msgHorasApontadas')).then(r => r !== undefined && recarregarOrdem())}/>
                </div>}
            </>}
        </Dialog>
    </div>;
}
