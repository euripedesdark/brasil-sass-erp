import React, {useEffect, useRef, useState} from 'react';
import { localeAtivo } from '../shared/LocaleData.js';
import { useTranslation } from 'react-i18next';
import {Card} from 'primereact/card';
import {TabView, TabPanel} from 'primereact/tabview';
import {DataTable} from 'primereact/datatable';
import {Column} from 'primereact/column';
import {Button} from 'primereact/button';
import {Dialog} from 'primereact/dialog';
import {InputText} from 'primereact/inputtext';
import {InputNumber} from 'primereact/inputnumber';
import {Dropdown} from 'primereact/dropdown';
import {Calendar} from 'primereact/calendar';
import {Checkbox} from 'primereact/checkbox';
import {Toast} from 'primereact/toast';
import {Tag} from 'primereact/tag';
import {api, opcional, brl, isoDate, periodoAtual} from './ativosApi';
import { formatoData } from '../shared/LocaleData.js';

const METODOS = [
    {label: 'ativos.metLinear', value: 'LINEAR'},
    {label: 'ativos.metSoma', value: 'SOMA_DIGITOS'},
    {label: 'ativos.metSaldo', value: 'SALDO_DECRESCENTE'}
];
const metodosOpts = (t) => METODOS.map((m) => ({label: t(m.label), value: m.value}));
const CONTAS_CLASSE = [
    ['contaAtivoId', 'ativos.ccAtivo'],
    ['contaDepreciacaoAcumuladaId', 'ativos.ccAcum'],
    ['contaDespesaDepreciacaoId', 'ativos.ccDesp'],
    ['contaGanhoBaixaId', 'ativos.ccGanho'],
    ['contaPerdaBaixaId', 'ativos.ccPerda'],
    ['contaReavaliacaoId', 'ativos.ccReav'],
    ['contaImpairmentId', 'ativos.ccImp']
];
const OPERACOES = {
    adicao: {titulo: 'ativos.opAdicao', url: 'adicao'},
    transferir: {titulo: 'ativos.opTransf', url: 'transferir'},
    reavaliar: {titulo: 'ativos.opReav', url: 'reavaliar'},
    impairment: {titulo: 'ativos.opImp', url: 'impairment'},
    baixar: {titulo: 'ativos.opBaixa', url: 'baixar'}
};
const statusTag = s => <Tag value={s} severity={s === 'BAIXADO' || s === 'ESTORNADA' ? 'danger' : 'success'}/>;

