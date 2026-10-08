import React, {useEffect, useRef, useState} from 'react';
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

const METODOS = [
    {label: 'Linear', value: 'LINEAR'},
    {label: 'Soma dos dígitos', value: 'SOMA_DIGITOS'},
    {label: 'Saldo decrescente', value: 'SALDO_DECRESCENTE'}
];
const CONTAS_CLASSE = [
    ['contaAtivoId', 'Conta do ativo'],
    ['contaDepreciacaoAcumuladaId', 'Depreciação acumulada'],
    ['contaDespesaDepreciacaoId', 'Despesa de depreciação'],
    ['contaGanhoBaixaId', 'Ganho na baixa'],
    ['contaPerdaBaixaId', 'Perda na baixa'],
    ['contaReavaliacaoId', 'Reserva de reavaliação'],
    ['contaImpairmentId', 'Perda por impairment']
];
const OPERACOES = {
    adicao: {titulo: 'Adição ao custo', url: 'adicao'},
    transferir: {titulo: 'Transferência', url: 'transferir'},
    reavaliar: {titulo: 'Reavaliação', url: 'reavaliar'},
    impairment: {titulo: 'Perda por impairment', url: 'impairment'},
    baixar: {titulo: 'Baixa (total ou parcial)', url: 'baixar'}
};
const statusTag = s => <Tag value={s} severity={s === 'BAIXADO' || s === 'ESTORNADA' ? 'danger' : 'success'}/>;

