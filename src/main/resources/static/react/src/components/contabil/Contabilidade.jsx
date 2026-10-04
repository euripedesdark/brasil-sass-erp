import React, { useState, useEffect, useRef, useCallback } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { TabView, TabPanel } from 'primereact/tabview';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';

const BASE = '/api/contabilidade';

const fmt = (v) => Number(v ?? 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 });

export const Contabilidade = () => {
    const toast = useRef(null);
    const [lancs, setLancs] = useState([]);
    const [contas, setContas] = useState([]);
    const [fechs, setFechs] = useState([]);
    const [razao, setRazao] = useState([]);
    const [balancete, setBalancete] = useState([]);
    const [balanco, setBalanco] = useState(null);
    const [loading, setLoading] = useState(true);
    const [dlg, setDlg] = useState(false);
    const [f, setF] = useState({ data: null, historico: '' });
    const [filtroConta, setFiltroConta] = useState(null);
    const [de, setDe] = useState(null);
    const [ate, setAte] = useState(null);
    const [exercicio, setExercicio] = useState(new Date().getFullYear());
    const [periodoFech, setPeriodoFech] = useState('');
    const [dlgTitulo, setDlgTitulo] = useState(false);
    const [tit, setTit] = useState({ tituloId: null, contaDebitoId: null, contaCreditoId: null });

    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? j ?? []); };
    const carregar = useCallback(async () => {
        setLoading(true);
        try {
            const [l, c, fe] = await Promise.all([
                apiFetch(BASE + '/lancamentos').then(js),
                apiFetch('/api/financeiro/plano-contas').then(js).catch(() => []),
                apiFetch(BASE + '/fechamentos').then(js),
            ]);
            setLancs(l); setContas(c); setFechs(fe);
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha ao carregar', life: 4000 }); }
        finally { setLoading(false); }
    }, []);
    useEffect(() => { carregar(); carregarEcdHist(); }, [carregar]);

    const contaLabel = (id) => { const c = contas.find(x => x.id === id); return c ? ((c.codigo || '') + ' - ' + (c.descricao || c.nome || '')) : id; };
    const iso = (d) => d ? d.toISOString().slice(0, 10) : '';

    const salvar = async () => {
        if (!f.data || !f.historico?.trim()) { toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Data e histórico obrigatórios', life: 3000 }); return; }
        await apiFetch(BASE + '/lancamentos', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ data: iso(f.data), historico: f.historico }) });
        setDlg(false); setF({ data: null, historico: '' }); carregar();
    };
    const lancar = async (id) => { const r = await apiFetch(BASE + '/lancamentos/' + id + '/lancar', { method: 'POST' }); if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Desbalanceado ou período fechado', life: 4000 }); return; } carregar(); };
    const estornar = async (id) => { await apiFetch(BASE + '/lancamentos/' + id + '/estornar', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: '{}' }); carregar(); };
    const buscarRazao = async () => { if (!filtroConta || !de || !ate) return; setRazao(await apiFetch(BASE + '/razao?contaId=' + filtroConta + '&de=' + iso(de) + '&ate=' + iso(ate)).then(js)); };
    const buscarBalancete = async () => { if (!de || !ate) return; setBalancete(await apiFetch(BASE + '/balancete?de=' + iso(de) + '&ate=' + iso(ate)).then(js)); };
    const [dre, setDre] = useState([]);
    const [ecd, setEcd] = useState(null);
    const [ecdHist, setEcdHist] = useState([]);
    const carregarEcdHist = async () => { try { const r = await apiFetch(BASE + '/ecd/historico'); setEcdHist(await r.json().catch(() => [])); } catch (e) { } };
    const [ecdCnpj, setEcdCnpj] = useState('');
    const [ecdNome, setEcdNome] = useState('');
    const buscarDre = async () => { const r = await apiFetch(BASE + '/dre?exercicio=' + exercicio); setDre(await r.json().catch(() => [])); };
    const gerarEcd = async () => {
        try {
            const r = await apiFetch(BASE + '/ecd/gerar?exercicio=' + exercicio + '&cnpj=' + encodeURIComponent(ecdCnpj) + '&nome=' + encodeURIComponent(ecdNome), { method: 'POST' });
            const j = await r.json().catch(() => null);
            setEcd(j);
            toast.current?.show({ severity: 'success', summary: 'ECD gerada', life: 3000 });
            carregarEcdHist();
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', life: 3000 }); }
    };
    const baixarEcd = () => {
        if (!ecd?.conteudo) return;
        const blob = new Blob([ecd.conteudo], { type: 'text/plain;charset=utf-8' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = 'ECD-' + ecd.exercicio + '.txt';
        link.click();
        URL.revokeObjectURL(link.href);
    };
        const buscarBalanco = async () => { const r = await apiFetch(BASE + '/balanco?exercicio=' + exercicio); setBalanco(await r.json().catch(() => null)); };
    const msgErro = async (r, padrao) => { const j = await r.json().catch(() => null); return j?.message || j?.errors?.[0]?.message || padrao; };
    const fechar = async () => {
        if (!periodoFech) return;
        const r = await apiFetch(BASE + '/fechamentos/' + periodoFech + '/fechar', { method: 'POST' });
        if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: await msgErro(r, 'Não foi possível fechar o período'), life: 4000 }); return; }
        setPeriodoFech('');
        carregar();
    };
    const reabrir = async (periodo) => {
        if (!window.confirm('Reabrir o período ' + periodo + '? Lançamentos desse período poderão voltar a ser alterados.')) return;
        const r = await apiFetch(BASE + '/fechamentos/' + periodo + '/reabrir', { method: 'POST' });
        if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: await msgErro(r, 'Não foi possível reabrir o período'), life: 4000 }); return; }
        toast.current?.show({ severity: 'success', summary: 'Período reaberto', detail: periodo, life: 3000 });
        carregar();
    };
    const gerarDeTitulo = async () => {
        if (!tit.tituloId || !tit.contaDebitoId || !tit.contaCreditoId) {
            toast.current?.show({ severity: 'warn', summary: 'Campos obrigatórios', detail: 'Informe o título e as contas de débito e crédito', life: 3500 });
            return;
        }
        if (tit.contaDebitoId === tit.contaCreditoId) {
            toast.current?.show({ severity: 'warn', summary: 'Contas iguais', detail: 'Débito e crédito devem ser contas diferentes', life: 3500 });
            return;
        }
        const r = await apiFetch(BASE + '/lancamentos/gerar-titulo/' + tit.tituloId, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ contaDebitoId: tit.contaDebitoId, contaCreditoId: tit.contaCreditoId })
        });
        if (!r.ok) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: await msgErro(r, 'Não foi possível gerar o lançamento do título'), life: 4500 }); return; }
        toast.current?.show({ severity: 'success', summary: 'Lançamento gerado', detail: 'Criado a partir do título ' + tit.tituloId, life: 3000 });
        setDlgTitulo(false);
        setTit({ tituloId: null, contaDebitoId: null, contaCreditoId: null });
        carregar();
    };

    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='flex justify-content-between align-items-center mb-3 flex-wrap gap-2'>
                <div><h2 className='m-0'>Contabilidade</h2><span className='bc-muted'>Diário, razão, balancete, balanço e fechamento</span></div>
                <div className='flex gap-2 flex-wrap'>
                    <Button label='Gerar de título' icon='pi pi-file-import' severity='secondary' outlined onClick={() => setDlgTitulo(true)} />
                    <Button label='Novo lançamento' icon='pi pi-plus' onClick={() => setDlg(true)} />
                </div>
            </div>
            <TabView>
                <TabPanel header='Lançamentos'>
                    <DataTable value={lancs} loading={loading} paginator rows={10} emptyMessage='Nenhum lançamento.' responsiveLayout='scroll' dataKey='id'>
                        <Column field='id' header='#' style={{ width: '4rem' }} />
                        <Column field='data' header='Data' style={{ width: '8rem' }} />
                        <Column field='historico' header='Histórico' />
                        <Column field='periodo' header='Período' style={{ width: '7rem' }} />
                        <Column header='Status' body={(r) => <Tag value={r.status} severity={r.status === 'LANCADO' ? 'success' : r.status === 'ESTORNADO' ? 'danger' : 'warning'} />} style={{ width: '8rem' }} />
                        <Column header='' body={(r) => (<div className='flex gap-1'>
                            {r.status === 'RASCUNHO' && <Button icon='pi pi-check' rounded text severity='success' tooltip='Lançar' onClick={() => lancar(r.id)} />}
                            {r.status === 'LANCADO' && <Button icon='pi pi-replay' rounded text severity='danger' tooltip='Estornar' onClick={() => estornar(r.id)} />}
                        </div>)} style={{ width: '7rem' }} />
                    </DataTable>
                </TabPanel>
                <TabPanel header='Razão'>
                    <div className='flex gap-2 mb-3 flex-wrap'>
                        <Dropdown value={filtroConta} options={contas.map(c => ({ label: (c.codigo || '') + ' - ' + (c.descricao || c.nome || ''), value: c.id }))} onChange={(e) => setFiltroConta(e.value)} placeholder='Conta' filter style={{ minWidth: '16rem' }} />
                        <Calendar value={de} onChange={(e) => setDe(e.value)} dateFormat='dd/mm/yy' placeholder='De' showIcon />
                        <Calendar value={ate} onChange={(e) => setAte(e.value)} dateFormat='dd/mm/yy' placeholder='Até' showIcon />
                        <Button label='Buscar' icon='pi pi-search' onClick={buscarRazao} />
                    </div>
                    <DataTable value={razao} paginator rows={10} emptyMessage='Use os filtros.' responsiveLayout='scroll'>
                        <Column field='lancamentoId' header='Lanç.' style={{ width: '6rem' }} />
                        <Column field='historico' header='Histórico' />
                        <Column header='Débito' body={(r) => fmt(r.debito)} style={{ width: '9rem' }} />
                        <Column header='Crédito' body={(r) => fmt(r.credito)} style={{ width: '9rem' }} />
                        <Column header='Saldo' body={(r) => fmt(r.saldo)} style={{ width: '9rem' }} />
                    </DataTable>
                </TabPanel>
                <TabPanel header='Balancete'>
                    <div className='flex gap-2 mb-3 flex-wrap'>
                        <Calendar value={de} onChange={(e) => setDe(e.value)} dateFormat='dd/mm/yy' placeholder='De' showIcon />
                        <Calendar value={ate} onChange={(e) => setAte(e.value)} dateFormat='dd/mm/yy' placeholder='Até' showIcon />
                        <Button label='Buscar' icon='pi pi-search' onClick={buscarBalancete} />
                    </div>
                    <DataTable value={balancete} paginator rows={15} emptyMessage='Use os filtros.' responsiveLayout='scroll'>
                        <Column field='conta' header='Conta' />
                        <Column header='Débito' body={(r) => fmt(r.debito)} />
                        <Column header='Crédito' body={(r) => fmt(r.credito)} />
                        <Column header='Saldo' body={(r) => fmt(r.saldo)} />
                    </DataTable>
                </TabPanel>
                <TabPanel header='Balanço'>
                    <div className='flex gap-2 mb-3 flex-wrap'>
                        <InputNumber value={exercicio} onValueChange={(e) => setExercicio(e.value)} useGrouping={false} />
                        <Button label='Calcular' icon='pi pi-calculator' onClick={buscarBalanco} />
                    </div>
                    {balanco && (<div className='grid'><div className='bc-form-col-4'><div className='p-3 border-round surface-card'><small>ATIVO</small><div className='text-2xl font-bold'>{fmt(balanco.ATIVO)}</div></div></div><div className='bc-form-col-4'><div className='p-3 border-round surface-card'><small>PASSIVO</small><div className='text-2xl font-bold'>{fmt(balanco.PASSIVO)}</div></div></div><div className='bc-form-col-4'><div className='p-3 border-round surface-card'><small>RESULTADO</small><div className='text-2xl font-bold'>{fmt(balanco.RESULTADO)}</div></div></div></div>)}
                </TabPanel>
                <TabPanel header='Fechamentos'>
                    <div className='flex gap-2 mb-3 flex-wrap'>
                        <InputText value={periodoFech} onChange={(e) => setPeriodoFech(e.target.value)} placeholder='AAAA-MM' maxLength={7} />
                        <Button label='Fechar período' icon='pi pi-lock' onClick={fechar} />
                    </div>
                    <DataTable value={fechs} paginator rows={10} emptyMessage='Nenhum fechamento.' responsiveLayout='scroll' dataKey='id'>
                        <Column field='periodo' header='Período' />
                        <Column field='status' header='Status' />
                        <Column field='fechadoEm' header='Fechado em' />
                        <Column header='' body={(r) => r.status === 'FECHADO' && (<Button label='Reabrir' icon='pi pi-lock-open' size='small' severity='warning' outlined onClick={() => reabrir(r.periodo)} />)} style={{ width: '9rem' }} />
                    </DataTable>
                </TabPanel>
                <TabPanel header='DRE'>
                    <div className='flex gap-2 mb-3 flex-wrap'><InputNumber value={exercicio} onValueChange={(e) => setExercicio(e.value)} useGrouping={false} /><Button label='Calcular DRE' icon='pi pi-calculator' onClick={buscarDre} /></div>
                    <DataTable value={dre} paginator rows={12} emptyMessage='Calcule por exercício.' responsiveLayout='scroll'>
                        <Column field='mes' header='Mês' style={{ width: '5rem' }} />
                        <Column header='Receitas' body={(r) => fmt(r.receitas)} />
                        <Column header='Custos' body={(r) => fmt(r.custos)} />
                        <Column header='Resultado' body={(r) => fmt(r.resultado)} />
                    </DataTable>
                </TabPanel>
                <TabPanel header='ECD'>
                    <div className='flex gap-2 mb-3 flex-wrap align-items-end'>
                        <span><label className='bc-label'>Exercicio</label><InputNumber value={exercicio} onValueChange={(e) => setExercicio(e.value)} useGrouping={false} /></span>
                        <span><label className='bc-label'>CNPJ</label><InputText value={ecdCnpj} onChange={(e) => setEcdCnpj(e.target.value)} placeholder='somente numeros' /></span>
                        <span><label className='bc-label'>Nome empresarial</label><InputText value={ecdNome} onChange={(e) => setEcdNome(e.target.value)} style={{ width: '20rem' }} /></span>
                        <Button label='Gerar ECD (Diario)' icon='pi pi-file' onClick={gerarEcd} /></div>
                    {ecd && <div className='surface-50 border-round p-3 mb-3'><strong>ECD gerada:</strong> {ecd.lancamentos} lancamentos, {ecd.contas} contas, {ecd.totalLinhas} linhas. Demonstracoes (J100/J150): {ecd.demonstracoes}. Confira no PVA antes de entregar.
                        {ecdHist.length > 0 && <div className='mt-2'><small>Geradas: </small>{ecdHist.map((h) => h.exercicio + ' (' + h.status + ')').join('; ')}</div>}
                        <div className='mt-2'><Button label='Baixar .txt' icon='pi pi-download' severity='secondary' outlined onClick={baixarEcd} /></div></div>}
                </TabPanel>
            </TabView>
            <Dialog visible={dlg} onHide={() => setDlg(false)} header='Novo lançamento' modal style={{ width: 'min(96vw, 520px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-6'><label className='bc-label'>Data *</label><Calendar value={f.data} onChange={(e) => setF({ ...f, data: e.value })} dateFormat='dd/mm/yy' showIcon /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Histórico *</label><InputText value={f.historico} onChange={(e) => setF({ ...f, historico: e.target.value })} /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'>
                    <Button label='Cancelar' text severity='secondary' onClick={() => setDlg(false)} />
                    <Button label='Salvar rascunho' icon='pi pi-check' onClick={salvar} />
                </div>
            </Dialog>
            <Dialog visible={dlgTitulo} onHide={() => setDlgTitulo(false)} header='Gerar lançamento de título' modal style={{ width: 'min(96vw, 520px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-12'><label className='bc-label'>ID do título *</label><InputNumber value={tit.tituloId} onValueChange={(e) => setTit({ ...tit, tituloId: e.value })} useGrouping={false} /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Conta de débito *</label><Dropdown value={tit.contaDebitoId} options={contas.map(c => ({ label: (c.codigo || '') + ' - ' + (c.descricao || c.nome || ''), value: c.id }))} onChange={(e) => setTit({ ...tit, contaDebitoId: e.value })} filter placeholder='Selecione' /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Conta de crédito *</label><Dropdown value={tit.contaCreditoId} options={contas.map(c => ({ label: (c.codigo || '') + ' - ' + (c.descricao || c.nome || ''), value: c.id }))} onChange={(e) => setTit({ ...tit, contaCreditoId: e.value })} filter placeholder='Selecione' /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'>
                    <Button label='Cancelar' text severity='secondary' onClick={() => setDlgTitulo(false)} />
                    <Button label='Gerar lançamento' icon='pi pi-check' onClick={gerarDeTitulo} />
                </div>
            </Dialog>
        </div>
    );
};
export default Contabilidade;