export default function Ativos() {
    const { t } = useTranslation();
    const toast = useRef(null);
    const [tab, setTab] = useState(0);
    const [ativos, setAtivos] = useState([]);
    const [classes, setClasses] = useState([]);
    const [contas, setContas] = useState([]);
    const [centros, setCentros] = useState([]);
    const [execucoes, setExecucoes] = useState([]);
    const [loading, setLoading] = useState(false);
    const [ativoDlg, setAtivoDlg] = useState(null);
    const [classeDlg, setClasseDlg] = useState(null);
    const [op, setOp] = useState(null);
    const [detalhe, setDetalhe] = useState(null);
    const [periodo, setPeriodo] = useState(periodoAtual());
    const [simulacao, setSimulacao] = useState([]);
    const [posicao, setPosicao] = useState(null);
    const [projecao, setProjecao] = useState([]);

    const erro = e => toast.current?.show({severity: 'error', summary: t('ativos.sumErro'), detail: e.message, life: 5000});
    const ok = m => toast.current?.show({severity: 'success', summary: t('ativos.sumOk'), detail: m, life: 3000});
    const run = async (fn, msg) => { try { const r = await fn(); if (msg) ok(msg); return r; } catch (e) { erro(e); return undefined; } };

    const load = async () => {
        setLoading(true);
        try {
            const [a, c, x] = await Promise.all([api('/api/ativos'), api('/api/ativos/classes'), api('/api/ativos/depreciacao/execucoes')]);
            setAtivos(a); setClasses(c); setExecucoes(x);
        } catch (e) { erro(e); } finally { setLoading(false); }
    };
    const loadRelatorios = () => run(async () => {
        setPosicao(await api('/api/ativos/relatorios/posicao'));
        setProjecao(await api('/api/ativos/relatorios/projecao?meses=12'));
    });
    useEffect(() => {
        load();
        opcional('/api/financeiro/plano-contas').then(setContas);
        opcional('/api/financeiro/centros-custo').then(setCentros);
    }, []);
    useEffect(() => { if (tab === 3) loadRelatorios(); }, [tab]);

    const contaOpts = contas.map(c => ({label: `${c.codigo} - ${c.descricao}`, value: c.id}));
    const centroOpts = centros.map(c => ({label: `${c.codigo || ''} ${c.descricao || c.nome || ''}`.trim(), value: c.id}));
    const classeOpts = classes.map(c => ({label: `${c.codigo} - ${c.descricao}`, value: c.id}));
    const nomeClasse = id => classes.find(c => c.id === id)?.descricao;

    const idField = (form, set, campo, label, opts) => (
        <div className="col-12 md:col-6" key={campo}><label>{label}</label>
            {opts.length
                ? <Dropdown value={form[campo]} options={opts} onChange={e => set({...form, [campo]: e.value})} filter showClear placeholder={t('ativos.phSelecione')}/>
                : <InputNumber value={form[campo]} onValueChange={e => set({...form, [campo]: e.value})} placeholder={t('ativos.phID')} useGrouping={false}/>}
        </div>
    );

    const salvarAtivo = async () => {
        const f = ativoDlg;
        const body = {...f, dataAquisicao: isoDate(f.dataAquisicao), dataInicioDepreciacao: isoDate(f.dataInicioDepreciacao), garantiaAte: isoDate(f.garantiaAte)};
        const r = await run(() => f.id ? api(`/api/ativos/${f.id}`, 'PUT', body) : api('/api/ativos', 'POST', body), t('ativos.msgAtivoSalvo'));
        if (r !== undefined) { setAtivoDlg(null); load(); }
    };
    const editarAtivo = a => setAtivoDlg({
        ...a,
        dataAquisicao: a.dataAquisicao ? new Date(a.dataAquisicao + 'T00:00') : null,
        dataInicioDepreciacao: a.dataInicioDepreciacao ? new Date(a.dataInicioDepreciacao + 'T00:00') : null,
        garantiaAte: a.garantiaAte ? new Date(a.garantiaAte + 'T00:00') : null
    });
    const salvarClasse = async () => {
        const c = classeDlg;
        const r = await run(() => c.id ? api(`/api/ativos/classes/${c.id}`, 'PUT', c) : api('/api/ativos/classes', 'POST', c), t('ativos.msgClasseSalva'));
        if (r !== undefined) { setClasseDlg(null); load(); }
    };
    const executarOp = async () => {
        const {tipo, ativo, form} = op;
        const r = await run(() => api(`/api/ativos/${ativo.id}/${OPERACOES[tipo].url}`, 'POST', {...form, data: isoDate(form.data)}), t(OPERACOES[tipo].titulo) + t('ativos.sufRegistrada'));
        if (r !== undefined) { setOp(null); load(); }
    };
    const abrirDetalhe = async a => {
        const r = await run(async () => ({
            ativo: a,
            dep: await api(`/api/ativos/${a.id}/depreciacao`).catch(() => null),
            movimentos: await api(`/api/ativos/${a.id}/movimentos`)
        }));
        if (r) setDetalhe(r);
    };

    const acoesAtivo = a => {
        const baixado = a.status === 'BAIXADO';
        const b = (icon, tip, fn, dis = baixado) => <Button icon={icon} text rounded tooltip={tip} tooltipOptions={{position: 'top'}} disabled={dis} onClick={fn}/>;
        return <div className="flex gap-1 flex-wrap">
            {b('pi pi-eye', t('ativos.tipDetalhes'), () => abrirDetalhe(a), false)}
            {b('pi pi-pencil', t('ativos.tipEditar'), () => editarAtivo(a))}
            {b('pi pi-calculator', t('ativos.tipDepreciar'), () => run(() => api(`/api/ativos/${a.id}/depreciar`, 'POST'), t('ativos.msgDepreciada')).then(load))}
            {b('pi pi-plus-circle', t('ativos.tipAdicao'), () => setOp({tipo: 'adicao', ativo: a, form: {}}))}
            {b('pi pi-arrows-h', t('ativos.tipTransferir'), () => setOp({tipo: 'transferir', ativo: a, form: {}}))}
            {b('pi pi-arrow-up', t('ativos.tipReavaliar'), () => setOp({tipo: 'reavaliar', ativo: a, form: {}}))}
            {b('pi pi-arrow-down', t('ativos.tipImpair'), () => setOp({tipo: 'impairment', ativo: a, form: {}}))}
            {b('pi pi-times', t('ativos.tipBaixar'), () => setOp({tipo: 'baixar', ativo: a, form: {percentual: 100, valorVenda: 0}}))}
        </div>;
    };

    const simular = () => run(async () => setSimulacao(await api(`/api/ativos/depreciacao/simular?periodo=${periodo}`)));
    const executar = async () => {
        const r = await run(() => api(`/api/ativos/depreciacao/executar?periodo=${periodo}`, 'POST'), t('ativos.msgExecucao', {periodo}));
        if (r !== undefined) { setSimulacao([]); load(); }
    };

    const opForm = () => {
        if (!op) return null;
        const {tipo, form} = op;
        const set = f => setOp({...op, form: f});
        const num = (campo, label, props = {mode: 'currency', currency: 'BRL', locale: 'pt-BR'}) =>
            <div className="col-12 md:col-6"><label>{label}</label><InputNumber value={form[campo]} onValueChange={e => set({...form, [campo]: e.value})} {...props}/></div>;
        const texto = (campo, label) => <div className="col-12"><label>{label}</label><InputText value={form[campo] || ''} onChange={e => set({...form, [campo]: e.target.value})}/></div>;
        return <div className="grid p-fluid">
            <div className="col-12 md:col-6"><label>{t('ativos.hData')}</label><Calendar value={form.data} onChange={e => set({...form, data: e.value})} dateFormat={formatoData()} showIcon/></div>
            {tipo === 'adicao' && <>{num('valor', t('ativos.hValor'))}{idField(form, set, 'contaContrapartidaId', t('ativos.fContaContrap'), contaOpts)}{texto('documento', t('ativos.fDocumento'))}{texto('observacao', t('ativos.hObservacao'))}</>}
            {(tipo === 'reavaliar' || tipo === 'impairment') && <>{num('valor', t('ativos.hValor'))}{texto('observacao', t('ativos.hObservacao'))}</>}
            {tipo === 'transferir' && <>{idField(form, set, 'centroCustoDestinoId', t('ativos.fCentroDestino'), centroOpts)}{texto('localizacaoDestino', t('ativos.fLocDestino'))}
                {num('responsavelDestinoId', t('ativos.fRespDestino'), {useGrouping: false})}{texto('observacao', t('ativos.hObservacao'))}</>}
            {tipo === 'baixar' && <>{num('percentual', t('ativos.fPercentual'), {min: 0, max: 100, maxFractionDigits: 2, suffix: ' %'})}{num('valorVenda', t('ativos.fVlrVenda'))}
                {idField(form, set, 'contaContrapartidaId', t('ativos.fContaVenda'), contaOpts)}{texto('motivo', t('ativos.fMotivo'))}</>}
        </div>;
    };

    const af = ativoDlg || {};
    const setAf = setAtivoDlg;
    const cf = classeDlg || {};

    return <div className="p-3"><Toast ref={toast}/>
        <Card title={t('ativos.titulo')} subTitle={t('ativos.subtitulo')}>
            <TabView activeIndex={tab} onTabChange={e => setTab(e.index)}>
                <TabPanel header={t('ativos.tabAtivos')}>
                    <div className="flex justify-content-end mb-3">
                        <Button label={t('ativos.newAtivo')} icon="pi pi-plus" onClick={() => setAtivoDlg({valorAquisicao: 0, valorResidual: 0, metodoDepreciacao: null, critico: false})}/>
                    </div>
                    <DataTable value={ativos} loading={loading} paginator rows={10} responsiveLayout="scroll" filterDisplay="row" emptyMessage={t('ativos.emptyAtivo')}>
                        <Column field="codigo" header={t('ativos.hCodigo')} sortable filter/>
                        <Column field="descricao" header={t('ativos.hDescricao')} sortable filter/>
                        <Column header={t('ativos.hClasse')} body={r => nomeClasse(r.classeId) || r.classe}/>
                        <Column field="localizacao" header={t('ativos.hLocal')}/>
                        <Column header={t('ativos.hCusto')} body={r => brl(Number(r.valorAquisicao || 0) + Number(r.valorReavaliacao || 0))} sortable sortField="valorAquisicao"/>
                        <Column header={t('ativos.hDepreciado')} body={r => brl(r.valorDepreciado)}/>
                        <Column header={t('ativos.hVlrContabil')} body={r => brl(Number(r.valorAquisicao || 0) + Number(r.valorReavaliacao || 0) - Number(r.valorDepreciado || 0) - Number(r.valorImpairment || 0))}/>
                        <Column field="ultimoPeriodoDepreciado" header={t('ativos.hUltPeriodo')}/>
                        <Column field="status" header={t('ativos.hStatus')} body={r => statusTag(r.status)}/>
                        <Column header={t('ativos.hAcoes')} body={acoesAtivo} style={{minWidth: '22rem'}}/>
                    </DataTable>
                </TabPanel>
                <TabPanel header={t('ativos.tabClasses')}>
                    <div className="flex justify-content-end mb-3"><Button label={t('ativos.newClasse')} icon="pi pi-plus" onClick={() => setClasseDlg({metodoDepreciacao: 'LINEAR', ativo: true})}/></div>
                    <DataTable value={classes} loading={loading} responsiveLayout="scroll" emptyMessage={t('ativos.emptyClasse')}>
                        <Column field="codigo" header={t('ativos.hCodigo')}/><Column field="descricao" header={t('ativos.hDescricao')}/>
                        <Column field="metodoDepreciacao" header={t('ativos.hMetodo')}/><Column field="vidaUtilMeses" header={t('ativos.hVidaUtil')}/>
                        <Column header={t('ativos.hContabiliza')} body={r => r.contaAtivoId && r.contaDepreciacaoAcumuladaId && r.contaDespesaDepreciacaoId ? <Tag value={t('ativos.sim')} severity="success"/> : <Tag value={t('ativos.nao')} severity="warning"/>}/>
                        <Column header={t('ativos.hAcoes')} body={r => <div className="flex gap-1">
                            <Button icon="pi pi-pencil" text rounded onClick={() => setClasseDlg({...r})}/>
                            <Button icon="pi pi-trash" text rounded severity="danger" onClick={() => run(() => api(`/api/ativos/classes/${r.id}`, 'DELETE'), t('ativos.msgClasseExcluida')).then(load)}/>
                        </div>}/>
                    </DataTable>
                </TabPanel>
                <TabPanel header={t('ativos.tabDep')}>
                    <div className="flex gap-2 align-items-end mb-3 flex-wrap">
                        <div><label className="block">{t('ativos.lblPeriodo')}</label><InputText value={periodo} onChange={e => setPeriodo(e.target.value)} style={{width: '9rem'}}/></div>
                        <Button label={t('ativos.btnSimular')} icon="pi pi-search" outlined onClick={simular}/>
                        <Button label={t('ativos.btnExecutar')} icon="pi pi-check" onClick={executar}/>
                    </div>
                    {simulacao.length > 0 && <DataTable value={simulacao} responsiveLayout="scroll" className="mb-4"
                        footer={`${t('ativos.footTotal')} ${brl(simulacao.reduce((s, l) => s + Number(l.cota), 0))}`}>
                        <Column field="codigo" header={t('ativos.hAtivo')}/><Column field="descricao" header={t('ativos.hDescricao')}/><Column field="metodo" header={t('ativos.hMetodo')}/>
                        <Column header={t('ativos.hBase')} body={r => brl(r.baseDepreciavel)}/><Column header={t('ativos.hAcumAnt')} body={r => brl(r.acumuladaAnterior)}/>
                        <Column header={t('ativos.hCota')} body={r => brl(r.cota)}/><Column header={t('ativos.hVlrApos')} body={r => brl(r.valorContabilApos)}/>
                    </DataTable>}
                    <h4>{t('ativos.hExecucoes')}</h4>
                    <DataTable value={execucoes} responsiveLayout="scroll" emptyMessage={t('ativos.emptyExec')}>
                        <Column field="periodo" header={t('ativos.hPeriodo')}/><Column field="quantidadeAtivos" header={t('ativos.tabAtivos')}/>
                        <Column header={t('ativos.hTotal')} body={r => brl(r.valorTotal)}/><Column header={t('ativos.hContabilizado')} body={r => brl(r.valorContabilizado)}/>
                        <Column field="lancamentoId" header={t('ativos.hLancamento')}/><Column header={t('ativos.hStatus')} body={r => statusTag(r.status)}/>
                        <Column header="" body={r => <Button label={t('ativos.btnEstornar')} icon="pi pi-undo" text severity="danger" disabled={r.status !== 'EFETIVADA'}
                            onClick={() => run(() => api(`/api/ativos/depreciacao/execucoes/${r.id}/estornar`, 'POST'), t('ativos.msgEstornada')).then(load)}/>}/>
                    </DataTable>
                </TabPanel>
                <TabPanel header={t('ativos.tabRel')}>
                    {posicao && <>
                        <div className="grid mb-3">
                            {[[t('ativos.tabAtivos'), posicao.quantidade], [t('ativos.hCusto'), brl(posicao.custo)], [t('ativos.kDepImp'), brl(posicao.depreciacaoAcumulada)], [t('ativos.hVlrContabil'), brl(posicao.valorContabil)]].map(([k, v]) =>
                                <div className="col-12 md:col-3" key={k}><Card><div className="text-500">{k}</div><div className="text-2xl font-bold">{v}</div></Card></div>)}
                        </div>
                        <h4>{t('ativos.hPosicao')}</h4>
                        <DataTable value={posicao.porClasse} responsiveLayout="scroll" className="mb-4">
                            <Column field="classe" header={t('ativos.hClasse')}/><Column field="quantidade" header={t('ativos.hQtd')}/>
                            <Column header={t('ativos.hCusto')} body={r => brl(r.custo)}/><Column header={t('ativos.hDepAcum')} body={r => brl(r.depreciacaoAcumulada)}/><Column header={t('ativos.hVlrContabil')} body={r => brl(r.valorContabil)}/>
                        </DataTable>
                    </>}
                    <h4>{t('ativos.hProj12')}</h4>
                    <DataTable value={projecao} responsiveLayout="scroll"><Column field="periodo" header={t('ativos.hPeriodo')}/><Column header={t('ativos.hDepreciacao')} body={r => brl(r.depreciacao)}/></DataTable>
                </TabPanel>
            </TabView>
        </Card>

        <Dialog header={af.id ? t('ativos.dlgEditarAtivo') : t('ativos.newAtivo')} visible={!!ativoDlg} onHide={() => setAtivoDlg(null)} modal style={{width: 'min(860px,95vw)'}}>
            <div className="grid p-fluid">
                <div className="col-12 md:col-4"><label>{t('ativos.hCodigo')}</label><InputText value={af.codigo || ''} onChange={e => setAf({...af, codigo: e.target.value})}/></div>
                <div className="col-12 md:col-8"><label>{t('ativos.hDescricao')}</label><InputText value={af.descricao || ''} onChange={e => setAf({...af, descricao: e.target.value})}/></div>
                <div className="col-12 md:col-6"><label>{t('ativos.hClasse')}</label><Dropdown value={af.classeId} options={classeOpts} showClear placeholder={t('ativos.phSelecione')}
                    onChange={e => { const c = classes.find(x => x.id === e.value); setAf({...af, classeId: e.value, classe: c?.descricao, vidaUtilMeses: af.vidaUtilMeses || c?.vidaUtilMeses}); }}/></div>
                {!af.id && idField(af, setAf, 'centroCustoId', t('ativos.fCentroCusto'), centroOpts)}
                {!af.id && <div className="col-12 md:col-6"><label>{t('ativos.hLocal')}</label><InputText value={af.localizacao || ''} onChange={e => setAf({...af, localizacao: e.target.value})}/></div>}
                <div className="col-12 md:col-6"><label>{t('ativos.fNumSerie')}</label><InputText value={af.numeroSerie || ''} onChange={e => setAf({...af, numeroSerie: e.target.value})}/></div>
                <div className="col-12 md:col-6"><label>{t('ativos.fFabricante')}</label><InputText value={af.fabricante || ''} onChange={e => setAf({...af, fabricante: e.target.value})}/></div>
                <div className="col-12 md:col-6"><label>{t('ativos.fModelo')}</label><InputText value={af.modelo || ''} onChange={e => setAf({...af, modelo: e.target.value})}/></div>
                <div className="col-12 md:col-6"><label>{t('ativos.fNotaDoc')}</label><InputText value={af.numeroDocumento || ''} onChange={e => setAf({...af, numeroDocumento: e.target.value})}/></div>
                <div className="col-12 md:col-6"><label>{t('ativos.fAtivoSup')}</label><InputNumber value={af.ativoPaiId} onValueChange={e => setAf({...af, ativoPaiId: e.value})} useGrouping={false}/></div>
                <div className="col-12 md:col-4"><label>{t('ativos.fDataAq')}</label><Calendar value={af.dataAquisicao} onChange={e => setAf({...af, dataAquisicao: e.value})} dateFormat={formatoData()} showIcon/></div>
                <div className="col-12 md:col-4"><label>{t('ativos.fIniDep')}</label><Calendar value={af.dataInicioDepreciacao} onChange={e => setAf({...af, dataInicioDepreciacao: e.value})} dateFormat={formatoData()} showIcon/></div>
                <div className="col-12 md:col-4"><label>{t('ativos.fGarantia')}</label><Calendar value={af.garantiaAte} onChange={e => setAf({...af, garantiaAte: e.value})} dateFormat={formatoData()} showIcon/></div>
                {!af.id && <div className="col-12 md:col-4"><label>{t('ativos.fVlrAq')}</label><InputNumber value={af.valorAquisicao} onValueChange={e => setAf({...af, valorAquisicao: e.value})} mode="currency" currency="BRL" locale={localeAtivo()}/></div>}
                <div className="col-12 md:col-4"><label>{t('ativos.fVlrResidual')}</label><InputNumber value={af.valorResidual} onValueChange={e => setAf({...af, valorResidual: e.value})} mode="currency" currency="BRL" locale={localeAtivo()}/></div>
                <div className="col-12 md:col-4"><label>{t('ativos.hVidaUtil')}</label><InputNumber value={af.vidaUtilMeses} onValueChange={e => setAf({...af, vidaUtilMeses: e.value})}/></div>
                <div className="col-12 md:col-4"><label>{t('ativos.fMetodoVazio')}</label><Dropdown value={af.metodoDepreciacao} options={metodosOpts(t)} showClear onChange={e => setAf({...af, metodoDepreciacao: e.value})}/></div>
                <div className="col-12 md:col-4"><label>{t('ativos.fUnContador')}</label><InputText value={af.unidadeContador || ''} placeholder={t('ativos.phUnContador')} onChange={e => setAf({...af, unidadeContador: e.target.value})}/></div>
                <div className="col-12 md:col-4 flex align-items-center gap-2 mt-4"><Checkbox inputId="critico" checked={!!af.critico} onChange={e => setAf({...af, critico: e.checked})}/><label htmlFor="critico">{t('ativos.fCritico')}</label></div>
            </div>
            <div className="flex justify-content-end mt-3"><Button label={t('ativos.btnSalvar')} icon="pi pi-save" onClick={salvarAtivo}/></div>
        </Dialog>

        <Dialog header={cf.id ? t('ativos.dlgEditarClasse') : t('ativos.newClasse')} visible={!!classeDlg} onHide={() => setClasseDlg(null)} modal style={{width: 'min(760px,95vw)'}}>
            <div className="grid p-fluid">
                <div className="col-12 md:col-4"><label>{t('ativos.hCodigo')}</label><InputText value={cf.codigo || ''} onChange={e => setClasseDlg({...cf, codigo: e.target.value})}/></div>
                <div className="col-12 md:col-8"><label>{t('ativos.hDescricao')}</label><InputText value={cf.descricao || ''} onChange={e => setClasseDlg({...cf, descricao: e.target.value})}/></div>
                <div className="col-12 md:col-4"><label>{t('ativos.hMetodo')}</label><Dropdown value={cf.metodoDepreciacao} options={metodosOpts(t)} onChange={e => setClasseDlg({...cf, metodoDepreciacao: e.value})}/></div>
                <div className="col-12 md:col-4"><label>{t('ativos.hVidaUtil')}</label><InputNumber value={cf.vidaUtilMeses} onValueChange={e => setClasseDlg({...cf, vidaUtilMeses: e.value})}/></div>
                <div className="col-12 md:col-4"><label>{t('ativos.fTaxa')}</label><InputNumber value={cf.taxaAnual} onValueChange={e => setClasseDlg({...cf, taxaAnual: e.value})} maxFractionDigits={4}/></div>
                <div className="col-12"><small className="text-500">{t('ativos.hintContas')}</small></div>
                {CONTAS_CLASSE.map(([campo, label]) => idField(cf, setClasseDlg, campo, t(label), contaOpts))}
            </div>
            <div className="flex justify-content-end mt-3"><Button label={t('ativos.btnSalvar')} icon="pi pi-save" onClick={salvarClasse}/></div>
        </Dialog>

        <Dialog header={op ? `${t(OPERACOES[op.tipo].titulo)} — ${op.ativo.codigo}` : ''} visible={!!op} onHide={() => setOp(null)} modal style={{width: 'min(640px,95vw)'}}>
            {opForm()}
            <div className="flex justify-content-end mt-3"><Button label={t('ativos.btnConfirmar')} icon="pi pi-check" onClick={executarOp}/></div>
        </Dialog>

        <Dialog header={detalhe ? `${detalhe.ativo.codigo} — ${detalhe.ativo.descricao}` : ''} visible={!!detalhe} onHide={() => setDetalhe(null)} modal style={{width: 'min(980px,96vw)'}}>
            {detalhe?.dep && <div className="grid mb-3">
                <div className="col-6 md:col-3">{t('ativos.fMetodo2')} <strong>{detalhe.dep.metodo}</strong></div>
                <div className="col-6 md:col-3">{t('ativos.fMeses')} <strong>{detalhe.dep.mesesDecorridos}/{detalhe.dep.mesesDepreciaveis}</strong></div>
                <div className="col-6 md:col-3">{t('ativos.fCotaMes')} <strong>{brl(detalhe.dep.depreciacaoMensal)}</strong></div>
                <div className="col-6 md:col-3">{t('ativos.hVlrContabil')} <strong>{brl(detalhe.dep.valorContabilCalculado)}</strong></div>
            </div>}
            <DataTable value={detalhe?.movimentos || []} responsiveLayout="scroll" emptyMessage={t('ativos.emptyMov')}>
                <Column field="dataMovimento" header={t('ativos.hData')}/><Column field="tipo" header={t('ativos.hTipo')}/>
                <Column header={t('ativos.hValor')} body={r => brl(r.valor)}/><Column header={t('ativos.hResultado')} body={r => r.resultado == null ? '' : brl(r.resultado)}/>
                <Column header={t('ativos.hOrigemDestino')} body={r => r.tipo === 'TRANSFERENCIA' ? `${r.localizacaoOrigem || r.centroCustoOrigemId || '-'} → ${r.localizacaoDestino || r.centroCustoDestinoId || '-'}` : ''}/>
                <Column field="lancamentoId" header={t('ativos.hLancamento')}/><Column field="status" header={t('ativos.hStatus')}/><Column field="observacao" header={t('ativos.hObservacao')}/>
            </DataTable>
        </Dialog>
    </div>;
}