export default function Ativos() {
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

    const erro = e => toast.current?.show({severity: 'error', summary: 'Erro', detail: e.message, life: 5000});
    const ok = m => toast.current?.show({severity: 'success', summary: 'OK', detail: m, life: 3000});
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
                ? <Dropdown value={form[campo]} options={opts} onChange={e => set({...form, [campo]: e.value})} filter showClear placeholder="Selecione"/>
                : <InputNumber value={form[campo]} onValueChange={e => set({...form, [campo]: e.value})} placeholder="ID" useGrouping={false}/>}
        </div>
    );

    const salvarAtivo = async () => {
        const f = ativoDlg;
        const body = {...f, dataAquisicao: isoDate(f.dataAquisicao), dataInicioDepreciacao: isoDate(f.dataInicioDepreciacao), garantiaAte: isoDate(f.garantiaAte)};
        const r = await run(() => f.id ? api(`/api/ativos/${f.id}`, 'PUT', body) : api('/api/ativos', 'POST', body), 'Ativo salvo');
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
        const r = await run(() => c.id ? api(`/api/ativos/classes/${c.id}`, 'PUT', c) : api('/api/ativos/classes', 'POST', c), 'Classe salva');
        if (r !== undefined) { setClasseDlg(null); load(); }
    };
    const executarOp = async () => {
        const {tipo, ativo, form} = op;
        const r = await run(() => api(`/api/ativos/${ativo.id}/${OPERACOES[tipo].url}`, 'POST', {...form, data: isoDate(form.data)}), OPERACOES[tipo].titulo + ' registrada');
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
            {b('pi pi-eye', 'Detalhes e razão do ativo', () => abrirDetalhe(a), false)}
            {b('pi pi-pencil', 'Editar', () => editarAtivo(a))}
            {b('pi pi-calculator', 'Depreciar até o mês atual', () => run(() => api(`/api/ativos/${a.id}/depreciar`, 'POST'), 'Depreciação registrada').then(load))}
            {b('pi pi-plus-circle', 'Adição ao custo', () => setOp({tipo: 'adicao', ativo: a, form: {}}))}
            {b('pi pi-arrows-h', 'Transferir', () => setOp({tipo: 'transferir', ativo: a, form: {}}))}
            {b('pi pi-arrow-up', 'Reavaliar', () => setOp({tipo: 'reavaliar', ativo: a, form: {}}))}
            {b('pi pi-arrow-down', 'Impairment', () => setOp({tipo: 'impairment', ativo: a, form: {}}))}
            {b('pi pi-times', 'Baixar', () => setOp({tipo: 'baixar', ativo: a, form: {percentual: 100, valorVenda: 0}}))}
        </div>;
    };

    const simular = () => run(async () => setSimulacao(await api(`/api/ativos/depreciacao/simular?periodo=${periodo}`)));
    const executar = async () => {
        const r = await run(() => api(`/api/ativos/depreciacao/executar?periodo=${periodo}`, 'POST'), `Depreciação de ${periodo} executada`);
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
            <div className="col-12 md:col-6"><label>Data</label><Calendar value={form.data} onChange={e => set({...form, data: e.value})} dateFormat="dd/mm/yy" showIcon/></div>
            {tipo === 'adicao' && <>{num('valor', 'Valor')}{idField(form, set, 'contaContrapartidaId', 'Conta de contrapartida', contaOpts)}{texto('documento', 'Documento')}{texto('observacao', 'Observação')}</>}
            {(tipo === 'reavaliar' || tipo === 'impairment') && <>{num('valor', 'Valor')}{texto('observacao', 'Observação')}</>}
            {tipo === 'transferir' && <>{idField(form, set, 'centroCustoDestinoId', 'Centro de custo destino', centroOpts)}{texto('localizacaoDestino', 'Localização destino')}
                {num('responsavelDestinoId', 'Responsável destino (ID)', {useGrouping: false})}{texto('observacao', 'Observação')}</>}
            {tipo === 'baixar' && <>{num('percentual', 'Percentual baixado (%)', {min: 0, max: 100, maxFractionDigits: 2, suffix: ' %'})}{num('valorVenda', 'Valor de venda')}
                {idField(form, set, 'contaContrapartidaId', 'Conta de recebimento da venda', contaOpts)}{texto('motivo', 'Motivo')}</>}
        </div>;
    };

    const af = ativoDlg || {};
    const setAf = setAtivoDlg;
    const cf = classeDlg || {};

    return <div className="p-3"><Toast ref={toast}/>
        <Card title="Ativo imobilizado" subTitle="Classes, ciclo de vida, depreciação mensal e contabilização (FI-AA)">
            <TabView activeIndex={tab} onTabChange={e => setTab(e.index)}>
                <TabPanel header="Ativos">
                    <div className="flex justify-content-end mb-3">
                        <Button label="Novo ativo" icon="pi pi-plus" onClick={() => setAtivoDlg({valorAquisicao: 0, valorResidual: 0, metodoDepreciacao: null, critico: false})}/>
                    </div>
                    <DataTable value={ativos} loading={loading} paginator rows={10} responsiveLayout="scroll" filterDisplay="row" emptyMessage="Nenhum ativo">
                        <Column field="codigo" header="Código" sortable filter/>
                        <Column field="descricao" header="Descrição" sortable filter/>
                        <Column header="Classe" body={r => nomeClasse(r.classeId) || r.classe}/>
                        <Column field="localizacao" header="Localização"/>
                        <Column header="Custo" body={r => brl(Number(r.valorAquisicao || 0) + Number(r.valorReavaliacao || 0))} sortable sortField="valorAquisicao"/>
                        <Column header="Depreciado" body={r => brl(r.valorDepreciado)}/>
                        <Column header="Valor contábil" body={r => brl(Number(r.valorAquisicao || 0) + Number(r.valorReavaliacao || 0) - Number(r.valorDepreciado || 0) - Number(r.valorImpairment || 0))}/>
                        <Column field="ultimoPeriodoDepreciado" header="Últ. período"/>
                        <Column field="status" header="Status" body={r => statusTag(r.status)}/>
                        <Column header="Ações" body={acoesAtivo} style={{minWidth: '22rem'}}/>
                    </DataTable>
                </TabPanel>
                <TabPanel header="Classes">
                    <div className="flex justify-content-end mb-3"><Button label="Nova classe" icon="pi pi-plus" onClick={() => setClasseDlg({metodoDepreciacao: 'LINEAR', ativo: true})}/></div>
                    <DataTable value={classes} loading={loading} responsiveLayout="scroll" emptyMessage="Nenhuma classe">
                        <Column field="codigo" header="Código"/><Column field="descricao" header="Descrição"/>
                        <Column field="metodoDepreciacao" header="Método"/><Column field="vidaUtilMeses" header="Vida útil (meses)"/>
                        <Column header="Contabiliza" body={r => r.contaAtivoId && r.contaDepreciacaoAcumuladaId && r.contaDespesaDepreciacaoId ? <Tag value="Sim" severity="success"/> : <Tag value="Não" severity="warning"/>}/>
                        <Column header="Ações" body={r => <div className="flex gap-1">
                            <Button icon="pi pi-pencil" text rounded onClick={() => setClasseDlg({...r})}/>
                            <Button icon="pi pi-trash" text rounded severity="danger" onClick={() => run(() => api(`/api/ativos/classes/${r.id}`, 'DELETE'), 'Classe excluída').then(load)}/>
                        </div>}/>
                    </DataTable>
                </TabPanel>
                <TabPanel header="Depreciação mensal">
                    <div className="flex gap-2 align-items-end mb-3 flex-wrap">
                        <div><label className="block">Período (AAAA-MM)</label><InputText value={periodo} onChange={e => setPeriodo(e.target.value)} style={{width: '9rem'}}/></div>
                        <Button label="Simular" icon="pi pi-search" outlined onClick={simular}/>
                        <Button label="Executar e contabilizar" icon="pi pi-check" onClick={executar}/>
                    </div>
                    {simulacao.length > 0 && <DataTable value={simulacao} responsiveLayout="scroll" className="mb-4"
                        footer={`Total do período: ${brl(simulacao.reduce((s, l) => s + Number(l.cota), 0))}`}>
                        <Column field="codigo" header="Ativo"/><Column field="descricao" header="Descrição"/><Column field="metodo" header="Método"/>
                        <Column header="Base" body={r => brl(r.baseDepreciavel)}/><Column header="Acumulada anterior" body={r => brl(r.acumuladaAnterior)}/>
                        <Column header="Cota" body={r => brl(r.cota)}/><Column header="Valor contábil após" body={r => brl(r.valorContabilApos)}/>
                    </DataTable>}
                    <h4>Execuções</h4>
                    <DataTable value={execucoes} responsiveLayout="scroll" emptyMessage="Nenhuma execução">
                        <Column field="periodo" header="Período"/><Column field="quantidadeAtivos" header="Ativos"/>
                        <Column header="Total" body={r => brl(r.valorTotal)}/><Column header="Contabilizado" body={r => brl(r.valorContabilizado)}/>
                        <Column field="lancamentoId" header="Lançamento"/><Column header="Status" body={r => statusTag(r.status)}/>
                        <Column header="" body={r => <Button label="Estornar" icon="pi pi-undo" text severity="danger" disabled={r.status !== 'EFETIVADA'}
                            onClick={() => run(() => api(`/api/ativos/depreciacao/execucoes/${r.id}/estornar`, 'POST'), 'Execução estornada').then(load)}/>}/>
                    </DataTable>
                </TabPanel>
                <TabPanel header="Relatórios">
                    {posicao && <>
                        <div className="grid mb-3">
                            {[['Ativos', posicao.quantidade], ['Custo', brl(posicao.custo)], ['Depreciação + impairment', brl(posicao.depreciacaoAcumulada)], ['Valor contábil', brl(posicao.valorContabil)]].map(([k, v]) =>
                                <div className="col-12 md:col-3" key={k}><Card><div className="text-500">{k}</div><div className="text-2xl font-bold">{v}</div></Card></div>)}
                        </div>
                        <h4>Posição por classe</h4>
                        <DataTable value={posicao.porClasse} responsiveLayout="scroll" className="mb-4">
                            <Column field="classe" header="Classe"/><Column field="quantidade" header="Qtd."/>
                            <Column header="Custo" body={r => brl(r.custo)}/><Column header="Depreciação acumulada" body={r => brl(r.depreciacaoAcumulada)}/><Column header="Valor contábil" body={r => brl(r.valorContabil)}/>
                        </DataTable>
                    </>}
                    <h4>Projeção de depreciação (12 meses)</h4>
                    <DataTable value={projecao} responsiveLayout="scroll"><Column field="periodo" header="Período"/><Column header="Depreciação" body={r => brl(r.depreciacao)}/></DataTable>
                </TabPanel>
            </TabView>
        </Card>

        <Dialog header={af.id ? 'Editar ativo' : 'Novo ativo'} visible={!!ativoDlg} onHide={() => setAtivoDlg(null)} modal style={{width: 'min(860px,95vw)'}}>
            <div className="grid p-fluid">
                <div className="col-12 md:col-4"><label>Código</label><InputText value={af.codigo || ''} onChange={e => setAf({...af, codigo: e.target.value})}/></div>
                <div className="col-12 md:col-8"><label>Descrição</label><InputText value={af.descricao || ''} onChange={e => setAf({...af, descricao: e.target.value})}/></div>
                <div className="col-12 md:col-6"><label>Classe</label><Dropdown value={af.classeId} options={classeOpts} showClear placeholder="Selecione"
                    onChange={e => { const c = classes.find(x => x.id === e.value); setAf({...af, classeId: e.value, classe: c?.descricao, vidaUtilMeses: af.vidaUtilMeses || c?.vidaUtilMeses}); }}/></div>
                {!af.id && idField(af, setAf, 'centroCustoId', 'Centro de custo', centroOpts)}
                {!af.id && <div className="col-12 md:col-6"><label>Localização</label><InputText value={af.localizacao || ''} onChange={e => setAf({...af, localizacao: e.target.value})}/></div>}
                <div className="col-12 md:col-6"><label>Número de série</label><InputText value={af.numeroSerie || ''} onChange={e => setAf({...af, numeroSerie: e.target.value})}/></div>
                <div className="col-12 md:col-6"><label>Fabricante</label><InputText value={af.fabricante || ''} onChange={e => setAf({...af, fabricante: e.target.value})}/></div>
                <div className="col-12 md:col-6"><label>Modelo</label><InputText value={af.modelo || ''} onChange={e => setAf({...af, modelo: e.target.value})}/></div>
                <div className="col-12 md:col-6"><label>Nota/documento de aquisição</label><InputText value={af.numeroDocumento || ''} onChange={e => setAf({...af, numeroDocumento: e.target.value})}/></div>
                <div className="col-12 md:col-6"><label>Ativo superior (ID)</label><InputNumber value={af.ativoPaiId} onValueChange={e => setAf({...af, ativoPaiId: e.value})} useGrouping={false}/></div>
                <div className="col-12 md:col-4"><label>Data de aquisição</label><Calendar value={af.dataAquisicao} onChange={e => setAf({...af, dataAquisicao: e.value})} dateFormat="dd/mm/yy" showIcon/></div>
                <div className="col-12 md:col-4"><label>Início da depreciação</label><Calendar value={af.dataInicioDepreciacao} onChange={e => setAf({...af, dataInicioDepreciacao: e.value})} dateFormat="dd/mm/yy" showIcon/></div>
                <div className="col-12 md:col-4"><label>Garantia até</label><Calendar value={af.garantiaAte} onChange={e => setAf({...af, garantiaAte: e.value})} dateFormat="dd/mm/yy" showIcon/></div>
                {!af.id && <div className="col-12 md:col-4"><label>Valor de aquisição</label><InputNumber value={af.valorAquisicao} onValueChange={e => setAf({...af, valorAquisicao: e.value})} mode="currency" currency="BRL" locale="pt-BR"/></div>}
                <div className="col-12 md:col-4"><label>Valor residual</label><InputNumber value={af.valorResidual} onValueChange={e => setAf({...af, valorResidual: e.value})} mode="currency" currency="BRL" locale="pt-BR"/></div>
                <div className="col-12 md:col-4"><label>Vida útil (meses)</label><InputNumber value={af.vidaUtilMeses} onValueChange={e => setAf({...af, vidaUtilMeses: e.value})}/></div>
                <div className="col-12 md:col-4"><label>Método (vazio = da classe)</label><Dropdown value={af.metodoDepreciacao} options={METODOS} showClear onChange={e => setAf({...af, metodoDepreciacao: e.value})}/></div>
                <div className="col-12 md:col-4"><label>Unidade do contador</label><InputText value={af.unidadeContador || ''} placeholder="h, km, ciclos" onChange={e => setAf({...af, unidadeContador: e.target.value})}/></div>
                <div className="col-12 md:col-4 flex align-items-center gap-2 mt-4"><Checkbox inputId="critico" checked={!!af.critico} onChange={e => setAf({...af, critico: e.checked})}/><label htmlFor="critico">Equipamento crítico</label></div>
            </div>
            <div className="flex justify-content-end mt-3"><Button label="Salvar" icon="pi pi-save" onClick={salvarAtivo}/></div>
        </Dialog>

        <Dialog header={cf.id ? 'Editar classe' : 'Nova classe'} visible={!!classeDlg} onHide={() => setClasseDlg(null)} modal style={{width: 'min(760px,95vw)'}}>
            <div className="grid p-fluid">
                <div className="col-12 md:col-4"><label>Código</label><InputText value={cf.codigo || ''} onChange={e => setClasseDlg({...cf, codigo: e.target.value})}/></div>
                <div className="col-12 md:col-8"><label>Descrição</label><InputText value={cf.descricao || ''} onChange={e => setClasseDlg({...cf, descricao: e.target.value})}/></div>
                <div className="col-12 md:col-4"><label>Método</label><Dropdown value={cf.metodoDepreciacao} options={METODOS} onChange={e => setClasseDlg({...cf, metodoDepreciacao: e.value})}/></div>
                <div className="col-12 md:col-4"><label>Vida útil (meses)</label><InputNumber value={cf.vidaUtilMeses} onValueChange={e => setClasseDlg({...cf, vidaUtilMeses: e.value})}/></div>
                <div className="col-12 md:col-4"><label>Taxa anual (fração, opcional)</label><InputNumber value={cf.taxaAnual} onValueChange={e => setClasseDlg({...cf, taxaAnual: e.value})} maxFractionDigits={4}/></div>
                <div className="col-12"><small className="text-500">Com as contas de ativo, depreciação acumulada e despesa preenchidas, a depreciação mensal e os movimentos geram lançamento contábil automaticamente.</small></div>
                {CONTAS_CLASSE.map(([campo, label]) => idField(cf, setClasseDlg, campo, label, contaOpts))}
            </div>
            <div className="flex justify-content-end mt-3"><Button label="Salvar" icon="pi pi-save" onClick={salvarClasse}/></div>
        </Dialog>

        <Dialog header={op ? `${OPERACOES[op.tipo].titulo} — ${op.ativo.codigo}` : ''} visible={!!op} onHide={() => setOp(null)} modal style={{width: 'min(640px,95vw)'}}>
            {opForm()}
            <div className="flex justify-content-end mt-3"><Button label="Confirmar" icon="pi pi-check" onClick={executarOp}/></div>
        </Dialog>

        <Dialog header={detalhe ? `${detalhe.ativo.codigo} — ${detalhe.ativo.descricao}` : ''} visible={!!detalhe} onHide={() => setDetalhe(null)} modal style={{width: 'min(980px,96vw)'}}>
            {detalhe?.dep && <div className="grid mb-3">
                <div className="col-6 md:col-3">Método: <strong>{detalhe.dep.metodo}</strong></div>
                <div className="col-6 md:col-3">Meses: <strong>{detalhe.dep.mesesDecorridos}/{detalhe.dep.mesesDepreciaveis}</strong></div>
                <div className="col-6 md:col-3">Cota do mês: <strong>{brl(detalhe.dep.depreciacaoMensal)}</strong></div>
                <div className="col-6 md:col-3">Valor contábil: <strong>{brl(detalhe.dep.valorContabilCalculado)}</strong></div>
            </div>}
            <DataTable value={detalhe?.movimentos || []} responsiveLayout="scroll" emptyMessage="Sem movimentos">
                <Column field="dataMovimento" header="Data"/><Column field="tipo" header="Tipo"/>
                <Column header="Valor" body={r => brl(r.valor)}/><Column header="Resultado" body={r => r.resultado == null ? '' : brl(r.resultado)}/>
                <Column header="Origem → destino" body={r => r.tipo === 'TRANSFERENCIA' ? `${r.localizacaoOrigem || r.centroCustoOrigemId || '-'} → ${r.localizacaoDestino || r.centroCustoDestinoId || '-'}` : ''}/>
                <Column field="lancamentoId" header="Lançamento"/><Column field="status" header="Status"/><Column field="observacao" header="Observação"/>
            </DataTable>
        </Dialog>
    </div>;
}
