import React, { useEffect, useState, useRef } from 'react';
import { Card } from 'primereact/card';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { InputNumber } from 'primereact/inputnumber';
import { InputTextarea } from 'primereact/inputtextarea';
import { Dropdown } from 'primereact/dropdown';
import { TabView, TabPanel } from 'primereact/tabview';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { useTranslation } from 'react-i18next';
import PlmService from '../../services/PlmService';

const comoLista = (r) => {
    const d = r && r.data !== undefined ? r.data : r;
    return Array.isArray(d) ? d : [];
};

export const Plm = () => {
    const { t } = useTranslation();
    const toast = useRef(null);
    const [mudancas, setMudancas] = useState([]);
    const [revisoes, setRevisoes] = useState([]);
    const [produtoId, setProdutoId] = useState(null);
    const [dlgMudanca, setDlgMudanca] = useState(false);
    const [dlgEfeito, setDlgEfeito] = useState(null);
    const [dlgRevisao, setDlgRevisao] = useState(false);
    const [dlgDecidir, setDlgDecidir] = useState(null);
    const [fm, setFm] = useState({ numero: '', titulo: '', descricao: '' });
    const [fe, setFe] = useState({ entidadeTipo: 'PRODUTO', entidadeId: null, acao: 'REGISTRAR', ordemExecucao: 1, observacao: '' });
    const [fr, setFr] = useState({ produtoId: null, revisao: '', descricao: '', motivo: '' });
    const [observacao, setObservacao] = useState('');
    const [processando, setProcessando] = useState(false);

    const carregarMudancas = async () => {
        try {
            const r = await PlmService.mudancas();
            setMudancas(comoLista(r));
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: t('plm.falhaCarregar'), life: 4000 }); }
    };
    const carregarRevisoes = async () => {
        if (!produtoId) return;
        try {
            const r = await PlmService.revisoes(produtoId);
            setRevisoes(comoLista(r));
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: t('plm.falhaCarregar'), life: 4000 }); }
    };

    useEffect(() => { carregarMudancas(); }, []);

    const salvarMudanca = async () => {
        if (!fm.numero?.trim() || !fm.titulo?.trim()) return;
        setProcessando(true);
        try {
            await PlmService.criarMudanca({ numero: fm.numero.trim(), titulo: fm.titulo.trim(), descricao: fm.descricao });
            setDlgMudanca(false); setFm({ numero: '', titulo: '', descricao: '' });
            await carregarMudancas();
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: t('plm.falhaCriarMudanca'), life: 4000 }); }
        finally { setProcessando(false); }
    };

    const acaoMudanca = async (fn, okMsg) => {
        setProcessando(true);
        try { await fn(); await carregarMudancas(); if (okMsg) toast.current?.show({ severity: 'success', summary: okMsg, life: 3000 }); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: t('plm.operacaoRecusada'), life: 4500 }); }
        finally { setProcessando(false); }
    };

    const salvarEfeito = async () => {
        if (!dlgEfeito || !fe.entidadeId) {
            if (!fe.entidadeId) toast.current?.show({ severity: 'warn', summary: t('plm.atencao'), detail: t('plm.informeEntidade'), life: 3500 });
            return;
        }
        setProcessando(true);
        try {
            await PlmService.adicionarEfeito({ mudancaId: dlgEfeito.id, entidadeTipo: fe.entidadeTipo, entidadeId: fe.entidadeId, acao: fe.acao, ordemExecucao: fe.ordemExecucao || 1, observacao: fe.observacao });
            setDlgEfeito(null); await carregarMudancas();
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: t('plm.falhaEfeito'), life: 4000 }); }
        finally { setProcessando(false); }
    };

    const salvarRevisao = async () => {
        if (!fr.produtoId || !fr.revisao?.trim()) return;
        setProcessando(true);
        try {
            await PlmService.criarRevisao({ produtoId: fr.produtoId, revisao: fr.revisao.trim(), descricao: fr.descricao, motivo: fr.motivo });
            setDlgRevisao(false); setFr({ produtoId: null, revisao: '', descricao: '', motivo: '' });
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: t('plm.falhaRevisao'), life: 4000 }); }
        finally { setProcessando(false); }
    };

    const statusTag = (s) => <Tag value={s} severity={s === 'APROVADA' || s === 'IMPLEMENTADA' || s === 'VIGENTE' ? 'success' : (s === 'REJEITADA' || s === 'ERRO' ? 'danger' : 'warning')} />;

    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <h2 className='m-0'>{t('plm.tituloPagina')}</h2>
            <p className='bc-muted'>{t('plm.subtitulo')}</p>
            <TabView>
                <TabPanel header={t('plm.mudancas')}>
                    <Button label={t('plm.novaMudanca')} icon='pi pi-plus' onClick={() => setDlgMudanca(true)} />
                    <DataTable value={mudancas} paginator rows={10} emptyMessage={t('plm.semMudancas')} responsiveLayout='scroll' dataKey='id' style={{ marginTop: '1rem' }}>
                        <Column field='numero' header={t('plm.numero')} />
                        <Column field='titulo' header={t('plm.titulo')} />
                        <Column field='status' header={t('plm.status')} body={(r) => statusTag(r.status)} />
                        <Column header={t('plm.acoes')} body={(r) => (
                            <div className='flex gap-1'>
                                {r.status === 'ABERTA' && (<><Button label={t('plm.efeito')} size='small' outlined onClick={() => setDlgEfeito(r)} /><Button label={t('plm.enviar')} size='small' onClick={() => acaoMudanca(() => PlmService.enviarAprovacao(r.id, {}), t('plm.enviada'))} /></>)}
                                {r.status === 'EM_APROVACAO' && (<><Button label={t('plm.aprovar')} size='small' severity='success' onClick={() => { setDlgDecidir({ row: r, aprovar: true }); setObservacao(''); }} /><Button label={t('plm.rejeitar')} size='small' severity='danger' outlined onClick={() => { setDlgDecidir({ row: r, aprovar: false }); setObservacao(''); }} /></>)}
                                {r.status === 'APROVADA' && (<Button label={t('plm.implementar')} size='small' severity='warning' onClick={() => acaoMudanca(() => PlmService.implementar(r.id), t('plm.implementada'))} />)}
                            </div>
                        )} />
                    </DataTable>
                </TabPanel>
                <TabPanel header={t('plm.revisoes')}>
                    <div className='flex gap-2 mb-3 flex-wrap align-items-end'>
                        <span><label className='bc-label'>{t('plm.produto')}</label><InputNumber value={produtoId} onValueChange={(e) => setProdutoId(e.value)} useGrouping={false} /></span>
                        <Button label={t('plm.buscar')} icon='pi pi-search' onClick={carregarRevisoes} />
                        <Button label={t('plm.novaRevisao')} icon='pi pi-plus' onClick={() => setDlgRevisao(true)} />
                    </div>
                    <DataTable value={revisoes} paginator rows={10} emptyMessage={t('plm.informeProduto')} responsiveLayout='scroll' dataKey='id'>
                        <Column field='revisao' header={t('plm.revisao')} />
                        <Column field='descricao' header={t('plm.descricao')} />
                        <Column field='status' header={t('plm.status')} body={(r) => statusTag(r.status)} />
                        <Column header={t('plm.acoes')} body={(r) => (r.status !== 'VIGENTE' && (<Button label={t('plm.vigorar')} size='small' onClick={async () => { await PlmService.vigorarRevisao(r.id); carregarRevisoes(); }} />))} />
                    </DataTable>
                </TabPanel>
            </TabView>
            <Dialog header={t('plm.novaMudanca')} visible={dlgMudanca} style={{ width: '32rem' }} onHide={() => setDlgMudanca(false)}>
                <div className='field'><label>{t('plm.numero')} *</label><InputText value={fm.numero} onChange={(e) => setFm({ ...fm, numero: e.target.value })} className='w-full' /></div>
                <div className='field'><label>{t('plm.titulo')} *</label><InputText value={fm.titulo} onChange={(e) => setFm({ ...fm, titulo: e.target.value })} className='w-full' /></div>
                <div className='field'><label>{t('plm.descricao')}</label><InputTextarea value={fm.descricao} onChange={(e) => setFm({ ...fm, descricao: e.target.value })} rows={3} className='w-full' /></div>
                <Button label={t('plm.salvar')} icon='pi pi-check' loading={processando} onClick={salvarMudanca} />
            </Dialog>
            <Dialog header={t('plm.novoEfeito')} visible={dlgEfeito !== null} style={{ width: '32rem' }} onHide={() => setDlgEfeito(null)}>
                <div className='field'><label>{t('plm.entidadeId')} *</label><InputNumber value={fe.entidadeId} onValueChange={(e) => setFe({ ...fe, entidadeId: e.value })} useGrouping={false} /></div>
                <div className='field'><label>{t('plm.acao')}</label><Dropdown value={fe.acao} options={[{ label: t('plm.registrarEvidencia'), value: 'REGISTRAR' }, { label: t('plm.vigorarRevisao'), value: 'VIGORAR_REVISAO' }]} onChange={(e) => setFe({ ...fe, acao: e.value })} /></div>
                <div className='field'><label>{t('plm.ordem')}</label><InputNumber value={fe.ordemExecucao} onValueChange={(e) => setFe({ ...fe, ordemExecucao: e.value })} /></div>
                <Button label={t('plm.salvar')} icon='pi pi-check' loading={processando} onClick={salvarEfeito} />
            </Dialog>
            <Dialog header={t('plm.decidir')} visible={dlgDecidir !== null} style={{ width: '32rem' }} onHide={() => setDlgDecidir(null)}>
                <div className='field'><label>{t('plm.observacao')}</label><InputTextarea value={observacao} onChange={(e) => setObservacao(e.target.value)} rows={3} className='w-full' /></div>
                <Button label={t('plm.confirmar')} icon='pi pi-check' loading={processando} onClick={async () => { await acaoMudanca(() => PlmService.decidir(dlgDecidir.row.id, dlgDecidir.aprovar, observacao), t('plm.decidida')); setDlgDecidir(null); }} />
            </Dialog>
            <Dialog header={t('plm.novaRevisao')} visible={dlgRevisao} style={{ width: '32rem' }} onHide={() => setDlgRevisao(false)}>
                <div className='field'><label>{t('plm.produto')} *</label><InputNumber value={fr.produtoId} onValueChange={(e) => setFr({ ...fr, produtoId: e.value })} useGrouping={false} /></div>
                <div className='field'><label>{t('plm.revisao')} *</label><InputText value={fr.revisao} onChange={(e) => setFr({ ...fr, revisao: e.target.value })} className='w-full' /></div>
                <div className='field'><label>{t('plm.motivo')}</label><InputTextarea value={fr.motivo} onChange={(e) => setFr({ ...fr, motivo: e.target.value })} rows={3} className='w-full' /></div>
                <Button label={t('plm.salvar')} icon='pi pi-check' loading={processando} onClick={salvarRevisao} />
            </Dialog>
        </div>
    );
};

export default Plm;
