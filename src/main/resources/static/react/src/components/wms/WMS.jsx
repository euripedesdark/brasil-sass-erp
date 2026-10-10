import React, { useState, useEffect, useRef, useCallback } from 'react';
import { apiFetch } from '../../services/ApiConfig';
import { Button } from 'primereact/button';
import { Card } from 'primereact/card';
import { Column } from 'primereact/column';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { TabView, TabPanel } from 'primereact/tabview';
import { Tag } from 'primereact/tag';
import { Toast } from 'primereact/toast';
import { localeAtivo } from '../shared/LocaleData.js';

const BASE = '/api/wms';
const fmt = (v) => Number(v ?? 0).toLocaleString(localeAtivo(), { minimumFractionDigits: 3 });

export const WMS = () => {
    const toast = useRef(null);
    const [ondas, setOndas] = useState([]);
    const [itens, setItens] = useState([]);
    const [ondaSel, setOndaSel] = useState(null);
    const [loading, setLoading] = useState(true);
    const [dlgOnda, setDlgOnda] = useState(false);
    const [dlgItem, setDlgItem] = useState(false);
    const [fOnda, setFOnda] = useState({});
    const [fItem, setFItem] = useState({});
    const [depId, setDepId] = useState('');
    const [prodId, setProdId] = useState('');
    const [sug, setSug] = useState(null);
    const [expId, setExpId] = useState('');
    const [vols, setVols] = useState([]);
    const [conf, setConf] = useState(null);

    const erroDe = (j, padrao) => j?.message || j?.errors?.[0]?.message || padrao;
    const chamar = async (url, method, corpo, padrao) => {
        const r = await apiFetch(BASE + url, { method, headers: { 'Content-Type': 'application/json' }, body: corpo === undefined ? undefined : JSON.stringify(corpo) });
        const j = await r.json().catch(() => null);
        if (!r.ok) throw new Error(erroDe(j, padrao || 'Operação recusada'));
        return j?.data ?? j;
    };
    const falha = (e) => toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4500 });
    const ok = (msg) => toast.current?.show({ severity: 'success', summary: msg, life: 2500 });
    const [qtdSep, setQtdSep] = useState({});
    const [rastreio, setRastreio] = useState('');
    const [dlgVol, setDlgVol] = useState(false);
    const [fVol, setFVol] = useState({ codigo: '', peso: null });
    const js = async (r) => { const j = await r.json().catch(() => null); return Array.isArray(j) ? j : (j?.data ?? j ?? []); };
    const carregar = useCallback(async () => {
        setLoading(true);
        try { setOndas(await apiFetch(BASE + '/ondas').then(js)); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: 'Falha ao carregar WMS', life: 4000 }); }
        finally { setLoading(false); }
    }, []);
    useEffect(() => { carregar(); }, [carregar]);
    const verItens = async (o) => { setOndaSel(o); setItens(await apiFetch(BASE + '/ondas/' + o.id + '/itens').then(js)); };
    const salvarOnda = async () => {
        if (!fOnda.depositoId) { toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Informe o depósito', life: 3500 }); return; }
        try { await chamar('/ondas', 'POST', { depositoId: Number(fOnda.depositoId), codigo: fOnda.codigo, responsavel: fOnda.responsavel }); setDlgOnda(false); setFOnda({}); ok('Onda criada'); carregar(); }
        catch (e) { falha(e); }
    };
    const salvarItem = async () => {
        if (!fItem.produtoId || !(Number(fItem.qtd) > 0)) { toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Informe produto e quantidade maior que zero', life: 3500 }); return; }
        try { await chamar('/ondas/' + ondaSel.id + '/itens', 'POST', { produtoId: Number(fItem.produtoId), qtdSolicitada: Number(fItem.qtd) }); setDlgItem(false); setFItem({}); ok('Item adicionado'); verItens(ondaSel); }
        catch (e) { falha(e); }
    };
    const gerarDeReservas = async () => {
        if (!depId) { toast.current?.show({ severity: 'warn', summary: 'Informe o deposito', detail: 'Use o campo Deposito ID da aba put-away ou digite aqui.', life: 4000 }); return; }
        try {
            const r = await apiFetch(BASE + '/ondas/gerar-de-reservas?depositoId=' + depId, { method: 'POST' });
            const j = await r.json().catch(() => null);
            if (!r.ok) throw new Error(j?.message || 'Falhou');
            toast.current?.show({ severity: 'success', summary: 'Onda ' + (j?.codigo || j?.data?.codigo) + ' criada', detail: (j?.itens ?? j?.data?.itens ?? 0) + ' itens de reservas', life: 4000 });
            carregar();
        } catch (e) { toast.current?.show({ severity: 'error', summary: 'Erro', detail: e.message, life: 4000 }); }
    };
    const liberar = async (id) => { try { await chamar('/ondas/' + id + '/liberar', 'POST'); ok('Onda liberada'); carregar(); } catch (e) { falha(e); } };
    const restante = (r) => Math.max(0, Number(r.qtdSolicitada ?? 0) - Number(r.qtdSeparada ?? 0));
    const separar = async (r) => {
        const qtd = qtdSep[r.id] ?? restante(r);
        if (!(Number(qtd) > 0)) { toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Informe uma quantidade maior que zero', life: 3500 }); return; }
        try { await chamar('/ondas/' + ondaSel.id + '/itens/' + r.id + '/separar', 'POST', { quantidade: Number(qtd) }); setQtdSep((m) => ({ ...m, [r.id]: undefined })); ok('Separação registrada'); verItens(ondaSel); }
        catch (e) { falha(e); }
    };
    const concluir = async (id) => { try { await chamar('/ondas/' + id + '/concluir', 'POST', undefined, 'Há itens não separados'); ok('Onda concluída'); } catch (e) { toast.current?.show({ severity: 'warn', summary: 'Pendente', detail: e.message, life: 3500 }); } carregar(); };
    const sugerir = async () => { const r = await apiFetch(BASE + '/putaway?depositoId=' + depId + '&produtoId=' + prodId); setSug(await r.json().catch(() => null)); };
    const carregarVols = async () => { if (!expId) return; try { const r = await apiFetch(BASE + '/volumes?expedicaoId=' + expId); if (!r.ok) throw new Error(erroDe(await r.json().catch(() => null), 'Expedição não encontrada')); setVols(await js(r)); } catch (e) { setVols([]); falha(e); } };
    const [dlgEmb, setDlgEmb] = useState(false);
    const [volEmb, setVolEmb] = useState(null);
    const [fEmb, setFEmb] = useState({ produtoId: '', quantidade: null, ondaItemId: '' });
    const abrirEmbalar = (v) => { setVolEmb(v); setFEmb({ produtoId: '', quantidade: null, ondaItemId: '' }); setDlgEmb(true); };
    const embalar = async () => {
        if (!fEmb.produtoId || !fEmb.quantidade || fEmb.quantidade <= 0) {
            toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Informe o produto e uma quantidade maior que zero', life: 3500 });
            return;
        }
        const r = await apiFetch(BASE + '/volumes/' + volEmb.id + '/itens', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ produtoId: Number(fEmb.produtoId), quantidade: fEmb.quantidade, ondaItemId: fEmb.ondaItemId ? Number(fEmb.ondaItemId) : null })
        });
        if (!r.ok) {
            const j = await r.json().catch(() => null);
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: j?.message || j?.errors?.[0]?.message || 'Item recusado (volume fechado ou fora da expedição)', life: 4500 });
            return;
        }
        toast.current?.show({ severity: 'success', summary: 'Item embalado', life: 2500 });
        setDlgEmb(false);
        carregarVols();
    };
    const fecharVolume = async (v) => {
        if (!window.confirm('Fechar o volume ' + (v.codigo || v.id) + '? Depois de fechado não recebe mais itens.')) return;
        const r = await apiFetch(BASE + '/volumes/' + v.id + '/fechar', { method: 'POST' });
        if (!r.ok) {
            const j = await r.json().catch(() => null);
            toast.current?.show({ severity: 'error', summary: 'Erro', detail: j?.message || j?.errors?.[0]?.message || 'Não foi possível fechar o volume', life: 4000 });
            return;
        }
        toast.current?.show({ severity: 'success', summary: 'Volume fechado', life: 2500 });
        carregarVols();
    };
    const criarVolume = async () => {
        if (!expId) { toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Informe a expedição', life: 3500 }); return; }
        if (!fVol.codigo.trim()) { toast.current?.show({ severity: 'warn', summary: 'Atenção', detail: 'Informe o código do volume', life: 3500 }); return; }
        try { await chamar('/volumes', 'POST', { expedicaoId: Number(expId), codigo: fVol.codigo.trim(), peso: fVol.peso }); setDlgVol(false); setFVol({ codigo: '', peso: null }); ok('Volume criado'); carregarVols(); }
        catch (e) { falha(e); }
    };
    const conferir = async () => { if (!expId) return; const r = await apiFetch(BASE + '/expedicoes/' + expId + '/conferencia'); setConf(await r.json().catch(() => null)); };
    const finalizar = async () => {
        if (!expId) return;
        if (!window.confirm('Finalizar a expedição ' + expId + '? As reservas serão consumidas.')) return;
        try { await chamar('/expedicoes/' + expId + '/finalizar', 'POST', { codigoRastreio: rastreio || null }); ok('Expedição finalizada'); carregarVols(); conferir(); }
        catch (e) { toast.current?.show({ severity: 'error', summary: 'Não finalizada', detail: e.message, life: 5000 }); }
    };

    return (
        <div className='p-4'>
            <Toast ref={toast} />
            <div className='flex justify-content-between align-items-center mb-3 flex-wrap gap-2'>
                <div><h2 className='m-0'>WMS</h2><span className='bc-muted'>Ondas, packing, put-away e expedição avançada</span></div>
                
                <span className='flex gap-2'>
                <Button label='Onda de reservas' icon='pi pi-bolt' outlined onClick={gerarDeReservas} tooltip='Gera onda das reservas do deposito (campo Deposito ID abaixo)' />
                <Button label='Nova onda' icon='pi pi-plus' onClick={() => { setFOnda({}); setDlgOnda(true); }} />
                </span>
            </div>
            <TabView>
                <TabPanel header='Ondas'>
                    <DataTable value={ondas} loading={loading} paginator rows={10} emptyMessage='Nenhuma onda.' responsiveLayout='scroll' dataKey='id' selectionMode='single' selection={ondaSel} onSelectionChange={(e) => e.value && verItens(e.value)}>
                        <Column field='codigo' header='Código' />
                        <Column field='status' header='Status' body={(r) => <Tag value={r.status} severity={r.status === 'CONCLUIDA' ? 'success' : r.status === 'ABERTA' ? 'info' : 'warning'} />} />
                        <Column field='responsavel' header='Responsável' />
                        <Column header='' body={(r) => (<div className='flex gap-1'>
                            {r.status === 'ABERTA' && <Button label='Liberar' size='small' outlined onClick={() => liberar(r.id)} />}
                            {r.status !== 'CONCLUIDA' && <Button label='Concluir' size='small' severity='success' onClick={() => concluir(r.id)} />}
                        </div>)} />
                    </DataTable>
                    {ondaSel && (<Card title={'Onda ' + ondaSel.codigo} className='mt-3'>
                        <div className='flex justify-end mb-2'><Button label='Adicionar item' size='small' icon='pi pi-plus' outlined onClick={() => { setFItem({}); setDlgItem(true); }} /></div>
                        <DataTable value={itens} paginator rows={8} emptyMessage='Sem itens.' responsiveLayout='scroll' dataKey='id'>
                            <Column field='produtoId' header='Produto' />
                            <Column header='Solicitada' body={(r) => fmt(r.qtdSolicitada)} />
                            <Column header='Separada' body={(r) => fmt(r.qtdSeparada)} />
                            <Column field='status' header='Status' />
                            <Column header='Separar' body={(r) => (r.status === 'SEPARADO' ? <Tag value='Completo' severity='success' /> : (<div className='flex gap-1 align-items-center'>
                                <InputNumber value={qtdSep[r.id] ?? restante(r)} onValueChange={(e) => setQtdSep((m) => ({ ...m, [r.id]: e.value }))} min={0} max={restante(r)} minFractionDigits={0} maxFractionDigits={3} inputStyle={{ width: '6rem' }} />
                                <Button label='Separar' size='small' onClick={() => separar(r)} disabled={ondaSel?.status !== 'EM_SEPARACAO'} tooltip={ondaSel?.status !== 'EM_SEPARACAO' ? 'Libere a onda para separar' : undefined} />
                            </div>))} style={{ width: '16rem' }} />
                        </DataTable>
                    </Card>)}
                </TabPanel>
                <TabPanel header='Put-away'>
                    <div className='flex gap-2 mb-3 flex-wrap'><InputText value={depId} onChange={(e) => setDepId(e.target.value)} placeholder='Depósito ID' /><InputText value={prodId} onChange={(e) => setProdId(e.target.value)} placeholder='Produto ID' /><Button label='Sugerir endereço' icon='pi pi-search' onClick={sugerir} /></div>
                    {sug && (<Card><p>Picking: <strong>{sug.enderecoPickingCodigo || '-'}</strong></p><p>Pulmão: <strong>{sug.enderecoPulmaoCodigo || '-'}</strong></p></Card>)}
                </TabPanel>
                <TabPanel header='Packing / Expedição'>
                    <div className='flex gap-2 mb-3 flex-wrap'><InputText value={expId} onChange={(e) => setExpId(e.target.value)} placeholder='Expedição ID' /><Button label='Volumes' outlined onClick={carregarVols} /><Button label='Novo volume' icon='pi pi-box' outlined disabled={!expId} onClick={() => { setFVol({ codigo: '', peso: null }); setDlgVol(true); }} /><InputText value={rastreio} onChange={(e) => setRastreio(e.target.value)} placeholder='Código de rastreio' /><Button label='Conferir' icon='pi pi-check' severity='success' onClick={conferir} /><Button label='Finalizar' icon='pi pi-send' severity='help' onClick={finalizar} /></div>
                    {conf && (<Card className='mb-3'><h3 className='m-0'>Conferência: {conf.conferido ? 'OK' : 'DIVERGENTE'}</h3><ul>{(conf.diferencas || []).map((d, i) => (<li key={i}>Produto {d.produtoId}: esperado {fmt(d.esperado)} x embalado {fmt(d.embalado)}</li>))}</ul></Card>)}
                    <DataTable value={vols} paginator rows={8} emptyMessage='Informe a expedição.' responsiveLayout='scroll' dataKey='id'>
                        <Column field='codigo' header='Volume' />
                        <Column field='status' header='Status' />
                        <Column header='' body={(v) => (v.status === 'ABERTO' ? (<div className='flex gap-1'>
                            <Button label='Embalar' icon='pi pi-plus' size='small' outlined onClick={() => abrirEmbalar(v)} />
                            <Button label='Fechar' icon='pi pi-lock' size='small' severity='warning' outlined onClick={() => fecharVolume(v)} />
                        </div>) : null)} style={{ width: '13rem' }} />
                    </DataTable>
                </TabPanel>
            </TabView>
            <Dialog visible={dlgOnda} onHide={() => setDlgOnda(false)} header='Nova onda' modal style={{ width: 'min(96vw, 480px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-6'><label className='bc-label'>Depósito ID *</label><InputText value={fOnda.depositoId || ''} onChange={(e) => setFOnda({ ...fOnda, depositoId: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Código *</label><InputText value={fOnda.codigo || ''} onChange={(e) => setFOnda({ ...fOnda, codigo: e.target.value })} /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Responsável</label><InputText value={fOnda.responsavel || ''} onChange={(e) => setFOnda({ ...fOnda, responsavel: e.target.value })} /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlgOnda(false)} /><Button label='Criar' icon='pi pi-check' onClick={salvarOnda} /></div>
            </Dialog>
            <Dialog visible={dlgItem} onHide={() => setDlgItem(false)} header='Adicionar item' modal style={{ width: 'min(96vw, 440px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-6'><label className='bc-label'>Produto ID *</label><InputText value={fItem.produtoId || ''} onChange={(e) => setFItem({ ...fItem, produtoId: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Qtd *</label><InputNumber value={fItem.qtd} onValueChange={(e) => setFItem({ ...fItem, qtd: e.value })} minFractionDigits={3} /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'><Button label='Cancelar' text severity='secondary' onClick={() => setDlgItem(false)} /><Button label='Adicionar' icon='pi pi-check' onClick={salvarItem} /></div>
            </Dialog>
            <Dialog visible={dlgEmb} onHide={() => setDlgEmb(false)} header={'Embalar no volume ' + (volEmb ? (volEmb.codigo || volEmb.id) : '')} modal style={{ width: 'min(96vw, 420px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-6'><label className='bc-label'>Produto ID *</label><InputText keyfilter='int' value={fEmb.produtoId} onChange={(e) => setFEmb({ ...fEmb, produtoId: e.target.value })} /></div>
                    <div className='bc-form-col-12'><label className='bc-label'>Item da onda ID (opcional — limita à quantidade separada)</label><InputText keyfilter='int' value={fEmb.ondaItemId} onChange={(e) => setFEmb({ ...fEmb, ondaItemId: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Quantidade *</label><InputNumber value={fEmb.quantidade} onValueChange={(e) => setFEmb({ ...fEmb, quantidade: e.value })} minFractionDigits={0} maxFractionDigits={3} min={0} /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'>
                    <Button label='Cancelar' text severity='secondary' onClick={() => setDlgEmb(false)} />
                    <Button label='Embalar' icon='pi pi-check' onClick={embalar} />
                </div>
            </Dialog>
            <Dialog visible={dlgVol} onHide={() => setDlgVol(false)} header={'Novo volume na expedição ' + expId} modal style={{ width: 'min(96vw, 420px)' }}>
                <div className='grid p-fluid'>
                    <div className='bc-form-col-6'><label className='bc-label'>Código *</label><InputText value={fVol.codigo} onChange={(e) => setFVol({ ...fVol, codigo: e.target.value })} /></div>
                    <div className='bc-form-col-6'><label className='bc-label'>Peso (kg)</label><InputNumber value={fVol.peso} onValueChange={(e) => setFVol({ ...fVol, peso: e.value })} minFractionDigits={0} maxFractionDigits={3} min={0} /></div>
                </div>
                <div className='flex justify-end gap-2 mt-3'>
                    <Button label='Cancelar' text severity='secondary' onClick={() => setDlgVol(false)} />
                    <Button label='Criar volume' icon='pi pi-check' onClick={criarVolume} />
                </div>
            </Dialog>
        </div>
    );
};
export default WMS;
