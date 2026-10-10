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
    const [mudancas, sett('plm.mudancas')] = useState([]);
    const [revisoes, sett('plm.revisoes')] = useState([]);
    const [produtoId, sett('plm.produto')Id] = useState(null);
    const [dlgMudanca, setDlgMudanca] = useState(false);
    const [dlgt('plm.efeito'), setDlgt('plm.efeito')] = useState(null);
    const [dlgt('plm.revisao'), setDlgt('plm.revisao')] = useState(false);
    const [dlgt('plm.decidir'), setDlgt('plm.decidir')] = useState(null);
    const [fm, setFm] = useState({ numero: '', titulo: '', descricao: '' });
    const [fe, setFe] = useState({ entidadeTipo: 'PRODUTO', entidadeId: null, acao: 'REGISTRAR', ordemExecucao: 1, observacao: '' });
    const [fr, setFr] = useState({ produtoId: null, revisao: '', descricao: '', motivo: '' });
    const [observacao, sett('plm.observacao')] = useState('');
    const [processando, setProcessando] = useState(false);

    const carregart('plm.mudancas') = async () => {
        try {
            const r = await PlmService.mudancas();
            sett('plm.mudancas')(comoLista(r));
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 't('plm.falhaCarregar')', life: 4000 }); }
    };
    const carregart('plm.revisoes') = async () => {
        if (!produtoId) return;
        try {
            const r = await PlmService.revisoes(produtoId);
            sett('plm.revisoes')(comoLista(r));
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 't('plm.falhaCarregar')', life: 4000 }); }
    };

    useEffect(() => { carregart('plm.mudancas')(); }, []);

    const salvarMudanca = async () => {
        if (!fm.numero?.trim() || !fm.titulo?.trim()) return;
        setProcessando(true);
        try {
            await PlmService.criarMudanca({ numero: fm.numero.trim(), titulo: fm.titulo.trim(), descricao: fm.descricao });
            setDlgMudanca(false); setFm({ numero: '', titulo: '', descricao: '' });
            await carregart('plm.mudancas')();
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 't('plm.falhaCriarMudanca')', life: 4000 }); }
        finally { setProcessando(false); }
    };

    const acaoMudanca = async (fn, okMsg) => {
        setProcessando(true);
        try { await fn(); await carregart('plm.mudancas')(); if (okMsg) toast.current?.show({ severity: 'success', summary: okMsg, life: 3000 }); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 't('plm.operacaoRecusada')', life: 4500 }); }
        finally { setProcessando(false); }
    };

    const salvart('plm.efeito') = async () => {
        if (!dlgt('plm.efeito')) return;
        if (!fe.entidadeId) { toast.current?.show({ severity: "warn", summary: "Campo obrigatorio", detail: "Informe o ID da entidade do efeito.", life: 3500 }); return; }
        setProcessando(true);
        try {
            await PlmService.adicionart('plm.efeito')({ mudancaId: dlgt('plm.efeito').id, entidadeTipo: fe.entidadeTipo, entidadeId: fe.entidadeId, acao: fe.acao, ordemExecucao: fe.ordemExecucao || 1, observacao: fe.observacao });
            setDlgt('plm.efeito')(null); await carregart('plm.mudancas')();
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 't('plm.falhat('plm.efeito')')', life: 4000 }); }
        finally { setProcessando(false); }
    };

    const salvart('plm.revisao') = async () => {
        if (!fr.produtoId || !fr.revisao?.trim()) return;
        setProcessando(true);
        try {
            await PlmService.criart('plm.revisao')({ produtoId: fr.produtoId, revisao: fr.revisao.trim(), descricao: fr.descricao, motivo: fr.motivo });
            setDlgt('plm.revisao')(false); setFr({ produtoId: null, revisao: '', descricao: '', motivo: '' });
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 't('plm.falhat('plm.revisao')')', life: 4000 }); }
        finally { setProcessando(false); }
    };

    const statusTag = (s) => <Tag value={s} severity={s === 'APROVADA' || s === 'IMPLEMENTADA' || s === 'VIGENTE' ? 'success' : (s === 'REJEITADA' || s === 'ERRO' ? 'danger' : 'warning')} />;

    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <h2 className='m-0'>t('plm.tituloPagina')</h2>
            <p className='bc-muted'>t('plm.subtitulo')</p>
            <TabView>
                <TabPanel header='t('plm.mudancas')'>
                    <Button label='t('plm.novaMudanca')' icon='pi pi-plus' onClick={() => setDlgMudanca(true)} />
                    <DataTable value={mudancas} paginator rows={10} emptyMessage='t('plm.semt('plm.mudancas')')' responsiveLayout='scroll' dataKey='id' style={{ marginTop: '1rem' }}>
                        <Column field='numero' header='t('plm.numero')' />
                        <Column field='titulo' header='t('plm.titulo')' />
                        <Column field='status' header='t('plm.status')' body={(r) => statusTag(r.status)} />
                        <Column header='t('plm.acoes')' body={(r) => (
                            <div className='flex gap-1'>
                                {r.status === 'ABERTA' && (<><Button label='t('plm.efeito')' size='small' outlined onClick={() => setDlgt('plm.efeito')(r)} /><Button label='t('plm.enviar')' size='small' onClick={() => acaoMudanca(() => PlmService.enviarAprovacao(r.id, {}), 't('plm.enviada')')} /></>)}
                                {r.status === 'EM_APROVACAO' && (<><Button label='t('plm.aprovar')' size='small' severity='success' onClick={() => { setDlgt('plm.decidir')({ row: r, aprovar: true }); sett('plm.observacao')(''); }} /><Button label='t('plm.rejeitar')' size='small' severity='danger' outlined onClick={() => { setDlgt('plm.decidir')({ row: r, aprovar: false }); sett('plm.observacao')(''); }} /></>)}
                                {r.status === 'APROVADA' && (<Button label='t('plm.implementar')' size='small' severity='warning' onClick={() => acaoMudanca(() => PlmService.implementar(r.id), 't('plm.implementada')')} />)}
                            </div>
                        )} />
                    </DataTable>
                </TabPanel>
                <TabPanel header='t('plm.revisoes')'>
                    <div className='flex gap-2 mb-3 flex-wrap align-items-end'>
                        <span><label className='bc-label'>t('plm.produto')</label><InputNumber value={produtoId} onValueChange={(e) => sett('plm.produto')Id(e.value)} useGrouping={false} /></span>
                        <Button label='t('plm.buscar')' icon='pi pi-search' onClick={carregart('plm.revisoes')} />
                        <Button label='t('plm.novat('plm.revisao')')' icon='pi pi-plus' onClick={() => setDlgt('plm.revisao')(true)} />
                    </div>
                    <DataTable value={revisoes} paginator rows={10} emptyMessage='t('plm.informet('plm.produto')')' responsiveLayout='scroll' dataKey='id'>
                        <Column field='revisao' header='t('plm.revisao')' />
                        <Column field='descricao' header='t('plm.descricao')' />
                        <Column field='status' header='t('plm.status')' body={(r) => statusTag(r.status)} />
                        <Column header='t('plm.acoes')' body={(r) => (r.status !== 'VIGENTE' && (<Button label='t('plm.vigorar')' size='small' onClick={async () => { await PlmService.vigorart('plm.revisao')(r.id); carregart('plm.revisoes')(); }} />))} />
                    </DataTable>
                </TabPanel>
            </TabView>
            <Dialog header='t('plm.novaMudanca')' visible={dlgMudanca} style={{ width: '32rem' }} onHide={() => setDlgMudanca(false)}>
                <div className='field'><label>t('plm.numero') *</label><InputText value={fm.numero} onChange={(e) => setFm({ ...fm, numero: e.target.value })} className='w-full' /></div>
                <div className='field'><label>t('plm.titulo') *</label><InputText value={fm.titulo} onChange={(e) => setFm({ ...fm, titulo: e.target.value })} className='w-full' /></div>
                <div className='field'><label>t('plm.descricao')</label><InputTextarea value={fm.descricao} onChange={(e) => setFm({ ...fm, descricao: e.target.value })} rows={3} className='w-full' /></div>
                <Button label='t('plm.salvar')' icon='pi pi-check' loading={processando} onClick={salvarMudanca} />
            </Dialog>
            <Dialog header='t('plm.novot('plm.efeito')')' visible={dlgt('plm.efeito') !== null} style={{ width: '32rem' }} onHide={() => setDlgt('plm.efeito')(null)}>
                <div className='field'><label>t('plm.entidadeId')</label><InputNumber value={fe.entidadeId} onValueChange={(e) => setFe({ ...fe, entidadeId: e.value })} useGrouping={false} /></div>
                <div className='field'><label>t('plm.acao')</label><Dropdown value={fe.acao} options={[{ label: 't('plm.registrarEvidencia')', value: 'REGISTRAR' }, { label: 't('plm.vigorart('plm.revisao')')', value: 'VIGORAR_REVISAO' }]} onChange={(e) => setFe({ ...fe, acao: e.value })} /></div>
                <div className='field'><label>t('plm.ordem')</label><InputNumber value={fe.ordemExecucao} onValueChange={(e) => setFe({ ...fe, ordemExecucao: e.value })} /></div>
                <Button label='t('plm.salvar')' icon='pi pi-check' loading={processando} onClick={salvart('plm.efeito')} />
            </Dialog>
            <Dialog header='t('plm.decidir')' visible={dlgt('plm.decidir') !== null} style={{ width: '32rem' }} onHide={() => setDlgt('plm.decidir')(null)}>
                <div className='field'><label>t('plm.observacao')</label><InputTextarea value={observacao} onChange={(e) => sett('plm.observacao')(e.target.value)} rows={3} className='w-full' /></div>
                <Button label='t('plm.confirmar')' icon='pi pi-check' loading={processando} onClick={async () => { await acaoMudanca(() => PlmService.decidir(dlgt('plm.decidir').row.id, dlgt('plm.decidir').aprovar, observacao), 't('plm.decidida')'); setDlgt('plm.decidir')(null); }} />
            </Dialog>
            <Dialog header='t('plm.novat('plm.revisao')')' visible={dlgt('plm.revisao')} style={{ width: '32rem' }} onHide={() => setDlgt('plm.revisao')(false)}>
                <div className='field'><label>t('plm.produto') ID *</label><InputNumber value={fr.produtoId} onValueChange={(e) => setFr({ ...fr, produtoId: e.value })} useGrouping={false} /></div>
                <div className='field'><label>t('plm.revisao') *</label><InputText value={fr.revisao} onChange={(e) => setFr({ ...fr, revisao: e.target.value })} className='w-full' /></div>
                <div className='field'><label>t('plm.motivo')</label><InputTextarea value={fr.motivo} onChange={(e) => setFr({ ...fr, motivo: e.target.value })} rows={3} className='w-full' /></div>
                <Button label='t('plm.salvar')' icon='pi pi-check' loading={processando} onClick={salvart('plm.revisao')} />
            </Dialog>
        </div>
    );
};

export default Plm;
